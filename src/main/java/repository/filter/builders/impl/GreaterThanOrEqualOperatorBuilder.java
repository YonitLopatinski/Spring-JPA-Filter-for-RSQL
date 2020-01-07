package repository.filter.builders.impl;

import repository.filter.builders.OperatorBuilder;

import javax.persistence.criteria.*;
import java.util.List;

public class GreaterThanOrEqualOperatorBuilder implements OperatorBuilder {

    public Predicate build(Root root, CriteriaBuilder builder, String property, List<Object> args) {
        final Object singleArgument = args.get(0);
        return builder.greaterThanOrEqualTo(root.get(property), (Comparable) singleArgument);
    }
}
