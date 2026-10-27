package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexJobDaoIntegrationTest extends CinnamonIntegrationTest {

    /**
     * IndexService reads a job, loads a snapshot of its item, indexes that snapshot and only then
     * deletes the job - by id. A change committed inside that window inserts a job of its own, and
     * index_jobs holds one row per (job_type, item_id): if that insert were simply dropped, the
     * index would keep the document built from the stale snapshot and no job would be left to
     * retry it. The change would be lost silently, with no failed job and nothing logged.
     * <br>
     * So an insert for an item that already has a pending job takes the row over with a fresh id,
     * which turns the in-flight indexer's delete into a no-op and leaves the work queued.
     * <br>
     * This runs in an uncommitted transaction of its own, so the IndexService thread working
     * alongside this test can neither see nor delete these rows - no timing involved.
     */
    @Test
    public void jobForANewerChangeSurvivesDeletionOfTheJobItReplaced() {
        // an item id no test object uses, and nothing is committed, so nothing is ever indexed:
        long itemId = 999_000_001L;
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            IndexJobDao jobDao = new IndexJobDao(sqlSession);

            IndexJob firstJob = new IndexJob(IndexJobType.OSD, itemId, IndexJobAction.UPDATE);
            jobDao.insertIndexJob(firstJob, false);
            Long firstId = firstJob.getId();
            assertTrue(jobDao.getIndexJobById(firstId).isPresent(), "the first job should be in the database");

            // what a request does when it changes the same item while the indexer is working on it:
            IndexJob secondJob = new IndexJob(IndexJobType.OSD, itemId, IndexJobAction.UPDATE);
            jobDao.insertIndexJob(secondJob, false);
            Long secondId = secondJob.getId();
            assertNotEquals(firstId, secondId, "the second job should have an id of its own");
            assertTrue(jobDao.getIndexJobById(secondId).isPresent(),
                    "the newer change should be the pending job now, but its id is not in the database");
            assertTrue(jobDao.getIndexJobById(firstId).isEmpty(),
                    "the id the indexer is holding should be gone, so that it cannot delete the newer job");

            // and now the indexer finishes indexing its stale snapshot and deletes what it read:
            jobDao.delete(firstJob);
            assertTrue(jobDao.getIndexJobById(secondId).isPresent(),
                    "deleting the job the indexer read must not remove the job for the newer change");

            sqlSession.rollback();
        }
    }

    /**
     * A job that failed keeps its failure count, and IndexService stops picking it up once that
     * count is past maxIndexAttempts. A new change to the same item has to hand it those attempts
     * back: the earlier failure may have been about the state the job was carrying, and since only
     * one job per item can exist, a dead row would otherwise block every later change to that item
     * for good.
     */
    @Test
    public void aNewChangeGivesAFailedJobItsAttemptsBack() {
        long itemId = 999_000_002L;
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            IndexJobDao jobDao = new IndexJobDao(sqlSession);

            IndexJob job = new IndexJob(IndexJobType.OSD, itemId, IndexJobAction.UPDATE);
            jobDao.insertIndexJob(job, false);
            job.setFailed(99);
            jobDao.updateStatus(job);
            assertEquals(99, jobDao.getIndexJobById(job.getId()).orElseThrow().getFailed().intValue(),
                    "the job should be recorded as failed before the next change arrives");

            IndexJob newChange = new IndexJob(IndexJobType.OSD, itemId, IndexJobAction.UPDATE);
            jobDao.insertIndexJob(newChange, false);
            assertEquals(0, jobDao.getIndexJobById(newChange.getId()).orElseThrow().getFailed().intValue(),
                    "a new change should start over with a full set of attempts");

            sqlSession.rollback();
        }
    }

    /**
     * maxBatchSize has to be applied by the database. This used to pass MyBatis' RowBounds to a
     * statement with no LIMIT, and RowBounds paginates in memory by default: every pending job row
     * was fetched and all but the first maxBatchSize thrown away, which is the opposite of what a
     * batch size is for on an installation with a backlog.
     * <br>
     * The row count cannot tell the two apart - in-memory truncation returns the same rows - so
     * this asserts the statement itself carries the limit.
     */
    @Test
    public void theBatchLimitIsAppliedByTheDatabase() {
        long itemIdBase = 999_000_100L;
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            IndexJobDao jobDao = new IndexJobDao(sqlSession);
            for (int i = 0; i < 5; i++) {
                jobDao.insertIndexJob(new IndexJob(IndexJobType.OSD, itemIdBase + i, IndexJobAction.UPDATE), false);
            }

            String sql = sqlSession.getConfiguration()
                    .getMappedStatement("com.dewarim.cinnamon.model.index.IndexJob.getIndexJobsByFailedCount")
                    .getBoundSql(Map.of("failed", 1, "limit", 3))
                    .getSql();
            assertTrue(sql.toUpperCase().contains("LIMIT"),
                    "the statement has to limit the rows itself, otherwise the whole job table is fetched: " + sql);
            assertEquals(3, jobDao.getIndexJobsByFailedCountWithLimit(1, 3).size(),
                    "and the limit should still be honoured");

            sqlSession.rollback();
        }
    }
}
