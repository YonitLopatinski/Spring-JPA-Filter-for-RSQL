package repository.filter.builders.impl;

import repository.filter.builders.OperatorBuilder;

import javax.persistence.criteria.*;
import java.util.List;

public class NotInOperatorBuilder implements OperatorBuilder {

    public Predicate build(Root root, CriteriaBuilder builder, String property, List<Object> args) {
        return builder.not(root.get(property).in(args));
    }
}