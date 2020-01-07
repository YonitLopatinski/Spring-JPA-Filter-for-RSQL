package repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import repository.model.Field;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface GenericRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

    <T> List<T> findAll(String filter);

    <T> List<T> insert(Collection<T> entities);

    <T> T insert(T entity);

    @Deprecated
    int update(List<Field> entry, Predicate predicate);

    int update(List<Field> entry, String filter);

    @Deprecated
    <T> List<T> find(Predicate predicate);

    @Deprecated
    int delete(Predicate predicate);

    int delete(String filter);

    @Deprecated
    long count(Predicate predicate);

    long count(String filter);

    @Deprecated
    Path getPath(String fieldName);

    @Deprecated
    CriteriaBuilder getCriteriaBuilder();
}
