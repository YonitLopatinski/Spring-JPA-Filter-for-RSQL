package repository.filter.builders;

import javax.persistence.criteria.*;
import java.util.List;

public interface OperatorBuilder {

    Predicate build(Root root, CriteriaBuilder builder, String property, List<Object> args);

}
