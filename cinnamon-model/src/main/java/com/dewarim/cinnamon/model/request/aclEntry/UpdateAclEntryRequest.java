package com.dewarim.cinnamon.model.request.aclEntry;

import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class UpdateAclEntryRequest implements UpdateRequest<AclEntry> {

    private List<AclEntry> aclEntries = new ArrayList<>();

    @Override
    public List<AclEntry> list() {
        return aclEntries;
    }

    public UpdateAclEntryRequest() {
    }

    public UpdateAclEntryRequest(Long id, Long aclId, Long groupId) {
        aclEntries.add(new AclEntry(id, aclId, groupId));
    }

    public UpdateAclEntryRequest(List<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }

    public List<AclEntry> getAclEntries() {
        return aclEntries;
    }

    @Override
    public boolean validated() {
        return aclEntries.stream().noneMatch(entry ->
                isNull(entry) || isNull(entry.getAclId()) || isNull(entry.getGroupId())
                        || isNull(entry.getId())
                        || entry.getAclId() < 1 || entry.getGroupId() < 1 || entry.getId() < 1);
    }

    @Override
    public Wrapper<AclEntry> fetchResponseWrapper() {
        return new AclEntryWrapper();
    }

    @Override
    public String toString() {
        return "UpdateAclEntryRequest{" +
                "aclEntries=" + aclEntries +
                '}';
    }
}
