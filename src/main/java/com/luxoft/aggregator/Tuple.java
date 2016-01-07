package com.luxoft.aggregator;

/**
 * Contains a pair of typed values.
 * @author Dmitry Dobrynin
 */
public class Tuple<A, B> {
    public final A _1;
    public final B _2;

    public Tuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple<?, ?> t = (Tuple<?, ?>) o;
        return _1 != null ? _1.equals(t._1) : t._1 == null && (_2 != null ? _2.equals(t._2) : t._2 == null);

    }

    public int hashCode() {
        return 31 * (_1 != null ? _1.hashCode() : 0) + (_2 != null ? _2.hashCode() : 0);
    }

    public String toString() {
        return String.format("(%s,%s)", _1, _2);
    }

    public static <A, B> Tuple<A, B> t(A _1, B _2) {
        return new Tuple<>(_1, _2);
    }
}
