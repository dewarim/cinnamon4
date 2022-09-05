package com.dewarim.cinnamon.model;

import java.util.Arrays;
import java.util.List;

/**
 * Default login providers. Server administrators may add LoginProvider with other login types,
 * so this enum is not a comprehensive list.
 */
public enum LoginType {

    CINNAMON, LDAP;

    private static final List<String> names = Arrays.stream(values()).map(Enum::toString).toList();

    public static boolean isKnown(String name){
        return names.contains(name);
    }
}
