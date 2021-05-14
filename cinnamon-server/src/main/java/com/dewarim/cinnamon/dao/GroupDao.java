package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Group;
import org.apache.ibatis.session.SqlSession;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupDao implements CrudDao<Group> {

    @Override
    public String getTypeClassName() {
        return Group.class.getName();
    }

    public Group getGroupById(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.Group.getGroupById", id);
    }
    public List<Group> getGroupsOfUserById(Long userId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.Group.getGroupsOfUserById", userId);
    }
    
    // TODO: cache results
    public Set<Group> getGroupsWithAncestorsOfUserById(Long userId){
        Set<Group> groups = new HashSet<>(getGroupsOfUserById(userId));
        groups.forEach(group -> {
            Long  parentId = group.getParentId();
            if(parentId != null){
                findAncestors(groups, parentId);
            }
        });
        return groups;
    }
    
    public Group getOwnerGroup(){
        Optional<Group> ownerGroup = getGroupByName(Constants.ALIAS_OWNER);
        if(ownerGroup.isPresent()){
            return ownerGroup.get();
        }
        throw new IllegalStateException("Could not find essential system group '_owner' in database."); 
    }
    
    public Optional<Group> getGroupByName(String name){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.Group.getGroupByName",name));
    }
    
    private void findAncestors(Set<Group> groups, Long parentId){
        if(parentId == null){
            return;
        }
        GroupDao dao    = new GroupDao();
        Group    parent = dao.getGroupById(parentId);
        if(parent != null && groups.add(parent)){
            findAncestors(groups, parent.getParentId());
        }
    }

}
