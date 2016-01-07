package com.luxoft.aggregator;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import javax.sql.DataSource;
import com.google.common.cache.*;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Searches for the multiplier of the instrument.
 * @author Dmitry Dobrynin
 */
public class MultiplierProvider {
    private DataSource dataSource;
    private LoadingCache<String, Optional<BigDecimal>> multiplierCache =
            CacheBuilder
                    .newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<String, Optional<BigDecimal>>() {
                public Optional<BigDecimal> load(String instrument) throws Exception {
                    return find(instrument);
                }
            });

    public MultiplierProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Optional<BigDecimal> find(String instrument) {
        List<BigDecimal> multipliers = new JdbcTemplate(dataSource).query(
                "select multiplier from INSTRUMENT_PRICE_MODIFIER where name = ?",
                new Object[] {instrument},
                (resultSet, i) -> resultSet.getBigDecimal("multiplier")
        );
        return multipliers.isEmpty() ? Optional.empty() : Optional.of(multipliers.get(0));
    }

    public Optional<BigDecimal> multiplierFor(String instrument) {
        return multiplierCache.getUnchecked(instrument);
    }

    /**
     * Invalidates previously cached value when multiplier is updated.
     * @param instrument instrument cache to be invalidated
     */
    public void invalidateCachedInstrument(String instrument) {
        multiplierCache.invalidate(instrument);
    }

    /**
     * Cleans all cached values when all multipliers are updated.
     */
    public void invalidateCache() {
        multiplierCache.invalidateAll();
    }
}
