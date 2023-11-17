package com.dewarim.cinnamon.model.request.config;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listConfigRequest")
public class ListConfigRequest extends DefaultListRequest implements ApiRequest<ListConfigRequest> {
    @Override
    public List<ApiRequest<ListConfigRequest>> examples() {
        return List.of(new ListConfigRequest());
    }
}
