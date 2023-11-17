package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteRelationRequest")
public class DeleteRelationRequest  extends DeleteByIdRequest<Relation>  implements ApiRequest<DeleteRelationRequest> {

    public DeleteRelationRequest() {
    }

    public DeleteRelationRequest(Long id) {
        super(id);
    }

    public DeleteRelationRequest(List<Long> ids) {
        super(ids);
    }

    @Override
    public List<ApiRequest<DeleteRelationRequest>> examples() {
        return List.of(new DeleteRelationRequest(68L));
    }
}
