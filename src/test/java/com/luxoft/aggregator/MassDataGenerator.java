package com.luxoft.aggregator;

import com.google.common.collect.*;
import org.junit.Test;
import rx.*;
import rx.Observable;
import rx.observables.StringObservable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Tests whether mass data does not kill JVM during calculation.
 * @author Dmitry Dobrynin
 */
public class MassDataGenerator {
    private static List<InstrumentPrice> prices;

    public static Observable<String> createFromFile() throws FileNotFoundException {
        List<String> strings = StringObservable.split(
                StringObservable.from(
                        new InputStreamReader(new FileInputStream("src/main/resources/example_input.txt"))),
                "(\r)?\n").toList().toBlocking().single();

        Iterator<String> infinite = Iterators.cycle(strings);

        return Observable.create(new Observable.OnSubscribe<String>() {
            public void call(Subscriber<? super String> subscriber) {
                for (int i = 0; i < 100000000; i++) {
                    try {
                        if (infinite.hasNext())
                            subscriber.onNext(infinite.next());
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        Aggregator aggregator = new Aggregator(createFromFile().publish());
        new StreamDriver(aggregator).run();
    }
}
