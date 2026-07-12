package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateMetasetTypeRequest")
public record UpdateMetasetTypeRequest(
        @JacksonXmlElementWrapper(localName = "metasetTypes")
        @JacksonXmlProperty(localName = "metasetType")
        List<MetasetType> metasetTypes) implements UpdateRequest<MetasetType>, ApiRequest<UpdateMetasetTypeRequest> {

    public UpdateMetasetTypeRequest {
        if (metasetTypes == null) {
            metasetTypes = new ArrayList<>();
        }
    }

    public UpdateMetasetTypeRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new MetasetType(id, name))));
    }

    @Override
    public List<MetasetType> list() {
        return metasetTypes;
    }

    @Override
    public boolean validated() {
        return metasetTypes.stream().allMatch(metasetType ->
                metasetType != null && metasetType.getName() != null && !metasetType.getName().trim().isEmpty()
                        && metasetType.getId() != null && metasetType.getId() > 0);
    }

    @Override
    public Wrapper<MetasetType> fetchResponseWrapper() {
        return new MetasetTypeWrapper();
    }

    @Override
    public List<ApiRequest<UpdateMetasetTypeRequest>> examples() {
        return List.of(new UpdateMetasetTypeRequest(1L, "thumbnail"));
    }
}
