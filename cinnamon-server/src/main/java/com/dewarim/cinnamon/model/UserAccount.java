package com.dewarim.cinnamon.model;

import java.security.Principal;

/**
 */
public class UserAccount implements Principal {
    
    private Long id;
    private String name;
    
    @Override
    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
