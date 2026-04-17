package com.dewarim.cinnamon.api;

import java.time.LocalDateTime;

public interface OwnableWithMetadata extends Accessible, Identifiable, Ownable {

    boolean isMetadataChanged();

    void setMetadataChanged(boolean metadataChanged);

    void setModified(LocalDateTime modified);

    void setModifierId(Long modifierId);
}
