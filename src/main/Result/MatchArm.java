import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface MatchArm<T, E, U> {
    static <T, E, U> OkArm<T, E, U> ok(Predicate<T> shouldMap, Function<T, U> mapper) {
        return new OkArm<>(Objects.requireNonNull(shouldMap), Objects.requireNonNull(mapper));
    }

    static <T, E, U> ErrArm<T, E, U> err(Predicate<E> shouldMap, Function<E, U> mapper) {
        return new ErrArm<>(Objects.requireNonNull(shouldMap), Objects.requireNonNull(mapper));
    }
}


record OkArm<T, E, U>(Predicate<T> shouldMap, Function<T, U> mapper) implements MatchArm<T, E, U> {}

record ErrArm<T, E, U>(Predicate<E> shouldMap, Function<E, U> mapper) implements MatchArm<T, E, U> {}
