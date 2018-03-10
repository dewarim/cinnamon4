package com.dewarim.cinnamon.api.login;

import java.util.List;

public interface LoginResult {

    List<GroupMapping> getGroupMappings();

    boolean isValidUser();

    String getErrorMessage();

}
