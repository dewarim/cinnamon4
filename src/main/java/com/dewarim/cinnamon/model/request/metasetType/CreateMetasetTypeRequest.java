package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createMetasetTypeRequest")
public record CreateMetasetTypeRequest(
        @JacksonXmlElementWrapper(localName = "metasetTypes")
        @JacksonXmlProperty(localName = "metasetType")
        List<MetasetType> metasetTypes) implements CreateRequest<MetasetType>, ApiRequest<CreateMetasetTypeRequest> {

    public CreateMetasetTypeRequest {
        if (metasetTypes == null) {
            metasetTypes = new ArrayList<>();
        }
    }

    public CreateMetasetTypeRequest(String name, boolean unique) {
        this(new ArrayList<>(List.of(new MetasetType(name, unique))));
    }

    @Override
    public List<MetasetType> list() {
        return metasetTypes;
    }

    @Override
    public boolean validated() {
        return metasetTypes.stream()
                .noneMatch(type -> type == null || type.getName() == null ||
                        type.getName().trim().isEmpty() ||
                        type.getUnique() == null
                );
    }

    @Override
    public Wrapper<MetasetType> fetchResponseWrapper() {
        return new MetasetTypeWrapper();
    }

    @Override
    public List<ApiRequest<CreateMetasetTypeRequest>> examples() {
        return List.of(new CreateMetasetTypeRequest("tika", true));
    }
}
