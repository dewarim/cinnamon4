package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListIndexItemRequest extends DefaultListRequest implements ListRequest<IndexItem> {

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }
}
