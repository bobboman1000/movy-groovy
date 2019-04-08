package com.bobenem.movygroovy.util.either;

import java.util.NoSuchElementException;
import java.util.function.Function;

final public class RightProjection<L, R> {
    private Either<L, R> either;
    RightProjection(Either<L, R> either) { this.either = either; }

    public R get() {
        if (this.either.isLeft()) throw new NoSuchElementException("RightProjection of left");
        return this.either.right;
    }
    public <T> Either<L, T> map(Function<R, T> fn) {
        if (this.either.isLeft()) return Left.apply(either.left);
        return Right.apply(fn.apply(this.either.right));
    }
}
