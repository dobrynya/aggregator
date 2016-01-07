package com.luxoft.aggregator;

import rx.functions.Func1;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Parses an instrument price from a string.
 * @author Dmitry Dobrynin
 */
public class InstrumentPriceParser implements Func1<String, Either<InstrumentPrice, Tuple<String, Exception>>> {
    public InstrumentPrice parseFrom(String line) {
        if (line != null) {
            String[] parts = line.trim().split("\\s*,\\s*");
            if (parts.length == 3 && !parts[0].isEmpty() && !parts[1].isEmpty() && !parts[2].isEmpty())
                return new InstrumentPrice(parts[0],
                        LocalDate.parse(parts[1], InstrumentPriceUtilities.DATE_FORMATTER),
                        new BigDecimal(parts[2]));
        }
        throw new IllegalArgumentException("Could not parse: " + line + "!");
    }

    public Either<InstrumentPrice, Tuple<String, Exception>> call(String line) {
        try {
            return Either.l(parseFrom(line));
        } catch (Exception e) {
            return Either.r(Tuple.t(line, e));
        }
    }
}
