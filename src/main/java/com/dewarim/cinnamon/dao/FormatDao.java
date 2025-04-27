package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.TimeUnit;

public class FormatDao implements CrudDao<Format> {

    private static final Cache<Long, Format> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private SqlSession sqlSession;

    public FormatDao() {
    }

    public FormatDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public String getTypeClassName() {
        return Format.class.getName();
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    @Override
    public boolean useCache() {
        return true;
    }

    @Override
    public void addToCache(Format item) {
        CACHE.put(item.getId(), item);
    }

    @Override
    public Format getCachedVersion(Long id) {
        return CACHE.getIfPresent(id);
    }
}
