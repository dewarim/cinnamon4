package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.model.CmnGroup;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListGroupRequest extends DefaultListRequest implements ListRequest<CmnGroup> {
    @Override
    public Wrapper<CmnGroup> fetchResponseWrapper() {
        return new GroupWrapper();
    }
}
