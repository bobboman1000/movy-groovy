package com.bobenem.movygroovy.util;

import com.bobenem.movygroovy.util.either.Either;
import com.bobenem.movygroovy.util.either.Left;
import com.bobenem.movygroovy.util.either.Right;
import com.bobenem.movygroovy.util.function.ThrowingSupplier;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Since it may not have become clear this boxes an operation that might fail
 * and forces the callee to deal with the failure, may it have occured. The
 * Idea is that you can map a function to the value of the monad and only before
 * having to unbox check if something went wrong, deescalating errors substantially
 *
 * In the future I will create Success and Failure, this will make it possible to match for
 * Success(value) and convert Failures to Success(failure) when calling failed.
 * Will result in some interesting new ways to handle exceptions I suppose
 *
 * The Cunstrctor will then look more like
 *
 * try {
 *     return Success(value)
 * } catch (Exception e) { return Failure(e) }
 *
 *  @param <T> the boxed type the operation ginve to apply will return */
public class Try<T> implements Serializable {
    // all of this cannot be final and I'm still bitter about that
    private T success;
    private Throwable failure;
    private boolean isFailed = false;

    public boolean isFailure() { return isFailed; }
    public boolean isSuccess() { return !isFailed; }

    private Try(Supplier<T> fn) {
        try { this.success = fn.get(); }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception nonFatal) { this.failure = nonFatal; this.isFailed = true; }
    }
    private Try(T t) { this.success = t; }
    private Try(Throwable e) {
        this.isFailed = true;
        this.failure = e;
    }
    private static <U, T> Try<T> apply(U v1, Function<U, T> fn) { return new Try<>(() -> fn.apply(v1)); }
    public static <T> Try<T> apply(Supplier<T> fn) { return new Try<>(fn); }
    public static <T> Try<T> applyThrowing(ThrowingSupplier<T> fn) {
        return new Try<>(() -> { try { return fn.get(); } catch (Throwable e) { throw new RuntimeException(e); } });
    }

    public static <T> Try<T> failed(Throwable e) { return new Try<>(e); }
    public static <T> Try<T> successful(T t) { return new Try<>(t); }

    /** Do not use this. Synonymous with getBy, just here for nicer semantics
     *  @return the state value, check if it is a success first */
    public T success() {
        if (!isSuccess()) throw new NullPointerException("The Try was not successful");
        return this.success;
    }
    /** Do not use this
     *  @return the occured exception, check if it is a failure first */
    public Throwable failure() {
        if (!isFailure()) throw new NullPointerException("The Try was not a failure");
        return this.failure;
    }

    /** register a consumer that will be called with T on Success
     *  @param consumer that takes T
     *  @return this try for chaining */
    public Try<T> onSuccess(Consumer<T> consumer) {
        if (isSuccess()) consumer.accept(this.success); return this;
    }
    /** register a consumer that will be called with the Exception on Failure
     *  @param exceptionConsumer that takes T
     *  @return this try for chaining */
    public Try<T> onFailure(Consumer<Throwable> exceptionConsumer) {
        if (isFailure()) exceptionConsumer.accept(this.failure); return this;
    }

    /** fail early, do not use this, always check if "isSuccess()" before
     *  @return getBy the underling value */
    public T get() {
        if (isFailure()) throw new RuntimeException(this.failure);
        return this.success;
    }

    /** maps a function to the state value
     *  @param fn the function to apply to the boxed value, must return R
     *  @param <R> return type
     *  @return the mapped try */
    public <R> Try<R> map(Function<T, R> fn) {
        if (isFailure()) return Try.failed(this.failure);
        try { // this conversion will fail if for T => R, T doesn't match the state type
            return Try.apply((T) success, fn);
        }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception e) { return Try.failed(e); }
    }

    /** This will take a function that returns another try and return just an try instead of an try[try]
     *  @param fn the function to apply to the boxed value, must return Try of R
     *  @param <R> return type
     *  @return the mapped try */
    public <R> Try<R> flatMap(Function<T, Try<R>> fn) {
        if (isFailure()) return Try.failed(this.failure);
        try { // see @map
            return fn.apply((T) success);
        }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception e) { return Try.failed(e); }
    }

    /** If this was a failure, return other instead
     *  @param other the fallback
     *  @return state or other */
    public T getOrElse(T other) {
        return isFailure() ? other : success;
    }

    /** If this was a failure, call this supplier instead
     *  @param fn the fallback-callback
     *  @return this or the result of the callback */
    public Try<T> orElseTry(Supplier<T> fn) {
        return isFailure() ? Try.apply(fn) : this;
    }

    /** @return an Option of this, empty if failure */
    public Option<T> toOption() {
        return isFailure() ? Option.empty() : Option.of(this.success);
    }

    public Either<Throwable, T> toEither() {
        return isFailure() ? Left.apply(this.failure) : Right.apply(this.success);
    }

    @Override public String toString() {
        return isSuccess() ? String.format("Success(%s)", this.success) : String.format("Failure(%s)", this.failure.getMessage());
    }
}
