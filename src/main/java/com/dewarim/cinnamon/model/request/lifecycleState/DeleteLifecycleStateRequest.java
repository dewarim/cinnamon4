package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteLifecycleStateRequest")
public class DeleteLifecycleStateRequest extends DeleteByIdRequest<LifecycleState> implements ApiRequest {

    public DeleteLifecycleStateRequest() {
    }

    public DeleteLifecycleStateRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteLifecycleStateRequest(Long id) {
        super(id);
    }
}
