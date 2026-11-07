package com.dewarim.cinnamon.dao.event;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class AccessLogDao {
    private static final Logger log = LogManager.getLogger(AccessLogDao.class);

    public List<AccessLogEntry> list() {
        try (SqlSession sqlSession = getSqlSession()) {
            return sqlSession.selectList("com.dewarim.cinnamon.model.event.AccessLogEntry.list");
        }
    }

    /**
     * The newest entries first, at most limit of them. What the admin endpoint reads: the unlimited
     * {@link #list()} above would render every logged request body and error response into one
     * response document.
     */
    public List<AccessLogEntry> list(int limit) {
        try (SqlSession sqlSession = getSqlSession()) {
            return sqlSession.selectList("com.dewarim.cinnamon.model.event.AccessLogEntry.listNewest",
                    Map.of("limit", limit));
        }
    }

    public AccessLogEntry insert(AccessLogEntry accessLogEntry) {
        try (SqlSession sqlSession = getSqlSession()) {
            log.debug("about to insert {}", accessLogEntry);
            int rows = sqlSession.insert("com.dewarim.cinnamon.model.event.AccessLogEntry.insert", accessLogEntry);
            log.debug("about to commit access log entry");
            sqlSession.commit();
            log.debug("Inserted {} access log entries.", rows);
            return accessLogEntry;
        } catch (Exception e) {
            log.error("fail: ", e);
            throw new CinnamonException("Failed to save event log entry:", e);
        }
    }

    private SqlSession getSqlSession() {
        return CinnamonServer.getAccessLogSession();
    }

    public int count() {
        try (SqlSession sqlSession = getSqlSession()) {
            return sqlSession.selectOne("com.dewarim.cinnamon.model.event.AccessLogEntry.count");
        }
    }

    public void truncate(int numberOfRowsToDelete) {
        try (SqlSession sqlSession = getSqlSession()) {
            sqlSession.delete("com.dewarim.cinnamon.model.event.AccessLogEntry.truncate", numberOfRowsToDelete);
            sqlSession.commit();
        }
    }
}
