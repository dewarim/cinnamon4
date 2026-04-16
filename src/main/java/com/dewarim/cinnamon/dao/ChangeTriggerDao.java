package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.ChangeTrigger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class ChangeTriggerDao implements CrudDao<ChangeTrigger>{

    private static final Logger log = LogManager.getLogger(ChangeTriggerDao.class);
    private static final Object CACHE_LOCK = new Object();
    private static volatile List<ChangeTrigger> cachedTriggers = List.of();
    private static volatile boolean cacheInitialized = false;

    @Override
    public String getTypeClassName() {
        return ChangeTrigger.class.getName();
    }

    public List<ChangeTrigger> listCached() {
        if (cacheInitialized) {
            return cachedTriggers;
        }
        synchronized (CACHE_LOCK) {
            if (!cacheInitialized) {
                cachedTriggers = List.copyOf(CrudDao.super.list());
                cacheInitialized = true;
                log.debug("Initialized changeTrigger cache with {} entries", cachedTriggers.size());
            }
        }
        return cachedTriggers;
    }

    public List<ChangeTrigger> findApplicableTriggers(UrlMapping mapping) {
        return listCached().stream()
                .filter(changeTrigger -> matchesMapping(changeTrigger, mapping) && changeTrigger.isActive())
                .toList();
    }

    public List<ChangeTrigger> findApplicablePostCommitTriggers(UrlMapping mapping) {
        return listCached().stream()
                .filter(changeTrigger -> matchesMapping(changeTrigger, mapping)
                        && changeTrigger.isPostCommitTrigger()
                        && changeTrigger.isActive())
                .toList();
    }

    private boolean matchesMapping(ChangeTrigger changeTrigger, UrlMapping mapping) {
        return mapping.getServlet().equals(changeTrigger.getController())
                && changeTrigger.getAction().equals(mapping.getAction());
    }

    public static void invalidateChangeTriggerCache() {
        synchronized (CACHE_LOCK) {
            cachedTriggers = List.of();
            cacheInitialized = false;
            log.debug("Invalidated changeTrigger cache");
        }
    }

    @Override
    public List<ChangeTrigger> create(List<ChangeTrigger> items) {
        List<ChangeTrigger> created = CrudDao.super.create(items);
        invalidateChangeTriggerCache();
        return created;
    }

    @Override
    public List<ChangeTrigger> update(List<ChangeTrigger> items) throws SQLException {
        List<ChangeTrigger> updated = CrudDao.super.update(items);
        invalidateChangeTriggerCache();
        return updated;
    }

    @Override
    public int delete(List<Long> ids) {
        int deleted = CrudDao.super.delete(ids);
        if (deleted > 0) {
            invalidateChangeTriggerCache();
        }
        return deleted;
    }
}
