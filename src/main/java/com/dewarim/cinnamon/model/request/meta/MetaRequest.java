package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "metaRequest")
public class MetaRequest implements ApiRequest<MetaRequest> {

    private Long         id;

    @JacksonXmlElementWrapper(localName = "typeIds")
    @JacksonXmlProperty(localName = "typeId")
    private List<Long> typeIds;
    public MetaRequest() {
    }

    public MetaRequest(Long id, List<Long> typeIds) {
        this.id = id;
        this.typeIds = typeIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(List<Long> typeIds) {
        this.typeIds = typeIds;
    }

    private boolean validated(){
        return id != null && id > 0 && (typeIds == null || typeIds.stream().noneMatch(typeId -> typeId == null || typeId < 0));
    }

    public Optional<MetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<MetaRequest>> examples() {
        return List.of(new MetaRequest(3L, List.of(12L, 13L)), new MetaRequest(1L, null));
    }
}
