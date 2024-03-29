package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteMetaRequest")
public class DeleteMetaRequest extends DeleteByIdRequest<Meta> implements ApiRequest<DeleteMetaRequest> {

    public DeleteMetaRequest() {
    }

    public DeleteMetaRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteMetaRequest(Long id) {
        super(id);
    }

    public DeleteMetaRequest(Long id, boolean ignoreNotFound) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteMetaRequest>> examples() {
        return List.of(new DeleteMetaRequest(List.of(3L, 5L, 6L)), new DeleteMetaRequest(1L,true));
    }
}
