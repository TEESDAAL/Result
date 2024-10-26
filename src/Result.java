import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A result type containing either a value of type T or an error.
 *
 * @param <T> The type of the value.
 *
 * @author Alan Teesdale
 */
public sealed interface Result<T> permits Err, Ok {

    /**
     * Convert a method into a result.
     *
     * @param function The function to run.
     * @param <T> The return type of the function.
     *
     * @return Convert the result of a function that could throw into a result.
     *     Which contains either the value or the error.
     *     (Immediately runs the provided function)
     */
    static <T> Result<T> fromFunction(FallibleSupplier<T> function) {
        Objects.requireNonNull(function);
        try {
            return Result.of(function.get());
        } catch (Throwable e) {
            return Result.of(() -> e);
        }
    }

    /**
     * Convert the runnable into a result.
     *
     * @param runnable The runnable that gets converted into a result.
     * @return Convert the result of the runnable to the empty result, or an error if one occurred.
     */
    static Result<Void> fromFunction(FallibleRunnable runnable) {
        Objects.requireNonNull(runnable);
        try {
            runnable.run();
            // needed to bypass the null checks from result.of
            return new Ok<>(null);
        } catch (Throwable e) {
            return Result.of(() -> e);
        }
    }

    /**
     * Create an Ok result holding the value.
     *
     * @param value The value the result should hold.
     * @param <T>   The type of the value.
     * @return the ok result with a given value.
     */
    static <T> Result<T> of(T value) {
        return new Ok<>(Objects.requireNonNull(value));
    }

    /**
     * Create an error variant of the result.
     *
     * @param error A supplier of an error - the invalid result.
     * @param <T> The type of the result.
     * @return The error result with a given supplier.
     */
    static <T> Result<T> of(Supplier<? extends Throwable> error) {
        Objects.requireNonNull(error);
        return new Err<>(error);
    }

    /**
     * Get the value out of the result, throws the error of the result if of the Err variant.
     * Wraps the Error in a runtime exception.
     *
     * @return The value if it exists or,
     *
     * @throws RuntimeException if the value doesn't exist.
     */
    T get() throws RuntimeException;

    /**
     * Get the Error if of the Err variant.
     *
     * @return The error that this result holds
     *
     * @throws NoSuchElementException if the result has no error - if it is the Ok variant.
     */
    Throwable getError() throws NoSuchElementException;

    /**
     * Returns if the result is of the Ok var.
     *
     * @return true if the result is okay, that is if it is not the err variant.
     */
    boolean isOk();


    /**
     * Returns if the result is of the Err variant.
     *
     * @return true if the result is **not** okay, that is if it is the err variant.
     */
    default boolean hasError() {
        return !isOk();
    }

    /**
     * Run a consumer on the value if the result is okay.
     *
     * @param action the action to run on the result
     */
    void ifOk(Consumer<? super T> action);

    /**
     * Run a consumer on the value if the result is okay, otherwise run the empty action.
     *
     * @param action      the action to run on the result
     * @param emptyAction the thing to run if there is no result
     */
    void ifOkOrElse(Consumer<? super T> action, Runnable emptyAction);


    /**
     * Use the mapper to map the value to a different value.
     * Keeps the error if the result is of error type.
     *
     * @param mapper map the value of the result to a different one if it exists
     * @param <U>    the type to map to
     * @return A result containing the mapped value.
     */
    <U> Result<U> map(Function<? super T, ? extends U> mapper);


    /**
     * Works similarly to map, but flattens the result down, keeps the current error if one exists.
     *
     * @param mapper the function that maps the value to the new result
     * @param <U>    The new type of the result
     * @return a new result provided by the mapper given the value of this result
     */
    <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper);

    /**
     * Transform the Error to a different Error type.
     *
     * @param mapper A function that takes the error to a different error type.
     *
     * @return A result which has the Err variant mapped to a different error.
     */
    Result<T> transformError(Function<? super Throwable, ? extends Throwable> mapper);

    /**
     * Transform the Error to a different Error type.
     *
     * @param mapper A function that takes the error to a different error type.
     *
     * @return A result which has the Err variant mapped to a different error.
     */
    <E extends Throwable> Result<T> transformErrorOfType(Class<E> errorClass, Function<E, ? extends Throwable> mapper);

    /**
     * Map the error variant of a particular error to a value
     *
     * @param errorClass The class of the error to map
     * @param mapper The error mapping function (Err(E) -> Ok(T))
     * @param <E> The type of error to map.
     *
     * @return A result which is unchanged unless it's an Err where the error is an instance of E.
     */
    <E extends Throwable> Result<T> mapErrorOfType(Class<E> errorClass, Function<E, T> mapper);

    /**
     * Map the error variant of a particular error to a value
     *
     * @param errorClass The class of the error to map
     * @param mapper The error mapping function (Err(E) -> Ok(T))
     * @param <E> The type of error to map.
     *
     * @return A result which is unchanged unless it's an Err where the error is an instance of E.
     */
    <E extends Throwable> Result<T> flatMapErrorOfType(Class<E> errorClass, Function<E, Result<T>> mapper);

    /**
     * Returns the result from the supplier if this is the Err variant, otherwise returns itself.
     *
     * @param supplier A supplier of a result, which gets run if this is of the Err variant.
     *
     * @return This result if it's Ok, otherwise the result given by the supplier.
     */
    Result<T> or(Supplier<? extends Result<? extends T>> supplier);


    /**
     * Convert this result into an optional.
     *
     * @return An optional which holds the value if it exists.
     */
    Optional<T> toOptional();

    /**
     * Gets the value from the result if it exists, otherwise returns the other value.
     *
     * @param other the value to return if the result is of the Err variant.
     *
     * @return either the value of the result if it exists or the other value.
     */
    T orElse(T other);


    /**
     * A match style statement for the result type, to allow matching over multiple errors.
     *
     * @param onOk         The match arm for okay results
     * @param error        The match arm that gets run if the error doesn't match any of the others
     * @param <U>          the return type
     *
     * @return a U given by the match arms
     */
    <U> U match(
            Function<T, U> onOk,
            Function<Throwable, U> error
    );

    /**
     * Gets the value from the result if it exists, otherwise returns the result of the supplier.
     *
     * @param supplier the supplier to run if the result is of the Err variant.
     *
     * @return either the value of the result if it exists or the result of the supplier.
     */
    T orElseGet(Supplier<? extends T> supplier);

    /**
     * Make a result an error variant if it doesn't match the filter.
     *
     * @param predicate The predicate the value must match.
     * @param error A supplier of an error for the Err case if it doesn't match the predicate.
     *
     * @return A result of Err variant if the value doesn't match the predicate.
     */
    Result<T> filter(Predicate<? super T> predicate, Supplier<? extends Throwable> error);

    /**
     * Throw a custom error if the result is of Err type, otherwise get the value.
     *
     * @param newError a supplier of the new error to throw if of the Err variant.
     *
     * @return The value of the Ok variant.
     */
    <E extends Throwable> T orElseThrow(Supplier<? extends E> newError) throws E;

    /**
     * Throw a custom error if the result is of Err type, otherwise get the value.
     *
     * @param newError a function that converts the stored error to a new error,
     *                 while maintaining the stack trace.
     *
     * @return the value of the Ok variant.
     */
    <E extends Throwable> T orElseThrow(Function<? super Throwable, ? extends E> newError) throws E;
}


