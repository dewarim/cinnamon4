package com.dewarim.cinnamon.model.request.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChangePermissionsRequest {

    private Long aclGroupId;
    private List<Long> add = new ArrayList<>();
    private List<Long> remove = new ArrayList<>();

    public ChangePermissionsRequest() {
    }

    public ChangePermissionsRequest(Long aclGroupId, List<Long> add, List<Long> remove) {
        this.aclGroupId = aclGroupId;
        this.add = add;
        this.remove = remove;
    }

    public boolean validated() {
        if(add == null && remove == null){
            return false;
        }
        if (Objects.requireNonNullElseGet(add, () -> remove).isEmpty()) {
            return false;
        }
        return aclGroupId != null && aclGroupId > 0;
    }

    public List<Long> getAdd() {
        if(add == null){
            add = new ArrayList<>();
        }
        return add;
    }

    public List<Long> getRemove() {
        if(remove == null){
            remove = new ArrayList<>();
        }
        return remove;
    }

    public Long getAclGroupId() {
        return aclGroupId;
    }

    public Optional<ChangePermissionsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
