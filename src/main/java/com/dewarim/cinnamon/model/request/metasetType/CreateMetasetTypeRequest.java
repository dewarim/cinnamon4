package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createMetasetTypeRequest")
public class CreateMetasetTypeRequest implements CreateRequest<MetasetType>, ApiRequest<CreateMetasetTypeRequest> {

    @JacksonXmlElementWrapper(localName = "metasetTypes")
    @JacksonXmlProperty(localName = "metasetType")
    private List<MetasetType> metasetTypes = new ArrayList<>();

    @Override
    public List<MetasetType> list() {
        return metasetTypes;
    }

    public CreateMetasetTypeRequest() {
    }

    public CreateMetasetTypeRequest(String name, boolean unique) {
        metasetTypes.add(new MetasetType(name, unique));
    }

    public CreateMetasetTypeRequest(List<MetasetType> metasetTypes) {
        this.metasetTypes = metasetTypes;
    }

    public List<MetasetType> getMetasetTypes() {
        return metasetTypes;
    }

    public void setMetasetTypes(List<MetasetType> metasetTypes) {
        this.metasetTypes = metasetTypes;
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
