package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.index.IndexEvent;
import com.dewarim.cinnamon.model.index.IndexEventType;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What went wrong while indexing: one row per failed index job attempt, with the message Lucene or
 * the indexer produced, written by IndexService and read by the admin endpoint.
 * <p>
 * The table is pruned by {@link #truncate(int)} rather than kept forever - see
 * luceneConfig.truncateIndexEventsAfterRow. Nothing else deletes from it: an index job that
 * eventually succeeds leaves its failures behind on purpose, because the point of the row is to say
 * that indexing was broken at that time, not that it is broken now.
 */
public class IndexEventDao implements CrudDao<IndexEvent> {

    private SqlSession sqlSession;

    public IndexEventDao() {
    }

    public IndexEventDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = CrudDao.super.getSqlSession();
        }
        return sqlSession;
    }

    public void commit() {
        SqlSession sqlSession = getSqlSession();
        sqlSession.commit();
    }

    @Override
    public String getTypeClassName() {
        return IndexEvent.class.getName();
    }

    /**
     * The newest events first, at most limit of them, optionally of one type only. The caller always
     * says how many, because the request that asks for them has a default of its own
     * (ListIndexEventsRequest.DEFAULT_LIMIT) and there is no second default worth keeping here.
     *
     * @param limit     how many rows at most - the caller decides, because the rows carry exception
     *                  messages and can be long
     * @param eventType the type to return, or null for every type
     */
    public List<IndexEvent> listNewest(int limit, IndexEventType eventType) {
        // not Map.of: it rejects a null value, and a null eventType is how the statement is told
        // to leave out the where clause.
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("eventType", eventType);
        return getSqlSession().selectList("com.dewarim.cinnamon.model.index.IndexEvent.listNewest", params);
    }

    public int count() {
        return getSqlSession().selectOne("com.dewarim.cinnamon.model.index.IndexEvent.count");
    }

    /**
     * Delete the oldest rows, as many as asked for.
     *
     * @return the number of rows deleted
     */
    public int truncate(int numberOfRowsToDelete) {
        if (numberOfRowsToDelete < 1) {
            return 0;
        }
        return getSqlSession().delete("com.dewarim.cinnamon.model.index.IndexEvent.truncate", numberOfRowsToDelete);
    }
}
