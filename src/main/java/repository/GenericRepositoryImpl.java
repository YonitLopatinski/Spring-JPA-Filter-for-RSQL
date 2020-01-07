package repository;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import repository.filter.CustomRSQLVisitor;
import repository.model.Field;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GenericRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements GenericRepository<T, ID> {

    private EntityManager entityManager;

    private RSQLParser parser = new RSQLParser();
    private CustomRSQLVisitor<T> visitor;

    public GenericRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;

        this.parser = new RSQLParser();
        this.visitor = new CustomRSQLVisitor<>();
    }

    public GenericRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
    }

    @Override
    public List<T> findAll(String filter) {
        if (!StringUtils.isEmpty(filter)) {
            Node rootNode = parser.parse(filter);
            Specification<T> spec = rootNode.accept(visitor);
            return this.findAll(spec);
        }
        return this.findAll();
    }

    @Override
    @Transactional
    public <T> List<T> insert(Collection<T> entities) {
        return entities.stream().filter(entity -> insert(entity) != null).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public <T> T insert(T entity) {
        try {
            entityManager.persist(entity);
        } catch (Exception e) {
            log.info("Failed to persist object", e);
            return null;
        }
        return entity;
    }

    @Override
    @Transactional
    public int update(List<Field> entry, Predicate predicate) {
        Class<T> clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(clazz);
        Root root = criteriaUpdate.from(clazz);
        root.alias(clazz.getSimpleName());
        entry.stream().forEach(field -> criteriaUpdate.set(field.getName(), field.getValue()));
        return entityManager.createQuery(criteriaUpdate.where(predicate)).executeUpdate();
    }

    @Override
    @Transactional
    public int update(List<Field> entry, String filter) {
        Class<T> clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(clazz);
        Root root = criteriaUpdate.from(clazz);
        root.alias(clazz.getSimpleName());
        entry.stream().forEach(field -> criteriaUpdate.set(field.getName(), field.getValue()));

        Node rootNode = parser.parse(filter);
        Specification<T> specification = rootNode.accept(visitor);
        final Predicate predicate = specification.toPredicate(root, criteriaBuilder.createQuery(), criteriaBuilder);

        return entityManager.createQuery(criteriaUpdate.where(predicate)).executeUpdate();
    }

    @Override
    public <T> List<T> find(Predicate predicate) {
        Class clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<T> root = criteriaQuery.from(clazz);
        root.alias(clazz.getSimpleName());
        return entityManager.createQuery(criteriaQuery.select(root)
                .where(predicate)).getResultList();
    }

    @Override
    @Transactional
    public int delete(Predicate predicate) {
        Class<T> clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(clazz);
        Root root = criteriaDelete.from(clazz);
        root.alias(clazz.getSimpleName());

        return entityManager.createQuery(criteriaDelete.where(predicate)).executeUpdate();
    }

    @Override
    @Transactional
    public int delete(String filter) {
        Class<T> clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(clazz);
        Root root = criteriaDelete.from(clazz);
        root.alias(clazz.getSimpleName());

        Node rootNode = parser.parse(filter);
        Specification<T> specification = rootNode.accept(visitor);
        final Predicate predicate = specification.toPredicate(root, criteriaBuilder.createQuery(), criteriaBuilder);

        return entityManager.createQuery(criteriaDelete.where(predicate)).executeUpdate();
    }

    @Override
    public long count(Predicate predicate) {
        Class clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(clazz);
        root.alias(clazz.getSimpleName());
        criteriaQuery.where(predicate);
        if (criteriaQuery.isDistinct()) {
            criteriaQuery.select(criteriaBuilder.countDistinct(root));
        } else {
            criteriaQuery.select(criteriaBuilder.count(root));
        }
        List<Long> results = entityManager.createQuery(criteriaQuery).getResultList();
        return results.stream().mapToLong(result -> result == null ? 0 : result).sum();
    }

    @Override
    public long count(String filter) {
        Class clazz = getDomainClass();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(clazz);
        root.alias(clazz.getSimpleName());

        Node rootNode = parser.parse(filter);
        Specification<T> specification = rootNode.accept(visitor);
        final Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        criteriaQuery.where(predicate);
        if (criteriaQuery.isDistinct()) {
            criteriaQuery.select(criteriaBuilder.countDistinct(root));
        } else {
            criteriaQuery.select(criteriaBuilder.count(root));
        }
        List<Long> results = entityManager.createQuery(criteriaQuery).getResultList();
        return results.stream().mapToLong(result -> result == null ? 0 : result).sum();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Override
    public Path getPath(String fieldName) {
        Class<T> clazz = getDomainClass();
        Root root = entityManager.getCriteriaBuilder().createQuery().from(clazz);
        root.alias(clazz.getSimpleName());
        return root.get(fieldName);
    }
}
