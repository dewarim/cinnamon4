package com.dewarim.cinnamon.model.relations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "relation")
public class ExtendedRelation implements Relation {

    private boolean parent;
    private Long    id;
    private Long    leftId;
    private Long    rightId;
    private Long    typeId;
    private String  metadata;

    public ExtendedRelation() {
    }

    public ExtendedRelation(Long leftId, Long rightId, Long typeId, String metadata, boolean parent) {
        this.leftId   = leftId;
        this.rightId  = rightId;
        this.typeId   = typeId;
        this.metadata = metadata;
    }

    @JsonProperty("isChild")
    public boolean isChild(){
        return !parent;
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("isParent")
    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getLeftId() {
        return leftId;
    }

    @Override
    public void setLeftId(Long leftId) {
        this.leftId = leftId;
    }

    @Override
    public Long getRightId() {
        return rightId;
    }

    @Override
    public void setRightId(Long rightId) {
        this.rightId = rightId;
    }

    @Override
    public Long getTypeId() {
        return typeId;
    }

    @Override
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    @Override
    public String getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedRelation that = (ExtendedRelation) o;
        return Objects.equals(id, that.id) && Objects.equals(leftId, that.leftId) && Objects.equals(rightId, that.rightId) && Objects.equals(typeId, that.typeId) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftId, rightId, typeId);
    }

    @Override
    public String toString() {
        return "ExtendedRelation{" +
                "isParent=" + parent +
                ", isChild=" + isChild()+
                ", id=" + id +
                ", leftId=" + leftId +
                ", rightId=" + rightId +
                ", typeId=" + typeId +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
