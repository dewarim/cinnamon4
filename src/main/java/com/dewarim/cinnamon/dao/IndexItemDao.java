package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.IndexItem;

public class IndexItemDao implements CrudDao<IndexItem>{

    @Override
    public String getTypeClassName() {
        return IndexItem.class.getName();
    }
}
