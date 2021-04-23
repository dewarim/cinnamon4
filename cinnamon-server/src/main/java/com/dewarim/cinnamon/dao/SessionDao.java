package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Session;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class SessionDao {

    public Session getSessionByTicket(String ticket) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.SessionMapper.getSessionByTicket", ticket);
    }

    public Session save(Session session) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.SessionMapper.insertSession", session);
        return session;
    }

    public void update(Session session) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.SessionMapper.updateSession", session);
    }

    public void delete(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.SessionMapper.deleteSession", id);
    }

    public List<Session> list(){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.SessionMapper.listSessions");
    }
}
