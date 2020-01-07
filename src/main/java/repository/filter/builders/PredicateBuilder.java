package repository.filter.builders;

import repository.filter.FilterOperationsEnum;
import repository.filter.builders.impl.*;

import javax.persistence.criteria.*;
import java.util.EnumMap;
import java.util.List;

public class PredicateBuilder {
    private static final PredicateBuilder predicateBuilder = new PredicateBuilder();
    EnumMap<FilterOperationsEnum, OperatorBuilder> visitorByOperator;

    private PredicateBuilder() {
        visitorByOperator = new EnumMap<>(FilterOperationsEnum.class);
        visitorByOperator.put(FilterOperationsEnum.EQUAL, new EqualOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.NOT_EQUAL, new NotEqualOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.GREATER_THAN, new GreaterThanOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.GREATER_THAN_OR_EQUAL, new GreaterThanOrEqualOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.LESS_THAN, new LessThanOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.LESS_THAN_OR_EQUAL, new LessThanOrEqualOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.IN, new InOperatorBuilder());
        visitorByOperator.put(FilterOperationsEnum.NOT_IN, new NotInOperatorBuilder());
    }

    public static PredicateBuilder getInstance() {
        return predicateBuilder;
    }

    public Predicate build(FilterOperationsEnum filterOperationsEnum, Root root, CriteriaBuilder builder, String property, List<Object> args) {
        return visitorByOperator.get(filterOperationsEnum).build(root, builder, property, args);
    }
}
