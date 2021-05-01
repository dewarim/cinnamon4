package com.dewarim.cinnamon.dao;

public enum SqlAction {

    DELETE(".delete"),
    GET_ALL_BY_ID(".getAllById"),
    INSERT(".insert"),
    LIST(".list"),
    UPDATE(".update")
    ;


    private String suffix;

    SqlAction(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
