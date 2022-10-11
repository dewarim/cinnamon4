package com.dewarim.cinnamon.model.index;

import org.apache.ibatis.type.EnumTypeHandler;

public class IndexJobActionEnumHandler extends EnumTypeHandler<IndexJobAction> {
    public IndexJobActionEnumHandler(Class<IndexJobAction> type) {
        super(type);
    }
}
