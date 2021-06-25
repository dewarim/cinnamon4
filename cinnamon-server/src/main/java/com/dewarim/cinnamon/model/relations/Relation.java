package com.dewarim.cinnamon.model.relations;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class Relation implements Identifiable {
    
    private Long id;
    private Long leftId;
    private Long rightId;
    private Long typeId;
    private String metadata;

    public Relation() {
    }

    public Relation(Long leftId, Long rightId, Long typeId, String metadata) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.typeId = typeId;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeftId() {
        return leftId;
    }

    public void setLeftId(Long leftId) {
        this.leftId = leftId;
    }

    public Long getRightId() {
        return rightId;
    }

    public void setRightId(Long rightId) {
        this.rightId = rightId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getMetadata() {
        return metadata;
    }

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
        Relation relation = (Relation) o;
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
