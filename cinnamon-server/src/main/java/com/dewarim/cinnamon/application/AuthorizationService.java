package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.authorization.BrowseAcls;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        BrowseAcls browseAcls = BrowseAcls.getInstance(user);
        return osds.stream().filter(browseAcls::hasBrowsePermissionForOsd).collect(Collectors.toList());
    }

}
