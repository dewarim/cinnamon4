package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Language;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LanguageDao implements CrudDao<Language> {

    private static final Cache<Long, Language> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    public String getTypeClassName() {
        return Language.class.getName();
    }

    public Optional<Language> getLanguageByIsoCode(String isoCode) {
        SqlSession sqlSession = getSqlSession();
        Language   Language   = sqlSession.selectOne("com.dewarim.cinnamon.model.Language.getLanguageByIsoCode", isoCode);
        return Optional.ofNullable(Language);
    }

    @Override
    public boolean useCache() {
        return true;
    }

    @Override
    public void addToCache(Language item) {
        CACHE.put(item.getId(), item);
    }

    @Override
    public Language getCachedVersion(Long id) {
        return CACHE.getIfPresent(id);
    }
}
