package result;

import java.util.function.Supplier;

/**
 * Represents a supplier of results which can error.
 * This is a functional interface whose functional method is getThrows().
 *
 * @param <T> The return type of the supplier.
 *
 * @author Alan Teesdale (300652164)
 */
public interface FallibleSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return getThrows();
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to get a result.
     *
     * @return the result of the supplier.
     *
     * @throws Throwable if it is unable to get the result.
     */
    T getThrows() throws Throwable;
}

