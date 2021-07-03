package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.Optional;

public class MetaRequest implements ApiRequest {

    private Long         id;

    @JacksonXmlElementWrapper(localName = "typeNames")
    @JacksonXmlProperty(localName = "typeName")
    private List<String> typeNames;

    /**
     * Expects the XML content of each metaset to be an XML DOM element in the response XML,
     * not a
     */
    private boolean      version3CompatibilityRequired;

    public MetaRequest() {
    }

    public MetaRequest(Long id, List<String> typeNames) {
        this.id = id;
        this.typeNames = typeNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return id != null && id > 0 && (typeNames == null || typeNames.stream().noneMatch(name -> name == null || name.trim().isEmpty()));
    }

    public Optional<MetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Object> examples() {
        return List.of(new MetaRequest(null, List.of("copyright", "thumbnail")), new MetaRequest(1L, null));
    }
}
