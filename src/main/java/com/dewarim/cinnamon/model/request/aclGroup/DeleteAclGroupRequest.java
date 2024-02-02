package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteAclGroupRequest")
public class DeleteAclGroupRequest extends DeleteByIdRequest<AclGroup> implements ApiRequest<DeleteAclGroupRequest> {

    public DeleteAclGroupRequest() {
    }

    public DeleteAclGroupRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclGroupRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteAclGroupRequest>> examples() {
        return List.of(new DeleteAclGroupRequest(List.of(5L,78L)));
    }
}
