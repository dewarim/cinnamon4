package com.dewarim.cinnamon.security.authorization;

import com.dewarim.cinnamon.model.Link;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return osds.stream().filter(accessFilter::hasBrowsePermissionForOsd).collect(Collectors.toList());
    }

    public List<Link> filterLinksByBrowsePermission(List<Link> links, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return links.stream().filter(accessFilter::hasBrowsePermissionForLink).collect(Collectors.toList());
    }

    public boolean userHasPermission(Long aclId, String permissionName, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return accessFilter.hasPermission(aclId, permissionName);
    }
}
