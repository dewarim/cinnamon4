package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listIndexItemRequest")
public class ListIndexItemRequest extends DefaultListRequest implements ListRequest<IndexItem>, ApiRequest<ListIndexItemRequest> {

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }

    @Override
    public List<ApiRequest<ListIndexItemRequest>> examples() {
        return List.of(new ListIndexItemRequest());
    }
}
