package core.util.either;

import core.util.Option;

import java.util.function.Function;

/**
 * Java has no data type to represent a disjunct set. This is why I created Either
 * You may either create a Left or Right that may hold it's corresponding type (L and R respectively).
 * A callee may now expect an Either of <\L, R>, which he has to account for.
 * @param <L> left of the disjunct value
 * @param <R> right of the disjunct value
 */
public abstract class Either<L, R> {
    final L left;
    final R right;

    public Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public abstract boolean isLeft();
    public abstract boolean isRight();

    public LeftProjection<L, R> left() {
        return new LeftProjection<>(this);
    }

    public RightProjection<L, R> right() {
        return new RightProjection<>(this);
    }

    public <T> T fold(Function<L, T> functionL, Function<R, T> functionR) {
        return (this.isLeft()) ? functionL.apply(this.left) : functionR.apply(this.right);
    }

    public Option<R> toOption() { return Option.of(this.right); }
}

