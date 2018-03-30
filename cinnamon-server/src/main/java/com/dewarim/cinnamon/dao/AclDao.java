package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Acl;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class AclDao {

    public Acl getAclById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.AclMapper.getAclById", id);
    }

    public Acl getAclByName(String name) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.AclMapper.getAclByName", name);
    }

    public Acl save(Acl acl) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.AclMapper.insertAcl", acl);
        return acl;
    }

    public int changeAclName(Acl acl) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.update("com.dewarim.cinnamon.AclMapper.changeAclName", acl);
    }

    public int deleteAcl(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.AclMapper.deleteAcl", id);
    }
    
    public List<Acl> list(){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.AclMapper.listAcls");
    }

}
