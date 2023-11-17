package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listFormatRequest")
public class ListFormatRequest extends DefaultListRequest implements ListRequest<Format>, ApiRequest<ListFormatRequest> {
    @Override
    public Wrapper<Format> fetchResponseWrapper() {
        return new FormatWrapper();
    }

    @Override
    public List<ApiRequest<ListFormatRequest>> examples() {
        return List.of(new ListFormatRequest());
    }
}
