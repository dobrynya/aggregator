package com.luxoft.aggregator;

import rx.Subscriber;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.io.OutputStreamWriter;
import com.google.common.base.Throwables;

/**
 * Consumes instrument prices and stores it at disk.
 * @author Dmitry Dobrynin
 */
public class Flusher extends Subscriber<InstrumentPrice> {
    private Writer writer;

    public Flusher(OutputStream outputStream) {
        writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
    }

    private void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }

    public void onCompleted() {
        close();
    }

    public void onError(Throwable e) {
        close();
    }

    public void onNext(InstrumentPrice price) {
        try {
            String line = String.format("%s,%s,%s\n", price.getName(),
                    price.getDate().format(InstrumentPriceUtilities.DATE_FORMATTER),
                    price.getPrice());
            writer.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            close();
            Throwables.propagate(e);
        }
    }
}
