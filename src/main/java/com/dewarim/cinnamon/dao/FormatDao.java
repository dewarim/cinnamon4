package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Format;

public class FormatDao implements CrudDao<Format>{

    @Override
    public String getTypeClassName() {
        return Format.class.getName();
    }
}
