package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Meta;

import java.util.List;

public interface MetaDao {

    List<Long> getUniqueMetaTypeIdsOfObject(Long id);

    List<Meta> listByObjectIds(List<Long> objectIds);
}