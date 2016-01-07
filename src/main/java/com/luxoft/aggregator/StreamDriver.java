package com.luxoft.aggregator;

import rx.Observable;

import java.io.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
import static com.luxoft.aggregator.InstrumentPriceUtilities.*;
import static java.lang.String.format;
import static java.lang.System.out;

/**
 * Implements business logic of consuming stream.
 * @author Dmitry Dobrynin
 */
public class StreamDriver {
    Aggregator agg;

    public StreamDriver(Aggregator agg) {
        this.agg = agg;
    }

    public void run() throws IOException {
        MultiplierProvider multiplierProvider = new MultiplierProvider(createDataSource());

        agg.failedToParse().forEach(StreamDriver::printError);

        Observable<InstrumentPrice> validPrices = agg.attach(StreamDriver::checkNonFutureAndBusinessDay);

        enrichPrice(validPrices, multiplierProvider)
                .subscribe(new Flusher(new FileOutputStream("multiplied.txt")));

        Observable.combineLatest(meanOfInstr1(validPrices), meanOfInstr2(validPrices), maxOfInstr3(validPrices),
                sumOfMostNewInstrumentPrices(validPrices), Arrays::asList)
                .forEach(out::println);

        agg.consumeStream();
    }

    public static void main(String[] args) throws IOException {
        new StreamDriver(new Aggregator("src/main/resources/example_input.txt")).run();
    }

    public static Observable<InstrumentPrice> checkNonFutureAndBusinessDay(Observable<InstrumentPrice> prices) {
        LocalDate currentDate = LocalDate.of(2014, Month.DECEMBER, 19);
        return prices.filter(date(d -> !d.isAfter(currentDate) && d.getDayOfWeek().ordinal() < 5));
    }

    public static void printError(Tuple<String, Exception> error) {
        out.println(format("Failed to parse %s because of %s!", error._1, error._2));
    }

    public static Observable<Optional<BigDecimal>> meanOfInstr1(Observable<InstrumentPrice> prices) {
        return mean(prices.filter(instrument(anyOf("INSTRUMENT1"))))
                .map(Optional::of).defaultIfEmpty(Optional.empty());
    }

    public static Observable<Optional<BigDecimal>> meanOfInstr2(Observable<InstrumentPrice> prices) {
        return mean(prices
                .filter(instrument(anyOf("INSTRUMENT2")))
                .filter(date(d -> d.getMonth() == Month.NOVEMBER && d.getYear() == 2014))
        ).map(Optional::of).defaultIfEmpty(Optional.empty());
    }

    public static Observable<Optional<BigDecimal>> maxOfInstr3(Observable<InstrumentPrice> prices) {
        return max(prices.filter(instrument(anyOf("INSTRUMENT3"))))
                .map(Optional::of).defaultIfEmpty(Optional.empty());
    }

    public static Observable<Optional<BigDecimal>> sumOfMostNewInstrumentPrices(Observable<InstrumentPrice> prices) {
        return mostRelevant(
                prices.filter(instrument(noneOf("INSTRUMENT1", "INSTRUMENT2", "INSTRUMENT3"))), 10,
                ((o1, o2) -> o1.getDate().compareTo(o2.getDate()))
        ).map(InstrumentPrice::getPrice)
                .reduce(BigDecimal::add).map(Optional::of).defaultIfEmpty(Optional.empty());
    }
}
