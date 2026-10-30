package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class Meta implements Identifiable {

    // Request validation, checked when a request cascades into this class with @Valid.
    @NotNull(groups = UpdateRequest.class)
    @Positive(groups = UpdateRequest.class)
    private Long   id;
    // a createOsd request carries metasets whose object is only created with them, so the object
    // id is demanded by createMeta (which passes CreateRequest.class) and not by the Default group
    @NotNull(groups = CreateRequest.class)
    @Positive
    private Long   objectId;
    @NotNull
    @Positive
    private Long   typeId;
    @NotNull
    private String content;

    public Meta() {
    }

    public Meta(Long objectId, Long typeId, String content) {
        this.objectId = objectId;
        this.typeId = typeId;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Meta osdMeta = (Meta) o;
        return Objects.equals(objectId, osdMeta.objectId) &&
                Objects.equals(typeId, osdMeta.typeId) &&
                Objects.equals(content, osdMeta.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, typeId);
    }

    @Override
    public String toString() {
        return "Meta{" +
                "id=" + id +
                ", objectId=" + objectId +
                ", typeId=" + typeId +
                ", content='" + content + '\'' +
                '}';
    }
}
