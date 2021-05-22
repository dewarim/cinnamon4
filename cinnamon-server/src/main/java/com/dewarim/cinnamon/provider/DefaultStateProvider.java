package com.dewarim.cinnamon.provider;

public enum DefaultStateProvider {

    CHANGE_ACL_STATE("ChangeAclState"),
    NOP_STATE("NopState")
    ;

    private final String className;

    DefaultStateProvider(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
