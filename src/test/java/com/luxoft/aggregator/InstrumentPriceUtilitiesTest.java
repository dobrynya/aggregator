package com.luxoft.aggregator;

import org.junit.Test;
import rx.Observable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import static java.time.DayOfWeek.*;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static com.luxoft.aggregator.InstrumentPriceUtilities.*;

/**
 * Specification on utility methods.
 * @author Dmitry Dobrynin
 */
public class InstrumentPriceUtilitiesTest {
    @Test
    public void instrumentProvidesInstrumentNameToTheSuppliedPredicate() throws Exception {
        assertTrue(instrument("INSTRUMENT1"::equals).call(new InstrumentPrice("INSTRUMENT1", null, null)));
        assertFalse(instrument("INSTRUMENT2"::equals).call(new InstrumentPrice("INSTRUMENT1", null, null)));
    }

    @Test
    public void anyOfReturnsTrueIfInstrumentIsOneOfTheSpecified() throws Exception {
        assertTrue(anyOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT1"));
        assertTrue(anyOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT2"));
        assertTrue(anyOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT3"));
        assertFalse(anyOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT5"));
    }

    @Test
    public void noneOfReturnsTrueIfInstrumentIsNotOneOfTheSpecified() throws Exception {
        assertFalse(noneOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT1"));
        assertFalse(noneOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT2"));
        assertFalse(noneOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT3"));
        assertTrue(noneOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3").call("INSTRUMENT5"));
    }

    @Test
    public void notInvertsSuppliedPredicateValue() throws Exception {
        assertTrue(not(o -> false).call(null));
        assertFalse(not(o -> true).call(null));
    }

    @Test
    public void dateAppliesDatePredicateToInstrumentDate() throws Exception {
        List<InstrumentPrice> prices = asList(
                LocalDate.of(2016, JANUARY, 4),
                LocalDate.of(2016, JANUARY, 5),
                LocalDate.of(2016, JANUARY, 6),
                LocalDate.of(2016, JANUARY, 7),
                LocalDate.of(2016, JANUARY, 8),
                LocalDate.of(2016, JANUARY, 9),
                LocalDate.of(2016, JANUARY, 10)
        ).stream().map(d -> new InstrumentPrice("Instrument", d, BigDecimal.ONE)).collect(Collectors.toList());

        List<InstrumentPrice> filtered = Observable.from(prices)
                .filter(date(d -> asList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY).contains(d.getDayOfWeek())))
                .toList()
                .toBlocking().single();

        for (InstrumentPrice p : filtered)
            assertTrue(LocalDate.of(2016, JANUARY, 9).isAfter(p.getDate()));
    }

    @Test
    public void meanCalculatesMeanValueOnTheSuppliedInstrumentPriceStream() throws Exception {
        List<InstrumentPrice> prices = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).stream()
                .map(i -> new InstrumentPrice("Instrument", LocalDate.now(), new BigDecimal(i)))
                .collect(Collectors.toList());
        BigDecimal meanValues = mean(Observable.from(prices)).toBlocking().single();
        assertThat(meanValues).isEqualTo(new BigDecimal(5.5D));
    }

    @Test
    public void maxProducesOnlyMaximum() throws Exception {
        List<InstrumentPrice> prices = asList(1, 2, 3, 2, 1, 9, 8, 10, 11, 7, 6, 5, 4).stream()
                .map(i -> new InstrumentPrice("Instrument", LocalDate.now(), new BigDecimal(i))).collect(Collectors.toList());

        BigDecimal maximums = max(Observable.from(prices)).toBlocking().single();
        assertThat(maximums).isEqualTo(new BigDecimal(11));
    }

    @Test
    public void mostRelevantReturnsStreamCollectedElemets() {
        Observable<InstrumentPrice> stream = Observable.from(asList(
                new InstrumentPrice("instrument8", LocalDate.of(2016, JANUARY, 8), BigDecimal.valueOf(8)),
                new InstrumentPrice("instrument5", LocalDate.of(2016, JANUARY, 5), BigDecimal.valueOf(5)),
                new InstrumentPrice("instrument4", LocalDate.of(2016, JANUARY, 4), BigDecimal.valueOf(4)),
                new InstrumentPrice("instrument2", LocalDate.of(2016, JANUARY, 2), BigDecimal.valueOf(2)),
                new InstrumentPrice("instrument3", LocalDate.of(2016, JANUARY, 3), BigDecimal.valueOf(3)),
                new InstrumentPrice("instrument1", LocalDate.of(2016, JANUARY, 1), BigDecimal.valueOf(1)),
                new InstrumentPrice("instrument7", LocalDate.of(2016, JANUARY, 7), BigDecimal.valueOf(7)),
                new InstrumentPrice("instrument6", LocalDate.of(2016, JANUARY, 6), BigDecimal.valueOf(6))
        ));

        List<InstrumentPrice> fiveMostRelevantByPrice =
                mostRelevant(stream, 5, (p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                        .toList().toBlocking().single();

        List<InstrumentPrice> expected = asList(
                new InstrumentPrice("instrument4", LocalDate.of(2016, JANUARY, 4), BigDecimal.valueOf(4)),
                new InstrumentPrice("instrument5", LocalDate.of(2016, JANUARY, 5), BigDecimal.valueOf(5)),
                new InstrumentPrice("instrument6", LocalDate.of(2016, JANUARY, 6), BigDecimal.valueOf(6)),
                new InstrumentPrice("instrument7", LocalDate.of(2016, JANUARY, 7), BigDecimal.valueOf(7)),
                new InstrumentPrice("instrument8", LocalDate.of(2016, JANUARY, 8), BigDecimal.valueOf(8))
        );

        assertThat(fiveMostRelevantByPrice).containsAll(expected);
    }

    @Test
    public void enrichPriceShouldReplaceInstrumentPriceIfCorrespondingMultiplierExists() throws IOException {
        Observable<InstrumentPrice> original = Observable.from(asList(
                new InstrumentPrice("INSTRUMENT1", LocalDate.now(), BigDecimal.ONE),
                new InstrumentPrice("INSTRUMENT2", LocalDate.now(), BigDecimal.ONE),
                new InstrumentPrice("INSTRUMENT3", LocalDate.now(), BigDecimal.ONE),
                new InstrumentPrice("INSTRUMENT5", LocalDate.now(), BigDecimal.ONE),
                new InstrumentPrice("INSTRUMENT-UNKNOWN", LocalDate.now(), BigDecimal.ONE)
        ));
        List<InstrumentPrice> enriched = enrichPrice(original, new MultiplierProvider(createDataSource())).toList().toBlocking().single();

        List<InstrumentPrice> expected = asList(
                new InstrumentPrice("INSTRUMENT1", LocalDate.now(), BigDecimal.valueOf(1.05)),
                new InstrumentPrice("INSTRUMENT2", LocalDate.now(), BigDecimal.valueOf(1.10)),
                new InstrumentPrice("INSTRUMENT3", LocalDate.now(), BigDecimal.valueOf(1.15)),
                new InstrumentPrice("INSTRUMENT5", LocalDate.now(), BigDecimal.valueOf(2)),
                new InstrumentPrice("INSTRUMENT-UNKNOWN", LocalDate.now(), BigDecimal.ONE)
        );
        assertThat(enriched).isEqualTo(expected);
    }
}