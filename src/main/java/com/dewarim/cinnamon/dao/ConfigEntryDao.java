package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.ConfigEntry;

public class ConfigEntryDao implements CrudDao<ConfigEntry> {

    @Override
    public String getTypeClassName() {
        return ConfigEntry.class.getName();
    }

}
