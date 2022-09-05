package com.dewarim.cinnamon.dao;

import java.util.List;

public interface MetaDao {

    List<Long> getUniqueMetaTypeIdsOfObject(Long id);


}