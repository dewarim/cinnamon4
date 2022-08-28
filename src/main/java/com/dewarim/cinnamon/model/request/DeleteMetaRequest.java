package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteMetaRequest")
public class DeleteMetaRequest extends DeleteByIdRequest<Meta> implements ApiRequest {

    public DeleteMetaRequest() {
    }

    public DeleteMetaRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteMetaRequest(Long id) {
        super(id);
    }
}
