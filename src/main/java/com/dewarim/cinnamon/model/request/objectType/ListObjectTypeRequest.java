package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listObjectTypeRequest")
public class ListObjectTypeRequest extends DefaultListRequest implements ListRequest<ObjectType>, ApiRequest<ListObjectTypeRequest> {

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListObjectTypeRequest>> examples() {
        return List.of(new ListObjectTypeRequest());
    }
}
