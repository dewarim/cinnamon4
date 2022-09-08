package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.index.IndexJob;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

public class IndexJobDao {

    public List<IndexJob> getIndexJobsByFailedCountWithLimit(int status, int limit) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = Map.of("limit", limit, "failed", status);
        return sqlSession.selectList("com.dewarim.cinnamon.model.index.IndexJob.getIndexJobsByFailedCountWithLimit", params, new RowBounds(0, limit));
    }

    public int countJobs() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.index.IndexJob.countJobs");
    }

    public int insertIndexJob(IndexJob job) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.insert("com.dewarim.cinnamon.model.index.IndexJob.insert", job);
    }

    public void commit() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.commit();
    }

    public void delete(IndexJob job) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.index.IndexJob.delete", job.getId());
    }

    public void updateStatus(IndexJob job) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.index.IndexJob.updateStatus", job);
    }
}
