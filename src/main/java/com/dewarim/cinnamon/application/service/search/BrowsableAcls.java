package com.dewarim.cinnamon.application.service.search;

import java.util.Set;

public record BrowsableAcls(Set<Long> objectAcls, Set<Long> ownerAcls, Long userId) {

    public boolean hasBrowsePermission(Long aclId, Long owner){
        return objectAcls.contains(aclId) || (owner.equals(userId) && ownerAcls.contains(aclId));
    }

}
