package com.dewarim.cinnamon.model.request.aclEntry;

import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class CreateAclEntryRequest implements CreateRequest<AclEntry> {

    private List<AclEntry> aclEntries = new ArrayList<>();

    @Override
    public List<AclEntry> list() {
        return aclEntries;
    }

    public CreateAclEntryRequest() {
    }

    public CreateAclEntryRequest(List<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }

    public List<AclEntry> getAclEntries() {
        return aclEntries;
    }

    @Override
    public boolean validated() {
        return aclEntries.stream().noneMatch(entry ->
             isNull(entry) || isNull(entry.getAclId()) || isNull(entry.getGroupId())
                    || entry.getAclId() < 1 || entry.getGroupId() < 1);
    }

    @Override
    public Wrapper<AclEntry> fetchResponseWrapper() {
        return new AclEntryWrapper();
    }
}
