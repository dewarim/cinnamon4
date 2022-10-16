package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.index.IndexJob;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

public class IndexJobDao {

    private SqlSession sqlSession;

    public List<IndexJob> getIndexJobsByFailedCountWithLimit(int failedCount, int limit) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("failed", failedCount);
        return sqlSession.selectList("com.dewarim.cinnamon.model.index.IndexJob.getIndexJobsByFailedCount", params, new RowBounds(0, limit));
    }

    public int countJobs() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.index.IndexJob.countJobs");
    }

    public int insertIndexJob(IndexJob job) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.insert("com.dewarim.cinnamon.model.index.IndexJob.insert", job);
    }

    public void commit() {
        SqlSession sqlSession = getSqlSession();
        sqlSession.commit();
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
        if (sqlSession != null) {
            return sqlSession;
        }
        return ThreadLocalSqlSession.getSqlSession();
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
        SqlSession session = getSqlSession();
        return session.insert("com.dewarim.cinnamon.model.index.IndexJob.reindexOsds", osdIds);
    }

    public int countFailedJobs() {
        SqlSession session = getSqlSession();
        return session.insert("com.dewarim.cinnamon.model.index.IndexJob.countFailedJobs");
    }

    public static class IndexRows {
        private int folders;
        private int osds;

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
