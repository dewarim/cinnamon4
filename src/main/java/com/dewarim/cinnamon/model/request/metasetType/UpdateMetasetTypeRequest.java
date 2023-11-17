package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateMetasetTypeRequest")
public class UpdateMetasetTypeRequest implements UpdateRequest<MetasetType>, ApiRequest<UpdateMetasetTypeRequest> {

    @JacksonXmlElementWrapper(localName = "metasetTypes")
    @JacksonXmlProperty(localName = "metasetType")
    private List<MetasetType> metasetTypes = new ArrayList<>();

    @Override
    public List<MetasetType> list() {
        return metasetTypes;
    }

    public UpdateMetasetTypeRequest() {
    }

    public UpdateMetasetTypeRequest(Long id, String name) {
        metasetTypes.add(new MetasetType(id,name));
    }

    public UpdateMetasetTypeRequest(List<MetasetType> MetasetTypes) {
        this.metasetTypes = MetasetTypes;
    }

    public List<MetasetType> getMetasetTypes() {
        return metasetTypes;
    }

    @Override
    public boolean validated() {
        return metasetTypes.stream().allMatch(MetasetType ->
            MetasetType != null && MetasetType.getName() != null && !MetasetType.getName().trim().isEmpty()
                    && MetasetType.getId() != null && MetasetType.getId() > 0);
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
