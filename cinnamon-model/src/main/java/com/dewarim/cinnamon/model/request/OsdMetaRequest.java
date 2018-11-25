package com.dewarim.cinnamon.model.request;

import java.util.List;
import java.util.Optional;

public class OsdMetaRequest {

    private Long         osdId;
    private List<String> typeNames;
    /**
     * Expects the XML content of each metaset to be an XML DOM element in the response XML,
     * not a
     */
    private boolean      version3CompatibilityRequired;

    public OsdMetaRequest() {
    }

    public OsdMetaRequest(Long osdId, List<String> typeNames) {
        this.osdId = osdId;
        this.typeNames = typeNames;
    }

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public List<String> getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(List<String> typeNames) {
        this.typeNames = typeNames;
    }

    public boolean isVersion3CompatibilityRequired() {
        return version3CompatibilityRequired;
    }

    public void setVersion3CompatibilityRequired(boolean version3CompatibilityRequired) {
        this.version3CompatibilityRequired = version3CompatibilityRequired;
    }

    private boolean validated(){
        return osdId != null && osdId > 0 && (typeNames == null || typeNames.stream().noneMatch(name -> name == null || name.trim().isEmpty()));
    }

    public Optional<OsdMetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
