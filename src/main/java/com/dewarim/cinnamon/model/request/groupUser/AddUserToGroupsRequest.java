package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "addUserToGroupsRequest")
public class AddUserToGroupsRequest implements ApiRequest {
    private List<Long> ids;

    public AddUserToGroupsRequest() {
    }

    public AddUserToGroupsRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    private boolean validated(){
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id > 0);
    }

    public Optional<AddUserToGroupsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
