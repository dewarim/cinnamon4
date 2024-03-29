package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listConfigEntryRequest")
public class ListConfigEntryRequest extends DefaultListRequest implements ListRequest<ConfigEntry>, ApiRequest<ListConfigEntryRequest> {

    @Override
    public Wrapper<ConfigEntry> fetchResponseWrapper() {
        return new ConfigEntryWrapper();
    }

    @Override
    public List<ApiRequest<ListConfigEntryRequest>> examples() {
        return List.of(new ListConfigEntryRequest());
    }
}
