package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.CmnGroup;
import org.apache.ibatis.session.SqlSession;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CmnGroupDao {

    public CmnGroup getCmnGroupById(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.CmnGroupMapper.getCmnGroupById", id);
    }
    public List<CmnGroup> getCmnGroupsOfUserById(Long userId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.CmnGroupMapper.getCmnGroupsOfUserById", userId);
    }
    
    // TODO: cache results
    public Set<CmnGroup> getGroupsWithAncestorsOfUserById(Long userId){
        Set<CmnGroup> groups = new HashSet<>(getCmnGroupsOfUserById(userId));
        groups.forEach(group -> {
            Long  parentId = group.getParentId();
            if(parentId != null){
                findAncestors(groups, parentId);
            }
        });
        return groups;
    }

    private void findAncestors(Set<CmnGroup> groups, Long parentId){
        if(parentId == null){
            return;
        }
        CmnGroupDao dao = new CmnGroupDao();
        CmnGroup parent = dao.getCmnGroupById(parentId);
        if(parent != null && groups.add(parent)){
            findAncestors(groups, parent.getParentId());
        }
    }
}