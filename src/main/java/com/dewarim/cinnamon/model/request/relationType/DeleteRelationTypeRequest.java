package com.dewarim.cinnamon.model.request.relationType;


import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteRelationTypeRequest")
public class DeleteRelationTypeRequest extends DeleteByIdRequest<RelationType> implements ApiRequest<DeleteRelationTypeRequest> {

    public DeleteRelationTypeRequest() {
    }

    public DeleteRelationTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteRelationTypeRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteRelationTypeRequest>> examples() {
        return List.of(new DeleteRelationTypeRequest(List.of(333L,543L)));
    }
}
