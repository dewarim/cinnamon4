package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.AclGroupPermission;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

public class AclGroupPermissionDao {

    private SqlSession sqlSession;

    public AclGroupPermissionDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public AclGroupPermissionDao() {
    }

    public boolean checkIfAnyAclGroupHasThisPermission(List<AclGroup> aclGroups, Permission permission) {
        if (aclGroups.isEmpty()) {
            return false;
        }
        return fetchAepCombinationOfAclGroupsAndThisPermission(aclGroups, permission).size() > 0;
    }

    public List<AclGroup> filterAclGroupsWithoutThisPermission(List<AclGroup> aclGroups, Permission permission) {
        if (aclGroups.isEmpty()) {
            return Collections.emptyList();
        }
        List<AclGroupPermission> aclGroupPermissions = fetchAepCombinationOfAclGroupsAndThisPermission(aclGroups, permission);
        Set<Long>                allowedAclGroupIds  = aclGroupPermissions.stream().map(AclGroupPermission::getAclGroupId).collect(Collectors.toSet());
        return aclGroups.stream().filter(aclGroup -> allowedAclGroupIds.contains(aclGroup.getId())).collect(Collectors.toList());
    }

    private List<AclGroupPermission> fetchAepCombinationOfAclGroupsAndThisPermission(List<AclGroup> aclGroups, Permission permission) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("aclGroups", aclGroups, "permissionId", permission.getId());
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroupPermission.getAclGroupPermissionForAclGroupsAndPermission", params);
    }

    public void addPermissions(AclGroup aclGroup, List<Long> permissionIdsToAdd) {
        SqlSession               sqlSession           = getSqlSession();
        List<AclGroupPermission> aclGroupPermissions  = sqlSession.selectList("com.dewarim.cinnamon.model.AclGroupPermission.listByAclGroupIds", List.of(aclGroup.getId()));
        Set<Long>                currentPermissionIds = aclGroupPermissions.stream().map(AclGroupPermission::getPermissionId).collect(Collectors.toSet());
        List<Long>               newPermissions       = permissionIdsToAdd.stream().filter(id -> !currentPermissionIds.contains(id)).toList();
        newPermissions.forEach(permissionId -> {
            AclGroupPermission aclGroupPermission = new AclGroupPermission(aclGroup.getId(), permissionId);
            sqlSession.insert("com.dewarim.cinnamon.model.AclGroupPermission.insert", aclGroupPermission);
        });
    }

    public void removePermissions(AclGroup aclGroup, List<Long> permissionIdsToRemove) {
        SqlSession               sqlSession          = getSqlSession();
        List<AclGroupPermission> aclGroupPermissions = sqlSession.selectList("com.dewarim.cinnamon.model.AclGroupPermission.listByAclGroupIds", List.of(aclGroup.getId()));
        aclGroupPermissions.stream().filter(agp -> permissionIdsToRemove.contains(agp.getPermissionId()))
                .forEach(agp -> sqlSession.delete("com.dewarim.cinnamon.model.AclGroupPermission.delete", agp.getId()));
    }

    public Map<Long, List<Long>> listPermissionsOfAclGroups(List<Long> aclGroupIds) {
        if(aclGroupIds.isEmpty()){
            return Collections.emptyMap();
        }
        SqlSession               sqlSession                = getSqlSession();
        Map<Long, List<Long>>    aclGroupIdToPermissionIds = new HashMap<>();
        List<AclGroupPermission> agpList                   = sqlSession.selectList("com.dewarim.cinnamon.model.AclGroupPermission.listByAclGroupIds", aclGroupIds);
        agpList.forEach(agp -> {
            List<Long> permissionIds = aclGroupIdToPermissionIds.getOrDefault(agp.getAclGroupId(), new ArrayList<>());
            permissionIds.add(agp.getPermissionId());
            aclGroupIdToPermissionIds.put(agp.getAclGroupId(), permissionIds);
        });
        return aclGroupIdToPermissionIds;
    }

    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }
}
