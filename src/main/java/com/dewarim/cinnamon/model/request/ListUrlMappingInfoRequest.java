package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UrlMappingInfo;
import com.dewarim.cinnamon.model.response.UrlMappingInfoWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listUrlMappingInfoRequest")

public class ListUrlMappingInfoRequest  extends DefaultListRequest implements ListRequest<UrlMappingInfo>, ApiRequest<ListUrlMappingInfoRequest> {
    @Override
    public Wrapper<UrlMappingInfo> fetchResponseWrapper() {
        return new UrlMappingInfoWrapper();
    }

    @Override
    public List<ApiRequest<ListUrlMappingInfoRequest>> examples() {
        return List.of(new ListUrlMappingInfoRequest());
    }
}
