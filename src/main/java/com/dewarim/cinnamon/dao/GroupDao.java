package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Group;
import org.apache.ibatis.session.SqlSession;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GroupDao implements CrudDao<Group> {

    @Override
    public String getTypeClassName() {
        return Group.class.getName();
    }

    // TODO: cache results
    public Set<Group> getGroupsWithAncestorsOfUserById(Long userId){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return new HashSet<>(sqlSession.selectList("com.dewarim.cinnamon.model.Group.getGroupsWithAncestorsOfUserById", userId));
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

}
