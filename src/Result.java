import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<T> {
    public final static record EMPTY() {}
    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T ok;
    Supplier<? extends Throwable> error;


    /**
     * @param ok The okay value of the function.
     */
    private Result(T ok) {
        Objects.requireNonNull(ok);
        this.ok = ok;
        this.error = null;
    }

    /**
     * @param error A supplier of the error that this encompasses.
     */
    private Result(Supplier<? extends Throwable> error) {
        Objects.requireNonNull(error);
        this.ok = null;
        this.error = error;
    }

    /**
     * @param ok The value the result should hold
     * @param <T> The type of the value
     * @return the ok result with a given value
     */
    public static <T> Result<T> of(T ok) {return new Result<>(Objects.requireNonNull(ok));
    }

    /**
     * @param error A supplier of an error - the invalid result
     * @param <T> The type of the result
     * @return The error result with a given supplier
     */
    public static <T> Result<T> of(Supplier<? extends Throwable> error) {
        return new Result<>(Objects.requireNonNull(error));
    }

    /**
     * @param function
     * @param <T>
     * @return Convert the result of a function that could throw into a result.
     *         Which contains either the value or the error
     *         (Immediately runs the provided function)
     */
    public static <T> Result<T> fromFunction(Supplier<T> function) {
        Objects.requireNonNull(function);
        try {
            return new Result<>(function.get());
        } catch (Throwable e) {
            return new Result<>(() -> e);
        }
    }

    /**
     * @param runnable
     * @return Convert the result of the runnable to the empty result, or an error if one occurred.
     */
    public static Result<EMPTY> fromFunction(Runnable runnable) {
        return fromFunction(()-> {
            runnable.run();
            return new EMPTY();
        });
    }


    /**
     * @return The value if it exists, or
     * @throws RuntimeException if the value doesn't exist.
     */
    public T get() throws RuntimeException {
        if (ok == null) {
            throw new RuntimeException(error.get());
        }
        return ok;
    }

    /**
     * @return The error produced by the supplier,
     * throws a null pointer exception if the result isOk
     */
    public Throwable getError() throws NullPointerException {
        Objects.requireNonNull(error, "Result is not the error variant");
        return error.get();
    }


    /**
     * @return true if the result is okay, that is if it is not the err variant.
     */
    public boolean isOk() {
        assert !Objects.equals(ok, error);
        return ok != null;
    }


    /**
     * @return true if the result is **not** okay, that is if it is the err variant.
     */
    public boolean hasError() {
        assert !Objects.equals(ok, error);
        return error!=null;
    }

    /**
     * Run a consumer on the value if the result is okay
     * @param action
     */
    public void ifOk(Consumer<? super T> action) {
        if (isOk()) {
            action.accept(ok);
        }
    }

    /**
     * Run a consumer on the value if the result is okay, otherwise run the empty action.
     * @param action
     * @param emptyAction
     */
    public void ifOkOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (isOk()) {
            action.accept(ok);
        } else {
            emptyAction.run();
        }
    }


    /**
     * Use the mapper to map the value to a different value.
     * Keeps the error if the result is of error type.
     * @param mapper
     * @return
     * @param <U>
     */
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (hasError()) {
            return new Result<>(error);
        } else {
            return new Result<>(Objects.requireNonNull(mapper.apply(ok)));
        }
    }

    /**
     * Works similarly to map, but flattens the result down, keeps the current error if one exists.
     */
    public <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (hasError()) {
            return new Result<>(error);
        }
        Result<U> r = (Result<U>) mapper.apply(ok);
        return Objects.requireNonNull(r);
    }


    public Result<T> or(Supplier<? extends Result<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isOk()) {
            return this;
        }
        @SuppressWarnings("unchecked")
        Result<T> r = (Result<T>) supplier.get();
        return Objects.requireNonNull(r);
    }

    public Optional<T> toOption() {
        return Optional.ofNullable(ok);
    }


    public T orElse(T other) {
        if (isOk()) {return ok;}
        return other;
    }


    public T orElseGet(Supplier<? extends T> supplier) {
        if (isOk()) {return ok;}
        return supplier.get();
    }


    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isOk()) {
            return ok;
        }
        throw exceptionSupplier.get();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Result<?> other
                && Objects.equals(ok, other.ok)
                && Objects.equals(error, other.error);
    }


    @Override
    public int hashCode() {
        return Objects.hash(ok, error);
    }

    @Override
    public String toString() {
        if (isOk()) {
            return "Result["+ok+"]";
        }
        return "Result["+getError()+"]";
    }
}
