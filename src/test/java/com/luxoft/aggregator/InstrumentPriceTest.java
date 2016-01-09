package com.luxoft.aggregator;

import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests InstrumentPrice.
 * @author Dmitry Dobrynin
 */
public class InstrumentPriceTest {
    @Test
    public void multiplyCreatesNewInstrumentPriceInstance() {
        InstrumentPrice p1 = new InstrumentPrice("product1", LocalDate.now(), BigDecimal.ONE);

        InstrumentPrice p2 = p1.multiply(BigDecimal.ONE);
        assertThat(p2).isNotSameAs(p1).isEqualToComparingFieldByField(p1);

        InstrumentPrice p3 = p1.multiply(BigDecimal.TEN);
        assertThat(p3.getName()).isEqualTo(p1.getName());
        assertThat(p3.getDate()).isEqualTo(p1.getDate());
        assertThat(p3.getPrice()).isEqualTo(BigDecimal.ONE.multiply(BigDecimal.TEN));
    }
}