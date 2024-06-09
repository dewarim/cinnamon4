package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.provider.state.NopStateProvider;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LifecycleWrapper extends BaseResponse implements Wrapper<Lifecycle>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "lifecycles")
    @JacksonXmlProperty(localName = "lifecycle")
    List<Lifecycle> lifecycles = new ArrayList<>();

    public LifecycleWrapper() {
    }

    public LifecycleWrapper(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    public List<Lifecycle> getLifecycles() {
        return lifecycles;
    }

    public void setLifecycles(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    @Override
    public List<Lifecycle> list() {
        return getLifecycles();
    }

    @Override
    public Wrapper<Lifecycle> setList(List<Lifecycle> lifecycles) {
        setLifecycles(lifecycles);
        return this;
    }

    @Override
    public List<Object> examples() {
        LifecycleWrapper wrapper   = new LifecycleWrapper();
        Lifecycle        lifecycle = new Lifecycle("review-lifecycle", 1L);
        LifecycleState   state1    = new LifecycleState("needs-edits", "<config/>", NopStateProvider.class.getName(), 1L, 1L);
        LifecycleState   state2    = new LifecycleState("needs-review", "<config/>", NopStateProvider.class.getName(), 1L, 2L);
        LifecycleState   state3    = new LifecycleState("published", "<config/>", NopStateProvider.class.getName(), 1L, 3L);
        state1.setId(1L);
        state2.setId(2L);
        state3.setId(3L);
        lifecycle.setLifecycleStates(List.of(state1, state2, state3));
        wrapper.getLifecycles().add(lifecycle);
        return List.of(wrapper);
    }
}
