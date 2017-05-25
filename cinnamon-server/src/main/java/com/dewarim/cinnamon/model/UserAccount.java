package com.dewarim.cinnamon.model;

import java.security.Principal;

/**
 */
public class UserAccount implements Principal {

    private String username;
    
    @Override
    public String getName() {
        return username;
    }
}
