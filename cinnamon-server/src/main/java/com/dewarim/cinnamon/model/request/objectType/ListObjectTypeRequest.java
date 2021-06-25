package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListObjectTypeRequest extends DefaultListRequest implements ListRequest<ObjectType> {

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }
}
