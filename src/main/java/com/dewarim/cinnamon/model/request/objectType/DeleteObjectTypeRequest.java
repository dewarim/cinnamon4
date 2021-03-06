package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteObjectTypeRequest extends DeleteByIdRequest<ObjectType> implements ApiRequest {

    public DeleteObjectTypeRequest() {
    }

    public DeleteObjectTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteObjectTypeRequest(Long id) {
        super(id);
    }
}
