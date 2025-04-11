package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.ObjectType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class ObjectTypeDao implements CrudDao<ObjectType>{

    private static final Cache<Long, ObjectType> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    public String getTypeClassName() {
        return ObjectType.class.getName();
    }

    @Override
    public boolean useCache() {
        return true;
    }

    @Override
    public void addToCache(ObjectType item) {
        CACHE.put(item.getId(), item);
    }

    @Override
    public ObjectType getCachedVersion(Long id) {
        return CACHE.getIfPresent(id);
    }
}
