package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.model.relations.RelationType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class RelationTypeWrapper implements Wrapper<RelationType>{

    @JacksonXmlElementWrapper(localName = "relationTypes")
    @JacksonXmlProperty(localName = "relationType")
    List<RelationType> relationTypes = new ArrayList<>();

    public List<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    @Override
    public List<RelationType> list() {
        return getRelationTypes();
    }

    @Override
    public Wrapper<RelationType> setList(List<RelationType> relationTypes) {
        this.setRelationTypes(relationTypes);
        return this;
    }
}
