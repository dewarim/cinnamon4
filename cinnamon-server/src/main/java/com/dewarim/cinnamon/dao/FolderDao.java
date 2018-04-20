package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Folder;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

public class FolderDao {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    // note: same code as OsdDao, although you could argue that fetching > 10K folders is a pathological case. 
    public List<Folder> getFoldersById(List<Long> ids, boolean includeSummary) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<Folder> results = new ArrayList<>(ids.size());
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
            results.addAll(sqlSession.selectList("com.dewarim.cinnamon.FolderMapper.getFoldersById", params));
            rowCount += BATCH_SIZE;
        }
        return results;
    }

    
    public Folder getFolderById(long id){
        return getFoldersById(Collections.singletonList(id),false).get(0);
    }

}
