package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.CmnGroup;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AclEntryDao {

    public List<AclEntry> getAclEntriesByGroupIdsAndAcl(List<Long> groupIds, long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("groupIds", groupIds);
        params.put("aclId", aclId);
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclEntryMapper.getAclEntriesByGroupIdsAndAcl", params);
    }

    public List<AclEntry> getAclEntriesByAclId(long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclEntryMapper.getAclEntriesByAclId", aclId);
    }
    public List<AclEntry> getAclEntriesByGroupId(long groupId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclEntryMapper.getAclEntriesByGroupId", groupId);
    }
    
    public List<AclEntry> getAclEntriesByGroup(CmnGroup group) {
        return getAclEntriesByGroupId(group.getId());
    }
    
    public Optional<AclEntry> getAclEntryForEveryoneGroup(long aclId) {
        return getAclEntryByAclAndGroupName(Constants.ALIAS_EVERYONE, aclId);
    }

    public Optional<AclEntry> getAclEntryForOwnerGroup(long aclId) {
        return getAclEntryByAclAndGroupName(Constants.ALIAS_OWNER, aclId);
    }

    private Optional<AclEntry> getAclEntryByAclAndGroupName(String name, long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("groupName", name);
        params.put("aclId", aclId);
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.AclEntryMapper.getAclEntryByAclAndGroupName", params));
    }


}
