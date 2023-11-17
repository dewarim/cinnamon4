package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "deleteGroupRequest")
public class DeleteGroupRequest extends DeleteByIdRequest<Group> implements ApiRequest<DeleteGroupRequest> {

    boolean recursive;

    public DeleteGroupRequest() {
    }

    public DeleteGroupRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteGroupRequest(Long id) {
        super(id);
    }

    public DeleteGroupRequest(List<Long> ids, boolean recursive) {
        super(ids);
        this.recursive = recursive;
    }


    public Optional<DeleteGroupRequest> validate() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public List<ApiRequest<DeleteGroupRequest>> examples() {
        return List.of(new DeleteGroupRequest(List.of(4L,6L,7L),true));
    }
}
