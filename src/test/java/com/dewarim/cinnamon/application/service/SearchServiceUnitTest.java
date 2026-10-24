package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.application.service.search.BrowsableAcls;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_ACL_ID;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_ID;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_OWNER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that pushing the browse-permission check into the Lucene query selects exactly the same
 * documents as evaluating {@link BrowsableAcls#hasBrowsePermission} in Java, which is what the
 * search did before the filter was pushed down.
 */
public class SearchServiceUnitTest {

    private static final long USER_ID = 7L;

    private record Doc(long id, long aclId, long ownerId) {}

    /** Builds an index whose fields mirror IndexService.addOsdFields. */
    private Directory indexOf(List<Doc> docs) throws IOException {
        Directory directory = new ByteBuffersDirectory();
        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
            for (Doc doc : docs) {
                Document luceneDoc = new Document();
                luceneDoc.add(new Field(LUCENE_FIELD_CINNAMON_CLASS, "OSD", StringField.TYPE_STORED));
                luceneDoc.add(new LongPoint(LUCENE_FIELD_ACL_ID, doc.aclId()));
                luceneDoc.add(new StoredField(LUCENE_FIELD_ACL_ID, doc.aclId()));
                luceneDoc.add(new LongPoint(LUCENE_FIELD_OWNER_ID, doc.ownerId()));
                luceneDoc.add(new StoredField(LUCENE_FIELD_OWNER_ID, doc.ownerId()));
                luceneDoc.add(new LongPoint(LUCENE_FIELD_ID, doc.id()));
                luceneDoc.add(new StoredField(LUCENE_FIELD_ID, doc.id()));
                luceneDoc.add(new NumericDocValuesField(LUCENE_FIELD_ID, doc.id()));
                writer.addDocument(luceneDoc);
            }
            writer.commit();
        }
        return directory;
    }

    private Set<Long> idsMatching(Directory directory, Query filter) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new BooleanQuery.Builder()
                    .add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
                    .add(filter, BooleanClause.Occur.FILTER)
                    .build();
            Set<Long> found = new TreeSet<>();
            for (var scoreDoc : searcher.search(query, Integer.MAX_VALUE).scoreDocs) {
                found.add(searcher.storedFields().document(scoreDoc.doc)
                        .getField(LUCENE_FIELD_ID).numericValue().longValue());
            }
            return found;
        }
    }

    private Set<Long> browsableInJava(List<Doc> docs, BrowsableAcls acls) {
        Set<Long> expected = new TreeSet<>();
        for (Doc doc : docs) {
            if (acls.hasBrowsePermission(doc.aclId(), doc.ownerId())) {
                expected.add(doc.id());
            }
        }
        return expected;
    }

    private void assertFilterMatchesJavaCheck(List<Doc> docs, BrowsableAcls acls) throws IOException {
        Directory directory = indexOf(docs);
        Query     filter    = SearchService.browsePermissionFilter(acls);
        assertNotNull(filter, "filter must exist when the user can browse something");
        assertEquals(browsableInJava(docs, acls), idsMatching(directory, filter));
    }

    @Test
    public void objectAclsOnly() throws IOException {
        List<Doc> docs = List.of(
                new Doc(1L, 10L, 99L),   // browsable acl, someone else's
                new Doc(2L, 11L, 99L),   // not browsable
                new Doc(3L, 10L, USER_ID));
        assertFilterMatchesJavaCheck(docs, new BrowsableAcls(Set.of(10L), Set.of(), USER_ID));
    }

    @Test
    public void ownerAclsApplyOnlyToOwnObjects() throws IOException {
        List<Doc> docs = List.of(
                new Doc(1L, 20L, USER_ID),   // owner acl + owned by user  -> browsable
                new Doc(2L, 20L, 99L),       // owner acl but NOT owned     -> not browsable
                new Doc(3L, 21L, USER_ID));  // owned but acl not browsable -> not browsable
        BrowsableAcls acls = new BrowsableAcls(Set.of(), Set.of(20L), USER_ID);

        assertEquals(Set.of(1L), browsableInJava(docs, acls), "sanity check on the Java predicate");
        assertFilterMatchesJavaCheck(docs, acls);
    }

    @Test
    public void objectAndOwnerAclsCombined() throws IOException {
        List<Doc> docs = List.of(
                new Doc(1L, 10L, 99L),       // via objectAcls
                new Doc(2L, 20L, USER_ID),   // via ownerAcls
                new Doc(3L, 20L, 99L),       // ownerAcl but not owner -> no
                new Doc(4L, 30L, USER_ID));  // neither -> no
        assertFilterMatchesJavaCheck(docs, new BrowsableAcls(Set.of(10L), Set.of(20L), USER_ID));
    }

    @Test
    public void noBrowsableAclsAtAllYieldsNoFilter() {
        assertNull(SearchService.browsePermissionFilter(new BrowsableAcls(Set.of(), Set.of(), USER_ID)),
                "a user who can browse nothing must not run a search at all");
    }

    /**
     * The important one: random acl/owner distributions, checked against the Java predicate that the
     * search used before the push-down.
     */
    @Test
    public void randomisedFilterMatchesJavaCheck() throws IOException {
        Random random = new Random(20261024L);
        for (int round = 0; round < 25; round++) {
            List<Doc> docs = new ArrayList<>();
            for (int i = 0; i < 400; i++) {
                docs.add(new Doc(i, random.nextInt(25), random.nextInt(6)));
            }
            Set<Long> objectAcls = new HashSet<>();
            Set<Long> ownerAcls  = new HashSet<>();
            for (int i = 0; i < 25; i++) {
                if (random.nextInt(4) == 0) objectAcls.add((long) i);
                if (random.nextInt(4) == 0) ownerAcls.add((long) i);
            }
            BrowsableAcls acls = new BrowsableAcls(objectAcls, ownerAcls, USER_ID);
            if (objectAcls.isEmpty() && ownerAcls.isEmpty()) {
                assertNull(SearchService.browsePermissionFilter(acls));
                continue;
            }
            assertFilterMatchesJavaCheck(docs, acls);
        }
    }

    /**
     * A large acl set must not be rejected for exceeding maxClauseCount: LongPoint.newSetQuery builds
     * a single PointInSetQuery rather than one boolean clause per value. Cinnamon allows very high
     * clause counts for client queries, so this must not eat into that budget.
     */
    @Test
    public void manyAclsDoNotConsumeClauseCount() throws IOException {
        List<Doc> docs = new ArrayList<>();
        Set<Long> objectAcls = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            docs.add(new Doc(i, i, 99L));
            objectAcls.add((long) i);
        }
        Directory directory = indexOf(docs);
        Query     filter    = SearchService.browsePermissionFilter(new BrowsableAcls(objectAcls, Set.of(), USER_ID));

        int savedMaxClauseCount = IndexSearcher.getMaxClauseCount();
        try {
            IndexSearcher.setMaxClauseCount(8);
            assertEquals(5000, idsMatching(directory, filter).size());
        } finally {
            IndexSearcher.setMaxClauseCount(savedMaxClauseCount);
        }
        assertTrue(true);
    }
}
