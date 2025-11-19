package main;

import java.util.Optional;

/**
 * The Err variant of the result.
 *
 * @param error a supplier of the error this variant holds.
 * @param <T> The type of result.
 *
 * @author Alan Teesdale (300652164)
 */
record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public T get() throws RuntimeException {
        throw new RuntimeException("Result is error variant with value: " + error);
    }

    @Override
    public E getError() {
        return error;
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
    public <U> Result <U, E> map(Function<? super T, ? extends U> mapper) {
        return (Result<U, E>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper) {
        return (Result<U, E>) this;
    }

    @Override
    public <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper) {
        return new Err<>(mapper.apply(this.error));
    }


    @SuppressWarnings("unchecked")
    @Override
    public Result<T, E> or(Supplier<? extends Result<? extends T, ?extends E>> supplier) {
        return (Result<T, E>) supplier.get();
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
    public <U> U match(
			Function<Result<T, E>, U> defaultArm,
            @SuppressWarnings("unchecked") MatchArm<T, E, U>... matchArms
    ) {
		for (MatchArm<T, E, U> arm : matchArms) {
			if (arm instanceof ErrArm<T, E, U> e && e.shouldMap().test(this.error)) {
				return e.mapper().apply(this.error);
			}
		}
		return defaultArm.apply(this);
	}

	@Override
    public <U> U match(
			Function<T, U> defaultOk,
			Function<E, U> defaultErr,
            @SuppressWarnings("unchecked") MatchArm<T, E, U>... matchArms
    ) {
		for (MatchArm<T, E, U> arm : matchArms) {
			if (arm instanceof ErrArm<T, E, U> e && e.shouldMap().test(this.error)) {
				return e.mapper().apply(this.error);
			}
		}
		return defaultErr.apply(this.error);
	}

    @Override
    public T orElseGet(Supplier<? extends T> supplier) {
        return supplier.get();
    }

    @Override
    public Result<T, E> filter(Predicate<? super T> predicate, Function<? super T, ? super E> toError) {
        return this;
    }

    @Override
    public <Er extends Throwable> T orElseThrow(Supplier<? extends Er> newError) throws Er {
		throw newError.get();
	}

    @Override
    public <Er extends Throwable> T orElseThrow(Function<E, Er> newError) throws Er {
        throw newError.apply(error);
    }

	@Override
	public Result<T, E> mapMatchingError(Predicate<? super E> shouldMap, Function<? super E, ? extends T> mapper) {
		if (shouldMap.test(this.error)) {
			return Result.ok(mapper.apply(this.error));
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> Result<T, U> flatMapError(Function<? super E, Result<? extends T, ? extends U>> mapper) {
		return (Result<T, U>) mapper.apply(this.error);
	}

	
}
