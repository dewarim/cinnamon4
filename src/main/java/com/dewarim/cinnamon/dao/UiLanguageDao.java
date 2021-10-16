package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.UiLanguage;


public class UiLanguageDao implements CrudDao<UiLanguage> {

    @Override
    public String getTypeClassName() {
        return UiLanguage.class.getName();
    }
}
