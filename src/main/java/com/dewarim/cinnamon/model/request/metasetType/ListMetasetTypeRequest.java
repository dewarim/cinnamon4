package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listMetasetTypeRequest")
public class ListMetasetTypeRequest extends DefaultListRequest implements ListRequest<MetasetType>, ApiRequest<ListMetasetTypeRequest> {
    @Override
    public Wrapper<MetasetType> fetchResponseWrapper() {
        return new MetasetTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListMetasetTypeRequest>> examples() {
        return List.of(new ListMetasetTypeRequest());
    }
}
