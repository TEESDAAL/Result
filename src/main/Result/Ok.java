import main.Result;

import java.util.Optional;

/**
 * The Ok variant of the result.
 *
 * @param value The value this result holds.
 * @param <T> The type of the value this holds.
 *
 * @author Alan Teesdale (300652164)
 */
record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public T get() throws RuntimeException {
        return value;
    }

    @Override
    public E getError() throws NullPointerException {
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
    public <U> Result <U, E> map(Function<? super T, ? extends U> mapper) {
        return Result.ok(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper) {
        return (Result<U, E>) mapper.apply(value);
    }

    @SuppressWarnings("unchecked")
	@Override
    public <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper) {
        return (Ok<T, U>) this;
    }


    @SuppressWarnings("unchecked")
	@Override
    public <U> Result<T, U> flatMapError(Function<? super E, Result<? extends T, ? extends U>> mapper) {
		return (Ok<T, U>) this;
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
    public T orElseGet(Supplier<? extends T> supplier) {
        return value;
    }

	@Override
	public Result<T, E> mapMatchingError(Predicate<? super E> shouldMap, Function<? extends E, ? extends T> mapper) {
		return this;
	}

    @Override
    public Result<T, E> flatMapMatchingError(Predicate<? super E> shouldMap, Function<E, Result<? extends T, ? extends E>> mapper) {
        return this;
    }

    @Override
	public Result<T, E> or(Supplier<? extends Result<? extends T, ? extends E>> supplier) {
		return this;
	}

	@Override
	public <U> U match(Function<Result<T, E>, U> defaultArm, @SuppressWarnings("unchecked") MatchArm<T, E, U>... matchArms) {
		for (MatchArm<T, E, U> arm : matchArms) {
			if (arm instanceof OkArm<T, E, U> e && e.shouldMap().test(this.value)) {
				return e.mapper().apply(this.value);
			}
		}
		return defaultArm.apply(this);
	}

	@Override
	public <U> U match(Function<T, U> defaultOk, Function<E, U> defaultErr, @SuppressWarnings("unchecked") MatchArm<T, E, U>... matchArms) {
		for (MatchArm<T, E, U> arm : matchArms) {
			if (arm instanceof OkArm<T, E, U> e && e.shouldMap().test(this.value)) {
				return e.mapper().apply(this.value);
			}
		}
		return defaultOk.apply(this.value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<T, E> filter(Predicate<? super T> predicate, Function<? super T, ? super E> toError) {
		if (predicate.test(this.value)) {
			return this;
		}
		return (Result<T, E>) Result.err(toError.apply(this.value));
	}

	@Override
	public <Er extends Throwable> T orElseThrow(Supplier<? extends Er> newError) throws Er {
		return this.value;
	}

	@Override
	public <Er extends Throwable> T orElseThrow(Function<E, Er> newError) throws Er {
		return this.value;
	}
}
