package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Set;

@JacksonXmlRootElement(localName = "deleteAllMetasRequest")
public class DeleteAllMetasRequest extends DeleteByIdRequest<Meta> implements ApiRequest<DeleteAllMetasRequest> {

    public DeleteAllMetasRequest() {
    }

    public DeleteAllMetasRequest(Set<Long> ids) {
        super(ids);
    }

    public DeleteAllMetasRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteAllMetasRequest>> examples() {
        return List.of(new DeleteAllMetasRequest(Set.of(14L,15L)));
    }
}
