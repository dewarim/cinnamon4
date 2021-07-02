package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.Optional;

public class CreateMetaRequest implements ApiRequest {

    private Long   id;
    private String content;
    private Long   typeId;
    private String typeName;

    public CreateMetaRequest() {
    }

    public CreateMetaRequest(Long id, String content, Long typeId) {
        this.id = id;
        this.content = content;
        this.typeId = typeId;
    }

    public CreateMetaRequest(Long id, String content, String typeName) {
        this.id = id;
        this.content = content;
        this.typeName = typeName;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    private boolean validated() {
        return id != null && id > 0
                && ((typeId != null && typeId > 0) || (typeName != null && !typeName.trim().isEmpty()))
                && (content != null);
    }

    public Optional<CreateMetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "SetMetaRequest{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", typeId=" + typeId +
                '}';
    }
}
