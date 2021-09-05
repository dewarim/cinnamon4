package com.dewarim.cinnamon.model.request.osd;

/**
 * Depending on the VersionPredicate, when the user requests the content of a folder,
 * the server returns either the newest object version, all versions
 * or the newest branch versions.
 */
public enum VersionPredicate {

    /**
     * Fetch all objects where LATEST_HEAD=true.
     */
    HEAD,

    /**
     * Fetch all objects.
     */
    ALL,

    /**
     * Fetch all objects where LATEST_BRANCH=true.
     */
    BRANCH

}
