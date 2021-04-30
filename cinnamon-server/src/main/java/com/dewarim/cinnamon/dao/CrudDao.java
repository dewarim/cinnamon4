package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import org.apache.ibatis.session.SqlSession;
import org.postgresql.util.PSQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dewarim.cinnamon.dao.SqlAction.*;

public interface CrudDao<T> {

    int BATCH_SIZE = 1000;

    default List<T> create(List<T> items) {
        List<T>    createdItems = new ArrayList<>();
        SqlSession sqlSession   = ThreadLocalSqlSession.getSqlSession();
        items.forEach(item -> {
            String sqlAction = getMapperNamespace(INSERT);
            sqlSession.insert(sqlAction, item);
            createdItems.add(item);
        });
        return createdItems;
    }

    default int delete(List<Long> ids) throws PSQLException {
        SqlSession       sqlSession  = ThreadLocalSqlSession.getSqlSession();
        List<List<Long>> partitions  = partitionLongList(ids);
        AtomicInteger    deleteCount = new AtomicInteger(0);
        partitions.forEach(partition -> {
            deleteCount.getAndAdd(sqlSession.delete(getMapperNamespace(DELETE), partition));
        });
        return deleteCount.get();
    }

    default List<T> list() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList(getMapperNamespace(LIST));
    }

    default List<T> getObjectsById(List<Long> ids) {
        List<List<Long>> partitions = partitionLongList(ids);
        SqlSession       sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<T>          results    = new ArrayList<>(ids.size());
        partitions.forEach(partition -> {
            results.addAll(sqlSession.selectList(getMapperNamespace(GET_ALL_BY_ID), partition));
        });
        return results;
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
        switch (action) {
            case INSERT:
                return name + INSERT.getSuffix();
            case DELETE:
                return name + DELETE.getSuffix();
            case LIST:
                return name + LIST.getSuffix();
            default:
                throw new IllegalArgumentException("Unmapped SqlAction " + action);
        }
    }

    static List<List<Long>> partitionLongList(List<Long> ids) {
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
}
