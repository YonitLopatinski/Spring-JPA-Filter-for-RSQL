package repository.filter;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

import java.util.Arrays;

public enum FilterOperationsEnum {
    EQUAL(RSQLOperators.EQUAL),
    NOT_EQUAL(RSQLOperators.NOT_EQUAL),
    GREATER_THAN(RSQLOperators.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(RSQLOperators.GREATER_THAN_OR_EQUAL),
    LESS_THAN(RSQLOperators.LESS_THAN),
    LESS_THAN_OR_EQUAL(RSQLOperators.LESS_THAN_OR_EQUAL),
    IN(RSQLOperators.IN),
    NOT_IN(RSQLOperators.NOT_IN);

    private ComparisonOperator rsqlOperator;

    FilterOperationsEnum(final ComparisonOperator rsqlOperator) {
        this.rsqlOperator = rsqlOperator;
    }

    public static FilterOperationsEnum getFilterOperator(final ComparisonOperator rsqlOperator) {
        return Arrays.stream(values())
                .filter(operation -> operation.getRsqlOperator().equals(rsqlOperator))
                .findAny()
                .orElseThrow(() -> new UnsupportedOperationException("Operator " + rsqlOperator + " is not supported for RSQL"));
    }

    private ComparisonOperator getRsqlOperator() {
        return rsqlOperator;
    }
}