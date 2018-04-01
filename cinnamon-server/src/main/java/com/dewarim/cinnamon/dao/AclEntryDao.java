package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclEntry;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AclEntryDao {
    
    public List<AclEntry> getAclEntriesByGroupIdsAndAcl(List<Long> groupIds, long aclId){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String,Object> params = new HashMap<>();
        params.put("groupIds",groupIds);
        params.put("aclId",aclId);
        return sqlSession.selectList("com.dewarim.cinnamon.AclEntryMapper.getAclEntriesByGroupIdsAndAcl",params);
    }
    
}
