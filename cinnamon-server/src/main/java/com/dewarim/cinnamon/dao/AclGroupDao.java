package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AclGroupDao implements CrudDao<AclGroup> {

    public List<AclGroup> getAclGroupsByGroupIdsAndAcl(List<Long> groupIds, long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("groupIds", groupIds);
        params.put("aclId", aclId);
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByGroupIdsAndAcl", params);
    }

    public List<AclGroup> getAclGroupsByAclId(long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByAclId", aclId);
    }
    public List<AclGroup> getAclGroupsByGroupId(long groupId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByGroupId", groupId);
    }
    
    public List<AclGroup> getAclGroupsByGroup(Group group) {
        return getAclGroupsByGroupId(group.getId());
    }
    
    public Optional<AclGroup> getAclGroupForEveryoneGroup(long aclId) {
        return getAclGroupByAclAndGroupName(Constants.ALIAS_EVERYONE, aclId);
    }

    public Optional<AclGroup> getAclGroupForOwnerGroup(long aclId) {
        return getAclGroupByAclAndGroupName(Constants.ALIAS_OWNER, aclId);
    }

    private Optional<AclGroup> getAclGroupByAclAndGroupName(String name, long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("groupName", name);
        params.put("aclId", aclId);
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.AclGroup.getAclGroupByAclAndGroupName", params));
    }

    @Override
    public String getTypeClassName() {
        return AclGroup.class.getName();
    }
}
