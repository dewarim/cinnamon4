package com.dewarim.cinnamon.model.index;

import org.apache.ibatis.type.EnumTypeHandler;

public class IndexJobTypeEnumHandler extends EnumTypeHandler<IndexJobType> {
    public IndexJobTypeEnumHandler(Class<IndexJobType> type) {
        super(type);
    }
}
