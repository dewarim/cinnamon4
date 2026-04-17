package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.api.content.ContentMetadata;

import java.time.LocalDateTime;

/**
 * Interface class for ObjectSystemData
 */
public interface CinnamonObject extends ContentMetadata, Ownable {

    Long getCreatorId();

    LocalDateTime getCreated();

    Long getModifierId();

    LocalDateTime getModified();

    Long getLanguageId();

    Long getParentId();

    Long getFormatId();

    Long getTypeId();

    boolean isLatestHead();

    boolean isLatestBranch();

    boolean isContentChanged();

    boolean isMetadataChanged();

    Long getLifecycleStateId();

    String getSummary();

    String getContentProvider();

    String getCmnVersion();

    void setAclId(Long id);

}
