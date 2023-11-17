package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteAclRequest")
public class DeleteAclRequest extends DeleteByIdRequest<Acl> implements ApiRequest<DeleteAclRequest> {

    public DeleteAclRequest() {
    }

    public DeleteAclRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteAclRequest>> examples() {
        return List.of(new DeleteAclRequest(List.of(43L, 99L)));
    }
}
