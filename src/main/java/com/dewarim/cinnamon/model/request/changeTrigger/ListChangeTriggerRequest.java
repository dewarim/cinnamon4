package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listChangeTriggerRequest")
public class ListChangeTriggerRequest extends DefaultListRequest implements ListRequest<ChangeTrigger>, ApiRequest<ListChangeTriggerRequest> {

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
