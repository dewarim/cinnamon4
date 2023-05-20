package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.ChangeTrigger;

public class ChangeTriggerDao implements CrudDao<ChangeTrigger>{

    @Override
    public String getTypeClassName() {
        return ChangeTrigger.class.getName();
    }
}
