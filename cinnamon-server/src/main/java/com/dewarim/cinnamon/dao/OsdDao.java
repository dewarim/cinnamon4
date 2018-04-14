package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsdDao {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    public List<ObjectSystemData> getObjectsById(List<Long> ids, boolean includeSummary) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<ObjectSystemData> results = new ArrayList<>(ids.size());
        int requestSize = ids.size();
        int rowCount = 0;
        Map<String,Object> params = new HashMap<>();
        params.put("includeSummary", includeSummary);
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if(lastIndex > requestSize){
                lastIndex = requestSize;
            }
            List<Long> partialList = ids.subList(rowCount, lastIndex);
            params.put("idList",partialList);
            results.addAll(sqlSession.selectList("com.dewarim.cinnamon.ObjectSystemDataMapper.getOsdsById", params));
            rowCount += BATCH_SIZE;
        }
        return results;
    }

}