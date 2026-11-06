package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.index.TikaFailure;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * The OSDs whose Tika extraction failed in a way that is specific to the document, with the number
 * of attempts made so far. TikaService writes these rows, removes them again when an extraction
 * finally succeeds, and stops trying an OSD once its attempts reach maxTikaAttempts. The rows are
 * reported by the index info endpoint and cleared by a reindex with updateTikaMetaset.
 */
public class TikaFailureDao {

    /**
     * Keeps a single row from growing without bound: the message may be an exception's, which can
     * be arbitrarily long, and nothing reads more than the beginning of it.
     */
    static final int MAX_ERROR_LENGTH = 1000;

    private SqlSession sqlSession;

    public TikaFailureDao() {
    }

    public TikaFailureDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    /**
     * How many rows {@link #list()} returns at most. The table grows with the size of the
     * repository rather than with the number of mistakes, so the list cannot be unbounded: see the
     * statement's comment.
     */
    public static final int LIST_LIMIT = 1000;

    /**
     * The most recently failed objects, newest first, up to {@link #LIST_LIMIT}. Use
     * {@link #count()} to find out whether there are more.
     */
    public List<TikaFailure> list() {
        return list(LIST_LIMIT);
    }

    public List<TikaFailure> list(int limit) {
        return getSqlSession().selectList("com.dewarim.cinnamon.model.index.TikaFailure.list",
                Map.of("limit", limit));
    }

    public int count() {
        return getSqlSession().selectOne("com.dewarim.cinnamon.model.index.TikaFailure.count");
    }

    /**
     * Raise the attempt counter of this OSD, or start it at 1.
     */
    public void recordFailure(Long osdId, String error) {
        String shortError = error == null ? "" : error.length() > MAX_ERROR_LENGTH
                ? error.substring(0, MAX_ERROR_LENGTH) : error;
        Map<String, Object> params = Map.of("osdId", osdId, "error", shortError);
        getSqlSession().insert("com.dewarim.cinnamon.model.index.TikaFailure.recordFailure", params);
    }

    /**
     * Forget the failures of these OSDs, which makes TikaService try them again.
     */
    public void deleteByOsdIds(List<Long> osdIds) {
        if (osdIds.isEmpty()) {
            return;
        }
        SqlSession sqlSession = getSqlSession();
        CrudDao.partitionLongList(osdIds).forEach(partition ->
                sqlSession.delete("com.dewarim.cinnamon.model.index.TikaFailure.deleteByOsdIds", partition));
    }

    public void deleteAll() {
        getSqlSession().delete("com.dewarim.cinnamon.model.index.TikaFailure.deleteAll");
    }

    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    public TikaFailureDao setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }
}
