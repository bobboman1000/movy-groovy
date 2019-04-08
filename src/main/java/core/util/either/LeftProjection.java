package com.bobenem.movygroovy.util.either;

import java.util.NoSuchElementException;
import java.util.function.Function;

final public class LeftProjection<L, R> {
    private Either<L, R> either;
    LeftProjection(Either<L, R> either) {
        this.either = either;
    }
    public L get() {
        if (this.either.isRight()) throw new NoSuchElementException("LeftProjection of Right");
        return this.either.left;
    }
    public <T> Either<T, R> map(Function<L, T> fn) {
        if (this.either.isRight()) return Right.apply(either.right);
        return Left.apply(fn.apply(this.either.left));
    }

}
