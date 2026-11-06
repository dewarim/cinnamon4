package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.IndexEventDao;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.index.IndexEvent;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;
import static com.dewarim.cinnamon.ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER;
import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static org.junit.jupiter.api.Assertions.*;

public class IndexAndSearchServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(IndexAndSearchServletIntegrationTest.class);

    public static Long osdId;
    public static Long folderId;
    public static Long osdWithContentId;

    @BeforeAll
    public static void initializeObjects() throws IOException {
        // create indexItem for xml-content
        // the index item is created by CreateTestDB.sql, otherwise it may not be ready/available to
        // the indexerService on time (or: in its thread) for when the OSD is created.

        TestObjectHolder toh          = new TestObjectHolder(adminClient, userId);
        File             bun          = new File("data/cinnamon-bun.png");
        Format           imagePng     = TestObjectHolder.formats.stream().filter(f -> f.getName().equals("image.png")).findFirst().orElseThrow();
        ObjectSystemData relatedOsd   = toh.createOsdWithContent("related image", imagePng, bun).osd;
        Long             relatedOsdId = relatedOsd.getId();
        osdWithContentId = relatedOsdId;

        // with XML content:
        toh.createOsd("search-me-osd")
                .createMetaSetType(true)
                .createRelationType()
                .setSummaryOnOsd("<summary><p>test-summary</p></summary>")
                .createOsdMeta("<xml><copyright>ACME Inc., 2023</copyright></xml>")
                .createRelation(relatedOsdId, "<xml><imageSize x='100' y='200'/></xml>")
                .createFolder("search-me-folder", createFolderId)
                .createFolderMeta("<xml><folder-meta-data archived='no'/></xml>");
        osdId    = toh.osd.getId();
        folderId = toh.folder.getId();
        log.info("created search-me objects: osd: {} folder: {}", osdId, folderId);

        // set xml-content:
        client.lockOsd(osdId);
        client.setContentOnLockedOsd(osdId, 1L, new File("pom.xml"));

        // with JSON content:
        toh.selectFormat("json")
                .createOsdWithContent(new File("src/test/resources/test.json"));

        // An idle index job table is not enough here: content is extracted and indexed in later
        // passes (TikaService inserts a second index job of its own), so the job queue can be
        // empty simply because the work has not been enqueued yet. Wait for what the tests below
        // actually query instead.
        assertTrue(waitUntilIndexIsIdle(), "index did not catch up with the test fixtures");
        long contentDeadline = System.currentTimeMillis();
        assertTrue(waitUntilOsdSearchFindsSomething(createTermQuery("xml_content", "cinnamon")),
                () -> "xml content of the pom should be searchable - " + indexState());
        assertTrue(waitUntilOsdSearchFindsSomething(createTermQuery("xml_content", "bob")),
                () -> "json content should be searchable - " + indexState());
        if (CinnamonServer.getConfig().getCinnamonTikaConfig().isUseTika()) {
            // Tika extraction is a pass of its own, and the index job it enqueues afterwards is a
            // third one - so searchForContent's tika_meta assertion needs its own wait, under the
            // same condition that guards the assertion.
            assertTrue(waitUntilOsdSearchFindsSomething(createTermQuery("tika_meta", "delicious")),
                    () -> "tika metadata should be searchable - " + indexState());
        }
        log.info("waited {} ms for content to become searchable", System.currentTimeMillis() - contentDeadline);
        ThreadLocalSqlSession.refreshSession();
    }

    @Test
    public void showInfo() throws IOException {
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true, true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);
        // TODO: would be nice to test with broken XML file.
        // this is just a broken job added via CreateTestDb script.
        assertTrue(indexInfoResponse.getFailedIndexJobs().size() > 0);
        assertEquals(indexInfoResponse.getFailedJobCount(), indexInfoResponse.getFailedIndexJobs().size());
    }

    @Test
    public void searchIds() throws IOException {
        // TODO: filter system indexItems before indexing with XML-Indexers
        // TODO: test filtering for unbrowsable items
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("searchIdsResponse for OSD search: {}", response);
        assertEquals(1, response.getOsdIds().size());
        SearchIdsResponse folderResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-folder</TermQuery></Clause></BooleanQuery>", SearchType.FOLDER);
        log.info("searchIdsResponse for FOLDER search: {}", response);
        assertEquals(1, folderResponse.getFolderIds().size());

        SearchIdsResponse allResponse = client.search("<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>search-me-*</WildcardQuery></Clause></BooleanQuery>", SearchType.ALL);
        assertEquals(1, allResponse.getFolderIds().size());
        assertEquals(1, allResponse.getOsdIds().size());
        assertEquals(osdId, allResponse.getOsdIds().getFirst());
        assertEquals(folderId, allResponse.getFolderIds().getFirst());

    }


    @Test
    public void searchForContent() throws IOException {
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>cinnamon</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("response: {}", mapper.writeValueAsString(response));
        assertTrue(response.getOsdIds().size() > 0);
        long exampleId = response.getOsdIds().getFirst();

        if (CinnamonServer.getConfig().getCinnamonTikaConfig().isUseTika()) {
            SearchIdsResponse imageSearchResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='tika_meta'>delicious</TermQuery></Clause></BooleanQuery>", SearchType.ALL);
            assertEquals(1, imageSearchResponse.getOsdIds().size());
            assertEquals(osdWithContentId, imageSearchResponse.getOsdIds().getFirst());
        }
        verifyOsdSearchResult(client.search(createTermQuery("is_latest_branch", "true"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createTermQuery("osd_name", "related image"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createPointQuery("osd_id", exampleId), SearchType.OSD));
        LocalDate localDate = LocalDate.now();
        verifyOsdSearchResult(client.search(createWildcardQuery("osd_created",
                localDate.toString().replace("-", "") + "*"), SearchType.OSD));
        // TODO: fix problem with indexing file content and running this search test in the same JVM.
        // ElementNameIndexer verified manually :/
        // verifyOsdSearchResult(client.search(createTermQuery("element_names", "dependency"), SearchType.OSD));

    }

    @Test
    public void searchForJsonContent() throws IOException {
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>bob</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("response: {}", mapper.writeValueAsString(response));
        assertTrue(response.getOsdIds().size() > 0);
        long exampleId = response.getOsdIds().getFirst();

        verifyOsdSearchResult(client.search(createTermQuery("is_latest_branch", "true"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createTermQuery("osd_name", "related image"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createPointQuery("osd_id", exampleId), SearchType.OSD));
        LocalDate localDate = LocalDate.now();
        verifyOsdSearchResult(client.search(createWildcardQuery("osd_created",
                localDate.toString().replace("-", "") + "*"), SearchType.OSD));
        // ElementNameIndexer verified manually :/
        // verifyOsdSearchResult(client.search(createTermQuery("element_names", "dependency"), SearchType.OSD));
        SearchIdsResponse jsonName = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='json_name'>bob</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        assertEquals(1, jsonName.getOsdIds().size());
    }

    @Test
    public void searchWithMalformedQuery() {
        assertClientError(() -> client.search("<BooleanQuery><Clause occurs='must'>", SearchType.ALL),
                INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
    }

    @Test
    public void searchWithDeeplyNestedRegexFailsLikeMalformedQuery() {
        // Lucene's recursive regex parser throws StackOverflowError on this, which must not escape the
        // request handling as an Error but fail like any other unparseable query.
        String nested = "(".repeat(200_000) + "a" + ")".repeat(200_000);
        assertClientError(() -> client.search("<RegexQuery fieldName='name'>" + nested + "</RegexQuery>", SearchType.ALL),
                INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
        // server is still fine afterwards:
        verifyOsdSearchResult(assertDoesNotThrow(() -> client.search(createTermQuery("name", "search-me-osd"), SearchType.OSD)));
    }

    private void verifyOsdSearchResult(SearchIdsResponse response) {
        assertNotNull(response.getOsdIds());
        assertFalse(response.getOsdIds().isEmpty());
    }

    private static String createPointQuery(String fieldName, long query) {
        String pointQuery = "<BooleanQuery><Clause occurs='must'><ExactPointQuery fieldName='" + fieldName + "' value='" + query + "' type='long'/></Clause></BooleanQuery>";
        log.debug("pointQuery: {}", pointQuery);
        return pointQuery;
    }

    private static String createTermQuery(String fieldName, String query) {
        String booleanQuery = "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='" + fieldName + "'>" + query + "</TermQuery></Clause></BooleanQuery>";
        log.debug("createTermQuery: {}", booleanQuery);
        return booleanQuery;
    }

    private static String createWildcardQuery(String fieldName, String query) {
        String wildQuery = "<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='" + fieldName + "'>" + query + "</WildcardQuery></Clause></BooleanQuery>";
        log.debug("wildcardQuery: {}", wildQuery);
        return wildQuery;
    }

    @Test
    public void reindexIsForbiddenForNonAdmins() {
        assertClientError(() -> client.reindex(new ReindexRequest()), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void reindexInvalidRequest() {
        assertClientError(() -> adminClient.reindex(new ReindexRequest(List.of(-1L), List.of())), INVALID_REQUEST);
    }

    /**
     * A reindex does not involve Tika: the indexer reads the tika metaset that is already there, and
     * TikaService only ever looks at OSDs which do not have one. So updateTikaMetaset asks for a
     * fresh extraction by deleting the metaset - and that is also the only way to recover documents
     * whose extraction failed while the Tika server was broken or misconfigured.
     */
    @Test
    public void reindexWithUpdateTikaMetasetDeletesTheTikaMetaset() throws IOException {
        MetasetType tikaType = TestObjectHolder.getMetasetType(TIKA_METASET_NAME);
        Long        osdId    = new TestObjectHolder(adminClient, userId).createOsd("reindex-tika-metaset").osd.getId();
        adminClient.createOsdMeta(osdId, "<tika>metadata of a previous extraction</tika>", tikaType.getId());

        // without the flag, a reindex leaves the metaset alone:
        ReindexResponse plainResponse = adminClient.reindex(new ReindexRequest(List.of(osdId), List.of()));
        assertEquals(0, plainResponse.getTikaMetasetsDeleted());
        assertEquals(1, adminClient.getOsdMetas(osdId, List.of(tikaType.getId())).size());

        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(osdId), List.of(), true));
        assertEquals(1, response.getTikaMetasetsDeleted());
        assertTrue(adminClient.getOsdMetas(osdId, List.of(tikaType.getId())).isEmpty(),
                "the tika metaset should be gone, so that TikaService extracts the document again");
        assertTrue(waitUntilIndexIsIdle(), "reindex did not finish");
    }

    /**
     * updateTikaMetaset applies to what the request names: the objects it lists, plus the content of
     * the folders it lists. A folder reindex arrives with an empty osdIds list, and that must not be
     * read as "the whole repository" - it would delete every tika metaset there is and have the
     * entire corpus extracted again.
     */
    @Test
    public void reindexWithUpdateTikaMetasetOnlyTouchesWhatTheRequestNames() throws IOException {
        MetasetType      tikaType       = TestObjectHolder.getMetasetType(TIKA_METASET_NAME);
        TestObjectHolder toh            = new TestObjectHolder(adminClient, userId);
        Long             namedFolderId  = toh.createFolder("tika-reindex-folder", createFolderId).folder.getId();
        Long             inNamedFolder  = toh.createOsd("tika-reindex-in-folder").osd.getId();
        adminClient.createOsdMeta(inNamedFolder, "<tika>metadata of the folder's document</tika>", tikaType.getId());
        Long elsewhere = new TestObjectHolder(adminClient, userId).createOsd("tika-reindex-elsewhere").osd.getId();
        adminClient.createOsdMeta(elsewhere, "<tika>metadata of an unrelated document</tika>", tikaType.getId());

        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(), List.of(namedFolderId), true));

        assertEquals(1, response.getTikaMetasetsDeleted(),
                "only the content of the named folder should have been scheduled for extraction");
        assertTrue(adminClient.getOsdMetas(inNamedFolder, List.of(tikaType.getId())).isEmpty(),
                "the folder's document should be extracted again");
        assertEquals(1, adminClient.getOsdMetas(elsewhere, List.of(tikaType.getId())).size(),
                "an object outside the named folder must keep its tika metaset");
        assertTrue(waitUntilIndexIsIdle(), "reindex did not finish");
    }

    /**
     * The failures TikaService records are reported by the index info endpoint. That is the only
     * place an operator can see them: the documents themselves are indexed and searchable, they
     * just carry no Tika metadata.
     */
    @Test
    public void indexInfoReportsFailedTikaExtractions() throws IOException {
        Long           osdId      = new TestObjectHolder(adminClient, userId).createOsd("tika-extraction-failure").osd.getId();
        TikaFailureDao failureDao = new TikaFailureDao();
        failureDao.recordFailure(osdId, "Tika answered with HTTP status 503");
        failureDao.recordFailure(osdId, "Tika answered with HTTP status 503");
        ThreadLocalSqlSession.getSqlSession().commit();
        try {
            IndexInfoResponse withList = adminClient.getIndexInfo(false, false, true);
            TikaFailure failure = withList.getFailedTikaExtractions().stream()
                    .filter(tikaFailure -> tikaFailure.getOsdId().equals(osdId)).findFirst().orElseThrow();
            // one row per OSD that counts up, rather than one row per attempt:
            assertEquals(2, failure.getAttempts());
            assertEquals("Tika answered with HTTP status 503", failure.getLastError());
            assertNotNull(failure.getLastAttempt());

            // the list is opt-in, the count is always reported:
            IndexInfoResponse withoutList = adminClient.getIndexInfo(false, false, false);
            assertNull(withoutList.getFailedTikaExtractions());
            assertEquals(withList.getFailedTikaExtractionCount(), withoutList.getFailedTikaExtractionCount());
            assertTrue(withoutList.getFailedTikaExtractionCount() >= 1);
        }
        finally {
            failureDao.deleteByOsdIds(List.of(osdId));
            ThreadLocalSqlSession.getSqlSession().commit();
        }
    }

    /**
     * A recorded Tika failure must not stand in the way of deleting its object: tika_failures
     * references objects, and nothing in the delete path clears the row - the foreign key cascades.
     */
    @Test
    public void deletingAnOsdRemovesItsRecordedTikaFailure() throws IOException {
        Long osdId = new TestObjectHolder(adminClient, userId).createOsd("tika-failure-and-delete").osd.getId();
        new TikaFailureDao().recordFailure(osdId, "Tika answered with HTTP status 503");
        ThreadLocalSqlSession.getSqlSession().commit();

        assertTrue(adminClient.deleteOsd(osdId));
        ThreadLocalSqlSession.refreshSession();
        assertTrue(new TikaFailureDao().list().stream()
                        .noneMatch(tikaFailure -> tikaFailure.getOsdId().equals(osdId)),
                "the failure row should have gone with the object");
    }

    @Test
    public void reindexAllTheThings() throws IOException {
        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(), List.of()));
        assertTrue(response.getDocumentsToIndex() > 0);
        assertTrue(response.getFoldersToIndex() > 0);
        // wait for the reindex to finish, so it does not disturb the following tests:
        assertTrue(waitUntilIndexIsIdle(), "reindex did not finish");
    }

    @Test
    public void reindexSpecificObjects() throws IOException {
        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(osdId), List.of(folderId)));
        assertEquals(1, response.getFoldersToIndex());
        assertEquals(1, response.getDocumentsToIndex());

        assertTrue(waitUntilIndexIsIdle(), "reindex did not finish");
        assertTrue(verifySearchFindsPointField("id", osdId, 1), "the reindexed osd must still be searchable");
        assertTrue(verifySearchFindsPointField("id", folderId, 1, SearchType.FOLDER),
                "the reindexed folder must still be searchable");
    }


    /**
     * With waitUntilSearchable enabled, an item must be searchable by the time the client has the
     * response - without the test sleeping or polling at all.
     * <p>
     * Honest note on what this does and does not prove: it pins down the guarantee, but it does
     * <b>not</b> fail against the previous ordering, where the wait ran in DbSessionFilter after
     * RequestResponseFilter had already written the body. Jetty keeps a small response in its output
     * buffer until the request completes - which is after every filter has returned - so the late
     * wait still happened to finish in time. Only a response flushed early (larger than the 32 KB
     * output buffer) would let a client run ahead of the server, and the endpoints that create index
     * jobs all return small responses, which makes that race latent rather than reachable today.
     * The wait is nevertheless done before the write now, so correctness no longer depends on
     * response size and buffer configuration.
     */
    @Test
    public void waitUntilSearchableMakesItemSearchableBeforeTheResponseArrives() throws IOException {
        LuceneConfig luceneConfig = CinnamonServer.config.getLuceneConfig();
        boolean      original     = luceneConfig.isWaitUntilSearchable();
        luceneConfig.setWaitUntilSearchable(true);
        try {
            String name = "wait-until-searchable-" + System.currentTimeMillis();
            var    osd  = new TestObjectHolder(client, userId).createOsd(name).osd;

            // deliberately no awaitCondition() here - that is the whole point of the setting
            SearchIdsResponse response = client.search(createTermQuery("name", name), SearchType.OSD);
            assertEquals(List.of(osd.getId()), response.getOsdIds(),
                    "the new osd must already be searchable when the create response has arrived");
        } finally {
            luceneConfig.setWaitUntilSearchable(original);
        }
    }

    // ticket 389: meta operations do not trigger reindex
    @Test
    public void updateMetaTest() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        assertTrue(waitUntilIndexIsIdle(), "new osd was not indexed");
        toh.createOsdMeta("<xml>issue389</xml>");
        assertTrue(waitUntilIndexIsIdle(), "osd meta was not indexed");

        String termQuery = createTermQuery("meta_content", "issue389");
        awaitCondition("new metaset content to become searchable",
                () -> !client.search(termQuery, SearchType.OSD).getOsdIds().isEmpty());

        SearchIdsResponse response = client.search(termQuery, SearchType.OSD);
        assertEquals(1, response.getOsdIds().size());
        assertEquals(toh.osd.getId(), response.getOsdIds().getFirst());
    }

    // verifySearchResults: a folder that is still in the Lucene index but no longer in the database
    // must be enqueued for removal from the index (the clean-up used to iterate over the unknown OSD ids).
    @Test
    public void unknownFolderIsRemovedFromIndex() throws IOException, SQLException {
        String name            = "unknown-folder-" + System.currentTimeMillis();
        Long   unknownFolderId = new TestObjectHolder(client, userId).createFolder(name, createFolderId).folder.getId();
        String query           = createTermQuery("name", name);
        assertTrue(waitForFolderSearchResult(query, List.of(unknownFolderId)), "new folder should be searchable");

        // remove the folder from the database only, so that the index still contains it:
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        try (PreparedStatement statement = sqlSession.getConnection().prepareStatement("delete from folders where id = ?")) {
            statement.setLong(1, unknownFolderId);
            assertEquals(1, statement.executeUpdate());
        }
        // force: the session does not know about the raw JDBC update, so a plain commit() would skip it
        sqlSession.commit(true);

        // verification hides the unknown folder and enqueues its removal from the index:
        assertEquals(List.of(), client.search(query, SearchType.FOLDER).getFolderIds());

        LuceneConfig luceneConfig = CinnamonServer.config.getLuceneConfig();
        luceneConfig.setVerifySearchResults(false);
        try {
            assertTrue(waitForFolderSearchResult(query, List.of()), "unknown folder should have been removed from the index");
        } finally {
            luceneConfig.setVerifySearchResults(true);
        }
    }

    /**
     * An item can be deleted between an index job being queued and IndexService picking it up.
     * findJobs then has nothing to index: the job row goes, and for a CREATE or UPDATE job that is
     * all that should happen. Handing such a job to the indexer anyway would dereference the
     * missing item, so this asserts no failed index event was recorded - the job row is deleted
     * either way, which makes that event the only observable difference between dropping the job
     * in findJobs and dropping it one step later.
     */
    @Test
    public void jobForAVanishedItemIsDroppedWithoutAFailedIndexEvent() {
        // an item id this suite cannot reach: objects and folders share seq_folder_and_object_ids,
        // which starts at 1 in the test database (at 10000000 in a migrated one) and climbs by a
        // few hundred per run. Ids are never reused, so nothing can claim this one later either.
        long     itemId = 999_000_003L;
        IndexJob job    = new IndexJob(IndexJobType.OSD, itemId, IndexJobAction.UPDATE);
        // committed, since the point is to let the live IndexService thread pick this up:
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            new IndexJobDao(sqlSession).insertIndexJob(job, false);
            sqlSession.commit();
        }
        Long jobId = job.getId();

        assertTrue(waitFor(() -> {
                    try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
                        return new IndexJobDao(sqlSession).getIndexJobById(jobId).isEmpty();
                    }
                }),
                () -> "IndexService should have removed the job for the missing item - " + indexState());

        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            List<IndexEvent> events = new IndexEventDao(sqlSession).list().stream()
                    .filter(event -> jobId.equals(event.getJobId()))
                    .toList();
            assertTrue(events.isEmpty(),
                    () -> "a job whose item is gone should not be handed to the indexer, but it recorded "
                            + events.stream().map(IndexEvent::getMessage).toList());
        }
    }

    private static boolean waitUntilOsdSearchFindsSomething(String query) {
        return waitFor(() -> {
            try {
                return !client.search(query, SearchType.OSD).getOsdIds().isEmpty();
            } catch (IOException e) {
                return false;
            }
        });
    }

    private boolean waitForFolderSearchResult(String query, List<Long> expectedFolderIds) {
        return waitFor(() -> {
            try {
                return client.search(query, SearchType.FOLDER).getFolderIds().equals(expectedFolderIds);
            } catch (IOException e) {
                return false;
            }
        });
    }

}
