package repository.filter;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.jpa.domain.Specification;

public class CustomRSQLVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    private RSQLSpecificationBuilder<T> builder;

    public CustomRSQLVisitor() {
        builder = new RSQLSpecificationBuilder<>();
    }

    @Override
    public Specification<T> visit(final AndNode node, final Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(final OrNode node, final Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(final ComparisonNode node, final Void params) {
        return builder.createSpecification(node);
    }
}