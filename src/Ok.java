import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Ok variant of the result.
 *
 * @param value The value this result holds.
 * @param <T> The type of the value this holds.
 *
 * @author Alan Teesdale (300652164)
 */
record Ok<T>(T value) implements Result<T> {
    @Override
    public T get() throws RuntimeException {
        return value;
    }

    @Override
    public Throwable getError() throws NullPointerException {
        throw new NullPointerException(this + ": Is of the Ok Variant");
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public void ifOk(Consumer<? super T> action) {
        action.accept(value);
    }

    @Override
    public void ifOkOrElse(Consumer<? super T> action, Runnable emptyAction) {
        action.accept(value);
    }

    @Override
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        return Result.of(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper) {
        return (Result<U>) mapper.apply(value);
    }

    @Override
    public Result<T> transformError(Function<? super Throwable, ? extends Throwable> mapper) {
        return this;
    }

    @Override
    public <E extends Throwable> Result<T> transformErrorOfType(Class<E> errorClass, Function<E, ? extends Throwable> mapper) {
        return this;
    }

    @Override
    public <E extends Throwable> Result<T> mapErrorOfType(Class<E> errorClass, Function<E, T> mapper) {
        return this;
    }

    @Override
    public <E extends Throwable> Result<T> flatMapErrorOfType(Class<E> errorClass, Function<E, Result<T>> mapper) {
        return this;
    }

    @Override
    public Result<T> or(Supplier<? extends Result<? extends T>> supplier) {
        return this;
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.of(value);
    }

    @Override
    public T orElse(T other) {
        return value;
    }

    @Override
    public <U> U match(
            Function<T, U> onOk,
            Function<Throwable, U> error
    ) {
        return onOk.apply(value);
    }

    @Override
    public T orElseGet(Supplier<? extends T> supplier) {
        return value;
    }

    @Override
    public Result<T> filter(Predicate<? super T> predicate, Supplier<? extends Throwable> error) {
        if (predicate.test(value)) {
            return this;
        }
        return Result.of(error);
    }

    @Override
    public <E extends Throwable> T orElseThrow(Supplier<? extends E> newError) throws E {
        return value;
    }

    @Override
    public <E extends Throwable> T orElseThrow(Function<? super Throwable, ? extends E> newError) throws E {
        return value;
    }
}
