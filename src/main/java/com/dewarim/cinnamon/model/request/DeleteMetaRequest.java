package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "deleteMetaRequest")
public class DeleteMetaRequest implements ApiRequest {

    private Long   id;
    private Long   metaId;
    private String typeName;

    public DeleteMetaRequest() {
    }

    public DeleteMetaRequest(Long id, Long metaId) {
        this.id = id;
        this.metaId = metaId;
    }

    public DeleteMetaRequest(Long id, String typeName) {
        this.id = id;
        this.typeName = typeName;
    }

    public Long getId() {
        return id;
    }

    public Long getMetaId() {
        return metaId;
    }

    public String getTypeName() {
        return typeName;
    }

    private boolean validated() {
        return id != null && id > 0 && ((metaId != null && metaId > 0) || typeName != null && !typeName.trim().isEmpty());
    }

    public Optional<DeleteMetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
