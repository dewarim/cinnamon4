package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteAclRequest")
public class DeleteAclRequest extends DeleteByIdRequest<Acl> implements ApiRequest {

    public DeleteAclRequest() {
    }

    public DeleteAclRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclRequest(Long id) {
        super(id);
    }
}
