package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteLinkRequest extends DeleteByIdRequest<Link> {

    public DeleteLinkRequest() {
    }

    public DeleteLinkRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteLinkRequest(Long id) {
        super(id);
    }
}
