package repository.filter;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RSQLSpecificationBuilder<T> {

    public Specification<T> createSpecification(final LogicalNode logicalNode) {

        List<Specification<T>> specifications = logicalNode.getChildren()
                .stream()
                .map(node -> createSpecification(node))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Specification<T> result = specifications.get(0);
        if (logicalNode.getOperator().equals(LogicalOperator.AND)) {
            for (int i = 1; i < specifications.size(); i++) {
                result = Specification.where(result).and(specifications.get(i));
            }
        } else if (logicalNode.getOperator().equals(LogicalOperator.OR)) {
            for (int i = 1; i < specifications.size(); i++) {
                result = Specification.where(result).or(specifications.get(i));
            }
        }

        return result;
    }

    public Specification<T> createSpecification(final ComparisonNode comparisonNode) {
        return Specification.where(new RSQLSpecification<T>(comparisonNode.getSelector(), comparisonNode.getOperator(), comparisonNode.getArguments()));
    }

    private Specification<T> createSpecification(final Node node) {
        if (node instanceof LogicalNode) {
            return createSpecification((LogicalNode) node);
        }
        if (node instanceof ComparisonNode) {
            return createSpecification((ComparisonNode) node);
        }
        return null;
    }
}