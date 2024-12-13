package com.dewarim.cinnamon.model.links;

import org.apache.ibatis.type.EnumTypeHandler;

public class LinkTypeEnumHandler extends EnumTypeHandler<LinkType> {
    public LinkTypeEnumHandler(Class<LinkType> type) {
        super(type);
    }
}
