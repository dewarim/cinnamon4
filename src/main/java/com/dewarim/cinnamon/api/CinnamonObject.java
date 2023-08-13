package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.api.content.ContentMetadata;

import java.util.Date;

/**
 * Interface class for ObjectSystemData
 */
public interface CinnamonObject extends ContentMetadata, Ownable {

    Long getCreatorId();

    Date getCreated();

    Long getModifierId();

    Date getModified();

    Long getLanguageId();

    Long getParentId();

    Long getFormatId();

    Long getTypeId();

    boolean isLatestHead();

    boolean isLatestBranch();

    Boolean isContentChanged();

    Boolean isMetadataChanged();

    Long getLifecycleStateId();

    String getSummary();

    String getContentProvider();

    String getCmnVersion();

    void setAclId(Long id);

}
