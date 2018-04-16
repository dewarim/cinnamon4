package com.dewarim.cinnamon.security.authorization;

import com.dewarim.cinnamon.model.Link;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        BrowseAcls browseAcls = BrowseAcls.getInstance(user);
        return osds.stream().filter(browseAcls::hasBrowsePermissionForOsd).collect(Collectors.toList());
    }
    
    public List<Link> filterLinksByBrowsePermission(List<Link> links, UserAccount user) {
        BrowseAcls browseAcls = BrowseAcls.getInstance(user);
        return links.stream().filter(browseAcls::hasBrowsePermissionForLink).collect(Collectors.toList());
    }

}
