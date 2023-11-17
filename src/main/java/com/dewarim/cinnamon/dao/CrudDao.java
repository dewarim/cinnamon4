package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dewarim.cinnamon.dao.SqlAction.*;

public interface CrudDao<T extends Identifiable> {

    int BATCH_SIZE = 1000;

    default List<T> create(List<T> items) {
        List<T>    createdItems = new ArrayList<>();
        SqlSession sqlSession   = getSqlSession();
        items.forEach(item -> {
            String sqlAction = getMapperNamespace(INSERT);
            try {
                sqlSession.insert(sqlAction, item);
            } catch (PersistenceException e) {
                throw new FailedRequestException(ErrorCode.DB_INSERT_FAILED, e);
            }
            createdItems.add(item);
        });
        return createdItems;
    }

    default int delete(List<Long> ids) {
        if (ids.isEmpty()) {
            // upstream code may filter the list of ids to 0.
            return 0;
        }
        SqlSession       sqlSession  = getSqlSession();
        List<List<Long>> partitions  = partitionLongList(ids);
        AtomicInteger    deleteCount = new AtomicInteger(0);
        partitions.forEach(partition -> {
            try {
                deleteCount.getAndAdd(sqlSession.delete(getMapperNamespace(DELETE), partition));
            } catch (PersistenceException e) {
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
            }
        });
        return deleteCount.get();
    }

    default List<T> list() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList(getMapperNamespace(LIST));
    }

    default List<T> getObjectsById(List<Long> ids) {
        List<List<Long>> partitions = partitionLongList(ids);
        SqlSession       sqlSession = getSqlSession();
        List<T>          results    = new ArrayList<>(ids.size());
        partitions.forEach(partition -> {
            if (!partition.isEmpty()) {
                results.addAll(sqlSession.selectList(getMapperNamespace(GET_ALL_BY_ID), partition));
            }
        });
        return results;
    }

    default Optional<T> getObjectById(Long id) {
        if(id == null){
            return Optional.empty();
        }
        List<T> items = getObjectsById(List.of(id));
        if (items.size() == 0) {
            return Optional.empty();
        } else {
            // since ids are unique primary keys, we do not have to check for size() > 1.
            return Optional.of(items.get(0));
        }
    }

    default List<List<T>> partitionList(List<T> items) {
        List<List<T>> partitions  = new ArrayList<>(items.size() / BATCH_SIZE);
        int           requestSize = items.size();
        int           rowCount    = 0;
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if (lastIndex > requestSize) {
                lastIndex = requestSize;
            }
            List<T> partialList = items.subList(rowCount, lastIndex);
            partitions.add(partialList);
            rowCount += BATCH_SIZE;
        }
        return partitions;
    }

    String getTypeClassName();

    default String getMapperNamespace(SqlAction action) {
        String name = getTypeClassName();
        return switch (action) {
            case INSERT -> name + INSERT.getSuffix();
            case DELETE -> name + DELETE.getSuffix();
            case GET_ALL_BY_ID -> name + GET_ALL_BY_ID.getSuffix();
            case LIST -> name + LIST.getSuffix();
            case UPDATE -> name + UPDATE.getSuffix();
        };
    }

    /**
     *  partition test code in ConfigEntryServletIntegrationTest.createAndDeleteLotsOfObjects()
     */
    static List<List<Long>> partitionLongList(List<Long> ids) {
        if (ids.size() < BATCH_SIZE) {
            return List.of(ids);
        }
        List<List<Long>> partitions  = new ArrayList<>(ids.size() / BATCH_SIZE);
        int              requestSize = ids.size();
        int              rowCount    = 0;
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if (lastIndex > requestSize) {
                lastIndex = requestSize;
            }
            List<Long> partialList = ids.subList(rowCount, lastIndex);
            partitions.add(partialList);
            rowCount += BATCH_SIZE;
        }
        return partitions;
    }

    default List<T> update(List<T> items) throws SQLException {
        List<T>    updatedItems = new ArrayList<>();
        SqlSession sqlSession   = getSqlSession();
        items.forEach(item -> {
            String sqlAction = getMapperNamespace(UPDATE);
            // some more effort to check if an object does exist, so we can
            // return a proper error message.
            if (verifyExistence()) {
                T existing = sqlSession.selectOne(getMapperNamespace(GET_ALL_BY_ID), Collections.singletonList(item.getId()));
                if (existing == null) {
                    throw new FailedRequestException(ErrorCode.OBJECT_NOT_FOUND, "Object with id " + item.getId() + " was not found in the database.");
                }
            }
            try {
                int updatedRows = sqlSession.update(sqlAction, item);
                if (updatedRows == 0) {
                    if (ignoreNopUpdates()) {
                        throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, "update failed on item " + item.getId());
                    }
                }
            } catch (PersistenceException e) {
                throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e);
            }
            updatedItems.add(item);
        });
        return updatedItems;
    }

    /**
     * Check if all objects from a list of ids actually exist.
     */
    default boolean verifyAllObjectsFromSetExist(List<Long> ids) {
        Set<Long> idSet = new HashSet<>(ids);
        return getObjectsById(ids).size() == idSet.size();
    }

    default boolean verifyExistence() {
        return CinnamonServer.config.getServerConfig().isVerifyExistence();
    }

    default boolean ignoreNopUpdates() {
        return CinnamonServer.config.getServerConfig().isIgnoreNopUpdates();
    }

    default SqlSession getSqlSession() {
        return ThreadLocalSqlSession.getSqlSession();
    }
}
