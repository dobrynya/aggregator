package com.luxoft.aggregator;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Contains details on instrument price at the date.
 * @author Dmitry Dobrynin
 */
public class InstrumentPrice {
    private String name;
    private LocalDate date;
    private BigDecimal price;

    public InstrumentPrice(String name, LocalDate date, BigDecimal price) {
        this.name = name;
        this.date = date;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstrumentPrice)) return false;

        InstrumentPrice that = (InstrumentPrice) o;
        return name.equals(that.name) && date.equals(that.date);

    }

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    public String toString() {
        return String.format("InstrumentPrice(%s,%s,%s)", name, date, price);
    }

    public InstrumentPrice multiply(BigDecimal coef) {
        return new InstrumentPrice(name, date, coef.multiply(price));
    }
}
