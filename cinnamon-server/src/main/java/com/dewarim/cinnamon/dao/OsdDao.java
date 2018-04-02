package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public class OsdDao {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    public List<ObjectSystemData> getObjectsById(List<Long> ids) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<ObjectSystemData> results = new ArrayList<>(ids.size());
        int requestSize = ids.size();
        int rowCount = 0;
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if(lastIndex > requestSize){
                lastIndex = requestSize;
            }
            List<Long> partialList = ids.subList(rowCount, lastIndex);
            results.addAll(sqlSession.selectList("com.dewarim.cinnamon.ObjectSystemDataMapper.getOsdsById", partialList));
            rowCount += BATCH_SIZE;
        }
        return results;
    }

}
