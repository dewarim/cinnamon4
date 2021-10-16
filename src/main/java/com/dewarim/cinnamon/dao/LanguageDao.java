package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Language;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class LanguageDao implements CrudDao<Language> {

    @Override
    public String getTypeClassName() {
        return Language.class.getName();
    }

    public Optional<Language> getLanguageById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Language   Language   = sqlSession.selectOne("com.dewarim.cinnamon.model.Language.getLanguageById", id);
        return Optional.ofNullable(Language);
    }

    public Optional<Language> getLanguageByIsoCode(String isoCode) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Language   Language   = sqlSession.selectOne("com.dewarim.cinnamon.model.Language.getLanguageByIsoCode", isoCode);
        return Optional.ofNullable(Language);
    }


}
