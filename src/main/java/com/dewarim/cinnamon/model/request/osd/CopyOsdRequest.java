package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("copyOsdRequest")
public record CopyOsdRequest(
        @JacksonXmlElementWrapper(localName = "sourceIds")
        @JacksonXmlProperty(localName = "sourceId")
        List<Long> sourceIds,
        Long targetFolderId,
        @JacksonXmlElementWrapper(localName = "metasetTypeIds")
        @JacksonXmlProperty(localName = "metasetTypeId")
        List<Long> metasetTypeIds) implements ApiRequest<CopyOsdRequest> {

    public CopyOsdRequest {
        if (sourceIds == null) {
            sourceIds = new ArrayList<>();
        }
        if (metasetTypeIds == null) {
            metasetTypeIds = new ArrayList<>();
        }
    }

    private boolean validated() {
        return !sourceIds.isEmpty()
                && sourceIds.stream().allMatch(id -> id != null && id > 0)
                && targetFolderId != null
                && targetFolderId > 0
                && metasetTypeIds.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<CopyOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<CopyOsdRequest>> examples() {
        return List.of(new CopyOsdRequest(List.of(1L, 2L, 3L), 20L, List.of(13L, 15L, 2L)));
    }
}
