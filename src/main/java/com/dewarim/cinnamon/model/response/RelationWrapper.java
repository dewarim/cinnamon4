package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationImpl;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class RelationWrapper extends BaseResponse implements Wrapper<Relation>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "relations")
    @JacksonXmlProperty(localName = "relation")
    List<Relation> relations = new ArrayList<>();

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public RelationWrapper() {
    }

    public RelationWrapper(List<Relation> relations) {
        this.relations = relations;
    }

    @Override
    public List<Relation> list() {
        return relations;
    }

    @Override
    public Wrapper<Relation> setList(List<Relation> relations) {
        setRelations(relations);
        return this;
    }

    @Override
    public List<Object> examples() {
        Relation relation = new RelationImpl(1L, 4L, 1L, "<generatedBy>PDF Renderer</generatedBy");
        relation.setId(399L);
        return List.of(new RelationWrapper(List.of(relation)));
    }
}
