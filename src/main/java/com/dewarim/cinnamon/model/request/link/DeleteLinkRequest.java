package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteLinkRequest")
public class DeleteLinkRequest extends DeleteByIdRequest<Link> implements ApiRequest<DeleteLinkRequest> {

    public DeleteLinkRequest() {
    }

    public DeleteLinkRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteLinkRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteLinkRequest>> examples() {
        return List.of(new DeleteLinkRequest(51L));
    }
}
