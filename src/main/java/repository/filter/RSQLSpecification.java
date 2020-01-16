package repository.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import org.hibernate.query.criteria.internal.path.RootImpl;
import org.springframework.data.jpa.domain.Specification;
import repository.filter.builders.PredicateBuilder;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RSQLSpecification<T> implements Specification<T> {

    private final String property;
    private final transient ComparisonOperator rsqlOperator;
    private final List<String> arguments;


    public RSQLSpecification(final String property, final ComparisonOperator rsqlOperator, final List<String> arguments) {
        super();
        this.property = property;
        this.rsqlOperator = rsqlOperator;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        final List<Object> args = castArguments(root);
        return PredicateBuilder.getInstance().build(FilterOperationsEnum.getFilterOperator(rsqlOperator), root, builder, property, args);
    }

    private List<Object> castArguments(final Root<T> root) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            final Field declaredField = ((RootImpl) root).getEntityType().getJavaType().getDeclaredField(property);
            final boolean exclude = declaredField.isAnnotationPresent(ExcludeFromFilter.class);
            if (exclude) {
                throw new UnsupportedOperationException("Filtering is not supported for excluded field: " + property);
            }

            final List<Object> args = arguments.stream().map(arg -> {
                if (arg.equals("null")) {
                    return null;
                }
                if (declaredField.getType().isEnum() && getEnumType(declaredField) == EnumType.ORDINAL) {
                    throw new UnsupportedOperationException("Filtering is not supported for ORDINAL EnumType, field: " + property
                            + " Should be defined as @Enumerated(EnumType.STRING)");
                }
                return objectMapper.convertValue(arg, declaredField.getType());
            }).collect(Collectors.toList());

            return args;
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Filtering is not supported for un-existing field: " + property);
        }
    }

    private EnumType getEnumType(Field declaredField) {
        final boolean hasEnumeratedAnnotation = declaredField.isAnnotationPresent(Enumerated.class);
        if (hasEnumeratedAnnotation) {
            return declaredField.getAnnotation(Enumerated.class).value();
        }
        return EnumType.ORDINAL;
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //This module is used for Instant objects serialization
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        return objectMapper;
    }
}