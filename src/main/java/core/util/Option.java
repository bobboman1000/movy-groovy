package com.bobenem.movygroovy.util;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * I didn't like the way javas optional handled, so I created my own
 *
 * In the future, I will create Some and None, just like in Scala, so you can
 * match for Some(value) and have a comparable non with a serial.
 *
 * @param <T> the boxed type */
public class Option<T> implements Serializable {
    private final T value;
    private Option(T value) { this.value = value; }
    private static Option EMPTY = new Option<Void>(null);


    /* This will always suppose that value is something nullable, no need to worry */
    public static <T> Option<T> of(T value) { return new Option<>(value); }
    public static <T> Option<T> empty() { return EMPTY; }

    public boolean isPresent() { return value != null; }

    public Option<T> ifSome(Consumer<T> consumer) {
        if (isPresent()) consumer.accept(value); return this;
    }
    public Option<T> ifNone(Runnable runnable) {
        if(!isPresent()) runnable.run(); return this;
    }

    /** fail early, do not use this, always check if "isPresent()" before
     *  @return getBy the underling value */
    public T get() {
        if (!isPresent()) throw new NullPointerException("The Option was EMPTY");
        return this.value;
    }

    /** maps a function to the state value
     *  @param fn the function to apply to the state value
     *  @param <R> output type of function
     *  @return Option of the result of fn() */
    public <R> Option<R> map(Function<T, R> fn) {
        /* this conversion will fail if for S => T, S doesn't match the state type
         * the compiler might say this cast is redundat but it isn't. */
        return isPresent() ? Option.of(fn.apply((T) value)) : Option.empty();
    }

    /** This will take a function that returns another option and return just an option instead of an option[option]
     *  @param fn the function to apply to the state value
     *  @param <R> Boxed output type of function
     *  @return Option of the result of fn() */
    public <R> Option<R> flatMap(Function<T, Option<R>> fn) {
        return isPresent() ? fn.apply((T) value) : Option.empty(); // see @map
    }

    /** @return I need not mention to use this with care */
    public T orNull() { return getOrElse(null); }
    /** If the state value is null, this will be returned instead
     *  @param other the fallback
     *  @return state or other */
    public T getOrElse(T other) {
        return isPresent() ? value : other;
    }
    /** If the state value is null, this supplier will be called instead
     *  @param other the fallback-callback
     *  @return state or the result of the callback */
    public T orElseGet(Supplier<T> other) {
        return isPresent() ? value : other.get();
    }

    public Option<T> filter(Predicate<T> condition) {
        if (isPresent() && condition.test(value)) return this;
        return Option.empty();
    }
    /* Bridges the gap between implementations with Javas Optional */
    public Optional<T> toOptional() { return Optional.ofNullable(value); }
    public static <T> Option<T> fromOptional(Optional<T> t) {
        return t.map(Option::of).orElseGet(Option::empty);
    }

    @Override public String toString() {
        if (isPresent()) {
            return String.format(value instanceof Double ? "Some(%.2f)" : "Some(%s)", value);
        } else return "None";
    }
}
