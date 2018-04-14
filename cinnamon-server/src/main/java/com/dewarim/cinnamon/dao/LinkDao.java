package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Link;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class LinkDao {


    public Optional<Link> getLinkById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.LinkMapper.getLinkById", id));
    }


}
