/*
 * Copyright (c) 2012 Ingo Wiarda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */
package com.dewarim.cinnamon.application.service.search;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;
import java.util.Arrays;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_ID;

/**
 * Collects the Cinnamon ids of all matching documents.
 * <p>
 * Result sets are deliberately unbounded: callers need complete sets (for example "every image
 * contained in a technical handbook"), so no hit may go missing. To make that affordable, only the
 * id of each hit is retained - 8 bytes per hit in a primitive array, instead of a whole Lucene
 * {@link org.apache.lucene.document.Document} (which measured ~471 bytes per hit).
 * <p>
 * Permission and type filtering is <i>not</i> done here: it is pushed into the Lucene query by
 * {@link com.dewarim.cinnamon.application.service.SearchService}, so documents the user may not
 * browse are never collected in the first place.
 * <p>
 * The id is read from the {@value com.dewarim.cinnamon.api.Constants#LUCENE_FIELD_ID} NumericDocValues
 * field, which is a columnar per-segment read. Indexes written before that field existed fall back to
 * the stored field, so an index keeps working until it has been rebuilt.
 */
public class ResultCollector implements Collector {

    private long[] ids = new long[256];
    private int    size;

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext leafReaderContext) throws IOException {
        // both obtained once per segment, not once per hit
        NumericDocValues idValues     = leafReaderContext.reader().getNumericDocValues(LUCENE_FIELD_ID);
        StoredFields     storedFields = leafReaderContext.reader().storedFields();

        return new LeafCollector() {

            @Override
            public void setScorer(Scorable scorer) {
                // ignore scorer: we collect every match, scores are irrelevant
            }

            @Override
            public void collect(int doc) throws IOException {
                add(readId(doc));
            }

            /**
             * Hits arrive in increasing docId order within a segment, which is what the
             * NumericDocValues iterator requires.
             */
            private long readId(int doc) throws IOException {
                if (idValues != null && idValues.advanceExact(doc)) {
                    return idValues.longValue();
                }
                return storedFields.document(doc).getField(LUCENE_FIELD_ID).numericValue().longValue();
            }
        };
    }

    private void add(long id) {
        if (size == ids.length) {
            ids = Arrays.copyOf(ids, ids.length * 2);
        }
        ids[size++] = id;
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    /**
     * @return the collected ids, in collection order and possibly containing duplicates.
     * Deduplication happens in {@link ResultCollectorManager#reduce}.
     */
    long[] ids() {
        return ids;
    }

    int size() {
        return size;
    }
}
