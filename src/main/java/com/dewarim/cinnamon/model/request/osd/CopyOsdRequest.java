package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "copyOsdRequest")
public class CopyOsdRequest implements ApiRequest<CopyOsdRequest> {

    @JacksonXmlElementWrapper(localName = "sourceIds")
    @JacksonXmlProperty(localName = "sourceId")
    private List<Long> sourceIds = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "metasetTypeIds")
    @JacksonXmlProperty(localName = "metasetTypeId")
    private List<Long> metasetTypeIds = new ArrayList<>();

    private Long targetFolderId;

    public CopyOsdRequest() {
    }

    public CopyOsdRequest(List<Long> sourceIds, Long targetFolderId, List<Long> metasetTypeIds) {
        this.sourceIds = sourceIds;
        this.targetFolderId = targetFolderId;
        this.metasetTypeIds = metasetTypeIds;
    }

    private boolean validated() {
        return sourceIds.size() > 0
                && sourceIds.stream().allMatch(id -> id != null && id > 0)
                && targetFolderId != null
                && targetFolderId > 0
                && (metasetTypeIds == null || metasetTypeIds.stream().allMatch(id -> id != null && id > 0));
    }

    public Optional<CopyOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public List<Long> getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(List<Long> sourceIds) {
        this.sourceIds = sourceIds;
    }

    public List<Long> getMetasetTypeIds() {
        if(metasetTypeIds == null){
            return Collections.emptyList();
        }
        return metasetTypeIds;
    }

    public void setMetasetTypeIds(List<Long> metasetTypeIds) {
        this.metasetTypeIds = metasetTypeIds;
    }

    public Long getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(Long targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    @Override
    public List<ApiRequest<CopyOsdRequest>> examples() {
        return List.of(new CopyOsdRequest(List.of(1L,2L,3L), 20L, List.of(13L, 15L, 2L)));
    }
}
