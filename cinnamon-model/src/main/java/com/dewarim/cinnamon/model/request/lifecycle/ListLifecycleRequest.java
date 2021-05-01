package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListLifecycleRequest extends DefaultListRequest implements ListRequest<Lifecycle> {

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }
}
