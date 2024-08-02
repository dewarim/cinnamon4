package com.dewarim.cinnamon.model.relations;

import com.dewarim.cinnamon.api.Identifiable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = RelationImpl.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, visible = false)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RelationImpl.class, name = "relation"),
        @JsonSubTypes.Type(value = ExtendedRelation.class, name = "extendedRelation")
})
public interface Relation extends Identifiable {
    void setId(Long id);

    Long getLeftId();

    void setLeftId(Long leftId);

    Long getRightId();

    void setRightId(Long rightId);

    Long getTypeId();

    void setTypeId(Long typeId);

    String getMetadata();

    void setMetadata(String metadata);
}
