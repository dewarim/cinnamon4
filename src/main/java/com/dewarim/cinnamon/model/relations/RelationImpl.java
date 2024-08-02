package com.dewarim.cinnamon.model.relations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "relation")
public class RelationImpl implements Relation {
    
    private Long id;
    private Long leftId;
    private Long rightId;
    private Long typeId;
    private String metadata;

    public RelationImpl() {
    }

    public RelationImpl(Long leftId, Long rightId, Long typeId, String metadata) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.typeId = typeId;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationImpl relation = (RelationImpl) o;
        return Objects.equals(leftId, relation.leftId) &&
               Objects.equals(rightId, relation.rightId) &&
               Objects.equals(typeId, relation.typeId) &&
               Objects.equals(metadata, relation.metadata);
    }

    @Override
    public int hashCode() {

        return Objects.hash(leftId, rightId, typeId);
    }

    @Override
    public String toString() {
        return "Relation{" +
               "id=" + id +
               ", leftId=" + leftId +
               ", rightId=" + rightId +
               ", typeId=" + typeId +
               ", metadata='" + metadata + '\'' +
               '}';
    }
}
