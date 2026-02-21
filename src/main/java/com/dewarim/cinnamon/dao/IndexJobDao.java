package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IndexJobDao {

    private static final Logger log = LogManager.getLogger(IndexJobDao.class);
    private SqlSession sqlSession;

    public IndexJobDao() {
    }

    public IndexJobDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<IndexJob> getIndexJobsByFailedCountWithLimit(int failedCount, int limit) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("failed", failedCount);
        return sqlSession.selectList("com.dewarim.cinnamon.model.index.IndexJob.getIndexJobsByFailedCount", params, new RowBounds(0, limit));
    }

    public Optional<IndexJob> getIndexJobById(Long id) {
        SqlSession sqlSession = getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.index.IndexJob.getIndexJobById", id));
    }

    public int countJobs() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.index.IndexJob.countJobs");
    }

    /**
     *
     * @param job the index job to insert
     * @param waitUntilSearchable if true, the job will be added to the request scope and the client will have to wait
     *                            until the item is searchable. This obviously only makes sense for synchronous actions,
     *                            so background threads should not set it to true as it won't have any effect aside from a possible memory leak.
     *                            <br>
     *                            This flag is ignored if IndexJobAction is of DELETE type.
     * @return rows changed by this action (ideally, this should return 1)
     */
    public int insertIndexJob(IndexJob job, boolean waitUntilSearchable) {
        SqlSession sqlSession  = getSqlSession();
        int        rowsChanged = sqlSession.insert("com.dewarim.cinnamon.model.index.IndexJob.insert", job);
        if (waitUntilSearchable && job.getAction() != IndexJobAction.DELETE) {
            log.debug("Adding index job to RequestScope: {}", job);
            RequestScope.addIndexJob(job);
        }
        return rowsChanged;
    }

    public void delete(IndexJob job) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.index.IndexJob.delete", job.getId());
    }

    public List<IndexJob> list() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.index.IndexJob.list");
    }

    public void updateStatus(IndexJob job) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.index.IndexJob.updateStatus", job);
    }

    public IndexJobDao setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }

    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    public IndexRows fullReindex() {
        SqlSession session = getSqlSession();
        int        folders = session.insert("com.dewarim.cinnamon.model.index.IndexJob.reindexAllFolders");
        int        osds    = session.insert("com.dewarim.cinnamon.model.index.IndexJob.reindexAllOsds");
        return new IndexRows(folders, osds);
    }

    public int reindexFolders(List<Long> folderIds) {
        SqlSession session = getSqlSession();
        return session.insert("com.dewarim.cinnamon.model.index.IndexJob.reindexFolders", folderIds);
    }

    public int reindexOsds(List<Long> osdIds) {
        SqlSession          session = getSqlSession();
        Map<String, Object> params  = Map.of("ids", osdIds);
        return session.insert("com.dewarim.cinnamon.model.index.IndexJob.reindexOsds", params);
    }

    public int countFailedJobs() {
        SqlSession session = getSqlSession();
        return session.selectOne("com.dewarim.cinnamon.model.index.IndexJob.countFailedJobs");
    }


    public void reIndexFolderContent(Long folderId) {
        List<Long> ids = new OsdDao().getObjectsByFolderId(folderId, false, VersionPredicate.ALL).stream().map(ObjectSystemData::getId).toList();
        if (ids.isEmpty()) {
            return;
        }
        reindexOsds(ids);
    }

    public List<IndexJob> listFailedIndexJobs() {
        SqlSession session = getSqlSession();
        return session.selectList("com.dewarim.cinnamon.model.index.IndexJob.listFailedIndexJobs");
    }

    public static class IndexRows {
        private final int folders;
        private final int osds;

        public IndexRows(int folders, int osds) {
            this.folders = folders;
            this.osds = osds;
        }

        public int getFolderRowCount() {
            return folders;
        }

        public int getOsdRowCount() {
            return osds;
        }
    }
}
