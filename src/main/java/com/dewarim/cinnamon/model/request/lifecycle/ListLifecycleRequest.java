package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listLifecycleRequest")
public class ListLifecycleRequest extends DefaultListRequest implements ListRequest<Lifecycle>, ApiRequest<ListLifecycleRequest> {

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }

    @Override
    public List<ApiRequest<ListLifecycleRequest>> examples() {
        return List.of(new ListLifecycleRequest());
    }
}
