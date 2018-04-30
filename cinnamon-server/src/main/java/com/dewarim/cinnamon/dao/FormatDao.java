package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class FormatDao {
    
    public List<Format> listFormats(){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.FormatMapper.list");
    }
    
}
