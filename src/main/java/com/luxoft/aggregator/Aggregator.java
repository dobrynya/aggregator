package com.luxoft.aggregator;

import java.io.*;
import rx.Observable;
import rx.observables.*;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Aggregates information on instruments from the specified file.
 * @author Dmitry Dobrynin
 */
public class Aggregator {
    private ConnectableObservable<String> stream;

    Observable<Either<InstrumentPrice, Tuple<String, Exception>>> parsed =
            Observable.defer(() -> stream.map(new InstrumentPriceParser()));

    Observable<Tuple<String, Exception>> failedToParse =
            Observable.defer(() -> parsed.filter(Either::isRight).map(Either::right));

    private Observable<InstrumentPrice> prices =
            Observable.defer(() -> parsed.filter(Either::isLeft).map(Either::left));

    public Aggregator(ConnectableObservable<String> stream) {
        this.stream = stream;
    }

    public Aggregator(InputStream inputStream) {
        this(StringObservable.split(StringObservable.from(
                new InputStreamReader(inputStream, Charset.forName("UTF-8"))),
                "(\r)?\n").publish()
        );
    }

    public Aggregator(String fileName) throws FileNotFoundException {
        this(new FileInputStream(fileName));
    }

    public Observable<Tuple<String, Exception>> failedToParse() {
        return failedToParse;
    }

    public <R> R attach(Function<Observable<InstrumentPrice>, R> attachment) {
        return attachment.apply(prices);
    }

    public void consumeStream() {
        stream.connect();
    }
}
