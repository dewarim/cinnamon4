package com.dewarim.cinnamon.model.request;


public class DefaultListRequest {

    private ListType type = ListType.FULL;

    public ListType getType() {
        return type;
    }

    public void setType(ListType type) {
        this.type = type;
    }
}
