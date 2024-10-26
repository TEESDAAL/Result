import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Err variant of the result.
 *
 * @param error a supplier of the error this variant holds.
 * @param <T> The type of result.
 *
 * @author Alan Teesdale (300652164)
 */
record Err<T>(Supplier<? extends Throwable> error) implements Result<T> {
    @Override
    public T get() throws RuntimeException {
        throw new RuntimeException(error.get());
    }

    @Override
    public Throwable getError() {
        return error.get();
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public void ifOk(Consumer<? super T> action) {
    }

    @Override
    public void ifOkOrElse(Consumer<? super T> action, Runnable emptyAction) {
        emptyAction.run();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        return (Result<U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper) {
        return (Result<U>) this;
    }

    @Override
    public Result<T> transformError(Function<? super Throwable, ? extends Throwable> mapper) {
        return new Err<>(() -> mapper.apply(this.error.get()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Throwable> Result<T> transformErrorOfType(Class<E> errorClass, Function<E, ? extends Throwable> mapper) {
        if (errorClass.isAssignableFrom(this.getError().getClass())) {
            return Result.of(() -> mapper.apply((E) this.getError()));
        }

        return this;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Result<T> or(Supplier<? extends Result<? extends T>> supplier) {
        return (Result<T>) supplier.get();
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public <U> U match(Function<T, U> onOk, Function<Throwable, U> error) {
        return error.apply(this.error.get());
    }

    @Override
    public T orElseGet(Supplier<? extends T> supplier) {
        return supplier.get();
    }

    @Override
    public Result<T> filter(Predicate<? super T> predicate, Supplier<? extends Throwable> error) {
        return this;
    }

    @Override
    public <E extends Throwable> T orElseThrow(Supplier<? extends E> newError) throws E {
        throw newError.get();
    }

    @Override
    public <E extends Throwable> T orElseThrow(Function<? super Throwable, ? extends E> newError) throws E{
        throw newError.apply(error.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Result<T> mapErrorOfType(Class<E> errorClass, Function<E, T> mapper) {
        if (errorClass.isAssignableFrom(this.getError().getClass())) {
            return Result.of(mapper.apply((E) this.getError()));
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Result<T> flatMapErrorOfType(Class<E> errorClass, Function<E, Result<T>> mapper) {
        if (errorClass.isAssignableFrom(this.getError().getClass())) {
            return mapper.apply((E) this.getError());
        }

        return this;
    }


    @Override
    public String toString() {
        return "Err[" + error.get() + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Err<?> err = (Err<?>) object;
        return Objects.equals(this.getError(), err.getError());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getError());
    }
}
