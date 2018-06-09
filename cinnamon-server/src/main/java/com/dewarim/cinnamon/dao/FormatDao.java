package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class FormatDao {
    
    public List<Format> listFormats(){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.FormatMapper.list");
    }

    public Optional<Format> getFormatById(Long formatId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.FormatMapper.getFormatById", formatId));
    }
}
