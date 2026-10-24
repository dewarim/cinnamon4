package com.dewarim.cinnamon.application.service.search;

import org.apache.lucene.search.CollectorManager;

import java.util.Arrays;
import java.util.Collection;

/**
 * Creates {@link ResultCollector}s and merges their results.
 * <p>
 * Using a CollectorManager instead of a single Collector is what allows
 * {@code IndexSearcher.search(Query, CollectorManager)} to be used - the single-Collector
 * {@code search(Query, Collector)} is deprecated - and lets a search be sliced across threads
 * later on without touching the collector.
 */
public class ResultCollectorManager implements CollectorManager<ResultCollector, long[]> {

    @Override
    public ResultCollector newCollector() {
        return new ResultCollector();
    }

    /**
     * Merges the per-slice ids into one sorted, duplicate-free array.
     * <p>
     * Sorting makes the result deterministic (the previous implementation returned a
     * {@code HashSet}, whose iteration order was not) and makes deduplication a linear scan.
     * Duplicates are possible when the index holds a stale document for an id, see issue #414.
     */
    @Override
    public long[] reduce(Collection<ResultCollector> collectors) {
        int total = 0;
        for (ResultCollector collector : collectors) {
            total += collector.size();
        }
        if (total == 0) {
            return new long[0];
        }
        long[] merged = new long[total];
        int    offset  = 0;
        for (ResultCollector collector : collectors) {
            System.arraycopy(collector.ids(), 0, merged, offset, collector.size());
            offset += collector.size();
        }
        Arrays.sort(merged);
        int unique = 1;
        for (int i = 1; i < merged.length; i++) {
            if (merged[i] != merged[unique - 1]) {
                merged[unique++] = merged[i];
            }
        }
        return unique == merged.length ? merged : Arrays.copyOf(merged, unique);
    }
}
