package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listRelationTypeRequest")
public class ListRelationTypeRequest extends DefaultListRequest implements ListRequest<RelationType>, ApiRequest<ListRelationTypeRequest> {

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListRelationTypeRequest>> examples() {
        return List.of(new ListRelationTypeRequest());
    }
}
