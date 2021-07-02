package com.dewarim.cinnamon.model.request.relationType;


import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteRelationTypeRequest extends DeleteByIdRequest<RelationType> implements ApiRequest {

    public DeleteRelationTypeRequest() {
    }

    public DeleteRelationTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteRelationTypeRequest(Long id) {
        super(id);
    }
}
