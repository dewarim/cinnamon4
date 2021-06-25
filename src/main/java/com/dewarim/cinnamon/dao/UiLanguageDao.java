package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.UiLanguage;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class UiLanguageDao {

    public List<UiLanguage> listUiLanguages() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.UiLanguageMapper.list");
    }

    public Optional<UiLanguage> getUiLanguageById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        UiLanguage uiLanguage = sqlSession.selectOne("com.dewarim.cinnamon.UiLanguageMapper.getUiLanguageById", id);
        return Optional.ofNullable(uiLanguage);
    }


}
