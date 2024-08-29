package com.dewarim.cinnamon.api;

import java.util.Date;

public interface OwnableWithMetadata extends Accessible, Identifiable, Ownable {

    boolean isMetadataChanged();

    void setMetadataChanged(boolean metadataChanged);

    void setModified(Date modified);

    void setModifierId(Long modifierId);
}
