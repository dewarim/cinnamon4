package com.dewarim.cinnamon.model.request.permission;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JacksonXmlRootElement(localName = "changePermissionsRequest")
public class ChangePermissionsRequest implements ApiRequest<ChangePermissionsRequest> {

    private Long aclGroupId;

    @JacksonXmlElementWrapper(localName = "addPermissions")
    @JacksonXmlProperty(localName = "addId")
    private List<Long> add = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "removePermissions")
    @JacksonXmlProperty(localName = "removeId")
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

    @Override
    public List<ApiRequest<ChangePermissionsRequest>> examples() {
        return List.of(new ChangePermissionsRequest(3L, List.of(4L,5L,6L),List.of(7L,8L,9L)));
    }

}
