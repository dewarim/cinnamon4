package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListMetasetTypeRequest extends DefaultListRequest implements ListRequest<MetasetType> {
    @Override
    public Wrapper<MetasetType> fetchResponseWrapper() {
        return new MetasetTypeWrapper();
    }
}
