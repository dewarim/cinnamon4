package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListGroupRequest extends DefaultListRequest implements ListRequest<Group> {
    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }
}
