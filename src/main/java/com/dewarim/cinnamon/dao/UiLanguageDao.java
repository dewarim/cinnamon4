package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.UiLanguage;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;


public class UiLanguageDao implements CrudDao<UiLanguage> {

    @Override
    public String getTypeClassName() {
        return UiLanguage.class.getName();
    }

    public Optional<UiLanguage> findByIsoCode(String language) {
        SqlSession sqlSession = getSqlSession();
        UiLanguage uiLanguage   = sqlSession.selectOne("com.dewarim.cinnamon.model.UiLanguage.getLanguageByIsoCode", language);
        return Optional.ofNullable(uiLanguage);
    }
}
