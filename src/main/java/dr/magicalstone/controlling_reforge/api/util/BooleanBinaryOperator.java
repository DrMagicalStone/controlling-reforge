package dr.magicalstone.controlling_reforge.api.util;

/**
 * Functional Binary Operator for boolean. Primitive version of {@link java.util.function.BinaryOperator}.
 */
@FunctionalInterface
public interface BooleanBinaryOperator {

    boolean applyAsBoolean(boolean left, boolean right);

}
