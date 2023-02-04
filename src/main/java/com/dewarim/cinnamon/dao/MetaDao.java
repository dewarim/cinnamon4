package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Meta;

import java.util.List;
import java.util.Optional;

public interface MetaDao {

    List<Long> getUniqueMetaTypeIdsOfObject(Long id);

    List<Meta> listMetaByObjectIds(List<Long> objectIds);

    Optional<Meta> getMetaById(Long id);
}