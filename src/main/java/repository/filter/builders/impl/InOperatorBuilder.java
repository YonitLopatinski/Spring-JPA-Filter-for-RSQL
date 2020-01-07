package repository.filter.builders.impl;

import repository.filter.builders.OperatorBuilder;

import javax.persistence.criteria.*;
import java.util.List;

public class InOperatorBuilder implements OperatorBuilder {

    public Predicate build(Root root, CriteriaBuilder builder, String property, List<Object> args) {
        return root.get(property).in(args);
    }
}