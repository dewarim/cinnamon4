package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Deletion;

public class DeletionDao implements CrudDao<Deletion>{

    @Override
    public String getTypeClassName() {
        return Deletion.class.getName();
    }

}
