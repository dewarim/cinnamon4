package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listChangeTriggerRequest")
public record ListChangeTriggerRequest(ListType type) implements DefaultListRequest, ListRequest<ChangeTrigger>, ApiRequest<ListChangeTriggerRequest> {

    public ListChangeTriggerRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListChangeTriggerRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<ChangeTrigger> fetchResponseWrapper() {
        return new ChangeTriggerWrapper();
    }

    @Override
    public List<ApiRequest<ListChangeTriggerRequest>> examples() {
        ListChangeTriggerRequest request = new ListChangeTriggerRequest();
        return List.of(request);
    }
}
