package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class FormatDao implements CrudDao<Format>{

    public Optional<Format> getFormatById(Long formatId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.Format.getFormatById", formatId));
    }

    @Override
    public String getTypeClassName() {
        return Format.class.getName();
    }
}
