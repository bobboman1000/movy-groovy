package com.bobenem.movygroovy.util.either;

final public class Left<L, R> extends Either<L, R>{
    private Left(L left) {
        super(left, null);
        if (left == null) throw new NullPointerException("Tried creating Left with null");
    }
    public static <L, R> Left<L, R> apply(L left) { return new Left<>(left); }

    @Override public boolean isLeft() { return true; }
    @Override public boolean isRight() { return false; }
}
