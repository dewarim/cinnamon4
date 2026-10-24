package com.dewarim.cinnamon.application.service.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_ID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultCollectorUnitTest {

    /**
     * @param withDocValues false simulates an index written by an older Cinnamon version, before the
     *                      id was added as a NumericDocValues field
     */
    private Directory indexOf(List<Long> ids, boolean withDocValues) throws IOException {
        Directory directory = new ByteBuffersDirectory();
        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
            for (Long id : ids) {
                Document doc = new Document();
                doc.add(new Field(LUCENE_FIELD_CINNAMON_CLASS, "OSD", StringField.TYPE_STORED));
                doc.add(new LongPoint(LUCENE_FIELD_ID, id));
                doc.add(new StoredField(LUCENE_FIELD_ID, id));
                if (withDocValues) {
                    doc.add(new NumericDocValuesField(LUCENE_FIELD_ID, id));
                }
                writer.addDocument(doc);
            }
            writer.commit();
        }
        return directory;
    }

    private long[] collect(Directory directory) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            return new IndexSearcher(reader).search(new MatchAllDocsQuery(), new ResultCollectorManager());
        }
    }

    @Test
    public void collectsIdsFromDocValues() throws IOException {
        assertArrayEquals(new long[]{1L, 2L, 3L, 42L},
                collect(indexOf(List.of(42L, 1L, 3L, 2L), true)),
                "ids are returned sorted");
    }

    /**
     * An index created before the DocValues field existed must keep working: the collector falls back
     * to the stored field, so a Cinnamon upgrade does not require a reindex before search works again.
     */
    @Test
    public void fallsBackToStoredFieldWhenDocValuesAreMissing() throws IOException {
        assertArrayEquals(new long[]{1L, 2L, 3L, 42L},
                collect(indexOf(List.of(42L, 1L, 3L, 2L), false)),
                "ids must still be readable without the DocValues field");
    }

    @Test
    public void emptyResultYieldsEmptyArray() throws IOException {
        assertArrayEquals(new long[0], collect(indexOf(List.of(), true)));
    }

    @Test
    public void duplicateIdsAreRemoved() throws IOException {
        // a stale document for an already indexed id, cf. issue #414
        assertArrayEquals(new long[]{1L, 2L},
                collect(indexOf(List.of(1L, 2L, 1L, 2L, 1L), true)));
    }

    @Test
    public void reduceSortsAndDeduplicatesAcrossCollectors() {
        ResultCollectorManager manager = new ResultCollectorManager();
        ResultCollector        first   = manager.newCollector();
        ResultCollector        second  = manager.newCollector();
        // simulate two slices having collected overlapping ids
        feed(first, 5L, 1L, 5L);
        feed(second, 3L, 1L);
        assertArrayEquals(new long[]{1L, 3L, 5L}, manager.reduce(List.of(first, second)));
    }

    @Test
    public void reduceOfEmptyCollectorsYieldsEmptyArray() {
        ResultCollectorManager manager = new ResultCollectorManager();
        assertArrayEquals(new long[0], manager.reduce(List.of(manager.newCollector())));
    }

    /**
     * The internal array starts small and has to grow; make sure nothing is lost or truncated.
     */
    @Test
    public void growsBeyondInitialCapacity() throws IOException {
        int count = 5000;
        List<Long> ids = new ArrayList<>();
        for (long i = count; i > 0; i--) {
            ids.add(i);
        }
        long[] collected = collect(indexOf(ids, true));
        assertEquals(count, collected.length);
        assertEquals(1L, collected[0]);
        assertEquals(count, collected[collected.length - 1]);
    }

    /**
     * Feeds every document of a small in-memory index into the given collector, so the merge in
     * {@link ResultCollectorManager#reduce} can be tested with more than one collector - which a
     * single-threaded search does not produce on its own.
     */
    private void feed(ResultCollector collector, long... ids) {
        try {
            Directory directory = indexOf(Arrays.stream(ids).boxed().toList(), true);
            try (DirectoryReader reader = DirectoryReader.open(directory)) {
                for (LeafReaderContext leaf : reader.leaves()) {
                    LeafCollector leafCollector = collector.getLeafCollector(leaf);
                    // ascending docIds, as the NumericDocValues iterator requires
                    for (int doc = 0; doc < leaf.reader().maxDoc(); doc++) {
                        leafCollector.collect(doc);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
