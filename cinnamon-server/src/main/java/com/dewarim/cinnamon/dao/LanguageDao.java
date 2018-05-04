package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Language;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LanguageDao {

    public List<Language> listLanguages() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.LanguageMapper.list");
    }

    public Optional<Language> getLanguageById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        Language Language = sqlSession.selectOne("com.dewarim.cinnamon.LanguageMapper.getLanguageById", id);
        return Optional.ofNullable(Language);
    }


}
