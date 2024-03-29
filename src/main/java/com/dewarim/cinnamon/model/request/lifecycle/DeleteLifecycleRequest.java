package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteLifecycleRequest")
public class DeleteLifecycleRequest extends DeleteByIdRequest<Lifecycle> implements ApiRequest<DeleteLifecycleRequest> {

    public DeleteLifecycleRequest() {
    }

    public DeleteLifecycleRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteLifecycleRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteLifecycleRequest>> examples() {
        return List.of(new DeleteLifecycleRequest(1024L));
    }
}
