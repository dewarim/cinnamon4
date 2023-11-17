package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteIndexItemRequest")
public class DeleteIndexItemRequest extends DeleteByIdRequest<IndexItem> implements ApiRequest<DeleteIndexItemRequest> {

    public DeleteIndexItemRequest() {
    }

    public DeleteIndexItemRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteIndexItemRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteIndexItemRequest>> examples() {
        return List.of(new DeleteIndexItemRequest(679L));
    }
}
