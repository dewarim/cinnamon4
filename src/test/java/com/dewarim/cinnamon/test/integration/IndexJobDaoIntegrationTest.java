package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

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
}
