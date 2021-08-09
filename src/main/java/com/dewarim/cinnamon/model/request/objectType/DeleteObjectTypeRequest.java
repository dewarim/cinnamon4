package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteObjectTypeRequest")
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
