package com.dewarim.cinnamon.model.index;

import org.apache.ibatis.type.EnumTypeHandler;

public class IndexTypeEnumHandler extends EnumTypeHandler<IndexType> {
    public IndexTypeEnumHandler(Class<IndexType> type) {
        super(type);
    }
}
