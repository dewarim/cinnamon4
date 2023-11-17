package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listLifecycleStateRequest")
public class ListLifecycleStateRequest extends DefaultListRequest implements ListRequest<LifecycleState>, ApiRequest<ListLifecycleStateRequest> {

    @Override
    public Wrapper<LifecycleState> fetchResponseWrapper() {
        return new LifecycleStateWrapper();
    }

    @Override
    public List<ApiRequest<ListLifecycleStateRequest>> examples() {
        return List.of(new ListLifecycleStateRequest());
    }
}
