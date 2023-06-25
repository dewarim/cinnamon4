package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AclGroupDao implements CrudDao<AclGroup> {

    public List<AclGroup> getAclGroupsByGroupIdsAndAcl(List<Long> groupIds, long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = Map.of("groupIds", groupIds, "aclId", aclId);
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByGroupIdsAndAcl", params);
    }

    public List<AclGroup> getAclGroupsByAclId(long aclId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<AclGroup> aclGroups= sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByAclId", aclId);
        addPermissionsToAclGroups(aclGroups);
        return aclGroups;
    }
    public List<AclGroup> getAclGroupsByGroupId(long groupId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<AclGroup> aclGroups  = sqlSession.selectList("com.dewarim.cinnamon.model.AclGroup.getAclGroupsByGroupId", groupId);
        addPermissionsToAclGroups(aclGroups);
        return aclGroups;
    }

    public void addPermissionsToAclGroups(List<AclGroup> aclGroups) {
        AclGroupPermissionDao agpDao = new AclGroupPermissionDao();
        Map<Long, List<Long>> aclGroupToPermissionIds = agpDao.listPermissionsOfAclGroups(aclGroups.stream().map(AclGroup::getId).collect(Collectors.toList()));
        aclGroups.forEach(aclGroup -> aclGroup.setPermissionIds(aclGroupToPermissionIds.getOrDefault(aclGroup.getId(),new ArrayList<>())));
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
        Map<String, Object> params = Map.of("groupName", name, "aclId", aclId);
        AclGroup aclGroup = sqlSession.selectOne("com.dewarim.cinnamon.model.AclGroup.getAclGroupByAclAndGroupName", params);
        if(aclGroup != null){
            addPermissionsToAclGroups(List.of(aclGroup));
        }
        return Optional.ofNullable(aclGroup);
    }

    @Override
    public String getTypeClassName() {
        return AclGroup.class.getName();
    }

    public void deleteByGroupIds(List<Long> ids) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.AclGroup.deleteByGroupIds", ids);
    }
}
