package core.util.either;

final public class Right<L, R> extends Either<L, R>{
    private Right(R right) {
        super(null, right);
        if (right == null) throw new NullPointerException("Tried creating Right with null");
    }
    public static <L, R> Right<L, R> apply(R right) { return new Right<>(right); }

    @Override public boolean isLeft() { return false; }
    @Override public boolean isRight() { return true; }
}
