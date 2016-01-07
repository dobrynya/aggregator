package com.luxoft.aggregator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import com.google.common.io.Files;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import rx.Observable;
import rx.functions.Func1;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import static com.luxoft.aggregator.Tuple.t;
import static java.util.Arrays.asList;

/**
 * Provides useful predicates.
 * @author Dmitry Dobrynin
 */
public class InstrumentPriceUtilities {

    /**
     * Commonly used date formatter.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);


    /**
     * Applies restriction on instrument name
     * @param instrumentPredicate specifies restriction
     * @return true if instrument name satisfies condition
     */
    public static Func1<InstrumentPrice, Boolean> instrument(Func1<String, Boolean> instrumentPredicate) {
        return price -> instrumentPredicate.call(price.getName());
    }

    /**
     * Collects elements if instrument name is specified as an argument.
     * @param instruments instruments to be collected
     * @return true if instrument is specified
     */
    public static Func1<String, Boolean> anyOf(String... instruments) {
        Set<String> instrumentSet = new HashSet<>(asList(instruments));
        return instrumentSet::contains;
    }

    /**
     * Collects elements if instrument name is not specified as an argument.
     * @param instruments instruments to be filtered out
     * @return true if instrument is not in the specified instruments
     */
    public static Func1<String, Boolean> noneOf(String... instruments) {
        return not(anyOf(instruments));
    }

    /**
     * Inverts predicate result.
     * @param original specifies original predicate
     * @param <T> specifies type of element
     * @return inverted predicate
     */
    public static <T> Func1<T, Boolean> not(Func1<T, Boolean> original) {
        return input -> !original.call(input);
    }

    /**
     * Allows to apply restriction on a date.
     * @param datePredicate specifies restriction
     * @return true if date satisfies condition
     */
    public static Func1<InstrumentPrice, Boolean> date(Func1<LocalDate, Boolean> datePredicate) {
        return price -> datePredicate.call(price.getDate());
    }

    /**
     * Aggregates a stream to calculate mean value.
     * @param prices a stream of instrument prices
     * @return stream of a single value
     */
    public static Observable<BigDecimal> mean(Observable<InstrumentPrice> prices) {
        return prices.map(InstrumentPrice::getPrice)
                .reduce(t(BigDecimal.ZERO, 0), (acc, price) -> t(acc._1.add(price), acc._2 + 1))
                .map(acc -> acc._1.divide(new BigDecimal(acc._2), MathContext.DECIMAL64));
    }

    /**
     * Aggregates a stream to calculate maximum price from the supplied stream.
     * @param prices a stream of instrument prices
     * @return stream of a single value
     */
    public static Observable<BigDecimal> max(Observable<InstrumentPrice> prices) {
        return prices.map(InstrumentPrice::getPrice).reduce(BigDecimal.ZERO, BigDecimal::max);
    }

    /**
     * Collects most relevant instrument prices from an input stream.
     * @param prices specifies original stream of instrument prices
     * @param maxElements maximum elements to collect
     * @param relevancy specifies a function to calculate relevancy of elements in ascending order
     * @return a stream of relevant instrument prices
     */
    public static Observable<InstrumentPrice> mostRelevant(Observable<InstrumentPrice> prices, int maxElements,
                                                      Comparator<InstrumentPrice> relevancy) {

        PriorityQueue<InstrumentPrice> accumulator = new PriorityQueue<>(relevancy);
        return prices.reduce(accumulator, (acc, price) -> {
            acc.offer(price);
            if (acc.size() > maxElements) acc.poll();
            return acc;
        }).flatMap(Observable::from);
    }

    /**
     * Enriches a stream of instrument prices with multiplier if it exists for the given instrument.
     * @param prices a stream to be enriched
     * @param provider multiplier provider
     * @return enriched stream
     */
    public static Observable<InstrumentPrice> enrichPrice(Observable<InstrumentPrice> prices,
                                                          MultiplierProvider provider) {
        return prices.map(price -> provider.multiplierFor(price.getName()).map(price::multiply).orElse(price));
    }

    /**
     * Creates in-memory database and creates a table.
     * @return data source
     * @throws IOException if the initializing script could not be loaded
     */
    public static DataSource createDataSource() throws IOException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        new JdbcTemplate(dataSource)
                .execute(Files.asCharSource(new File("src/main/resources/initialize-schema.sql"),
                        Charset.forName("UTF-8")).read());
        return dataSource;
    }
}
