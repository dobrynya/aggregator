package com.luxoft.aggregator;

import java.util.NoSuchElementException;

/**
 * Represents a container for alternative values.
 * @author Dmitry Dobrynin
 */
public abstract class Either<L, R> {
    public boolean isLeft() {
        return false;
    };

    public boolean isRight() {
        return false;
    }

    public L left() {
        throw new NoSuchElementException(String.format("%s.left()!", this));
    }

    public R right() {
        throw new NoSuchElementException(String.format("%s.right()!", this));
    }

    public String toString() {
        return String.format("%s(%s)", isLeft() ? "Left" : "Right", isLeft() ? left() : right());
    }

    /**
     * Constructs a new left instance.
     * @param left specifies left
     * @param <L> left type
     * @param <R> right type
     * @return a newly created left
     */
    public static <L, R> Either<L, R> l(L left) {
        return new Either<L, R>() {
            public boolean isLeft() {
                return true;
            }

            public L left() {
                return left;
            }
        };
    }

    /**
     * Constructs a new right instance
     * @param right specifies right
     * @param <L> left type
     * @param <R> right type
     * @return a newly created right
     */
    public static <L, R> Either<L, R> r(R right) {
        return new Either<L, R>() {
            public boolean isRight() {
                return true;
            }

            public R right() {
                return right;
            }
        };
    }
}
