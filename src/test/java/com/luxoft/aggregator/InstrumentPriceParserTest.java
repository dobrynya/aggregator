package com.luxoft.aggregator;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specification on the parser.
 * @author Dmitry Dobrynin
 */
public class InstrumentPriceParserTest {
    InstrumentPriceParser parser = new InstrumentPriceParser();

    @Test
    public void returnsRightInCaseOfInvalidData() throws Exception {
        assertThat(parser.call(null).isRight()).isTrue();
        assertThat(parser.call("instrument,date,").isRight()).isTrue();
        assertThat(parser.call("instrument,date,2.15").isRight());
        assertThat(parser.call("instrument,01-Jan-1997,2-15").isRight()).isTrue();
    }

    @Test
    public void createsInstrumentPrice() {
        Either<InstrumentPrice, Tuple<String, Exception>> price = parser.call("instrument,01-Jan-1997,2.15");
        assertThat(price.isLeft()).isTrue();
        assertThat(price.left()).isEqualTo(new InstrumentPrice("instrument", LocalDate.of(1997, Month.JANUARY, 1),
                        new BigDecimal("2.15")));
    }
}