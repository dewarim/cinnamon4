package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonRootName("updateLinkRequest")
public record UpdateLinkRequest(
        @JacksonXmlElementWrapper(localName = "links")
        @JacksonXmlProperty(localName = "link")
        List<Link> links) implements UpdateRequest<Link>, ApiRequest<UpdateLinkRequest> {

    public UpdateLinkRequest {
        if (links == null) {
            links = new ArrayList<>();
        } else {
            // during testing, we fetch LinkResponses, update the object and submit them again.
            // The extra OSD / Folder fields confuse the XmlMapper, so here we map LinkResponse to Link.
            links = links.stream().map(link ->
                    link instanceof LinkResponse ? new Link((LinkResponse) link) : link
            ).collect(Collectors.toList());
        }
    }

    public UpdateLinkRequest() {
        this(new ArrayList<>());
    }

    public UpdateLinkRequest(long id, Long aclId, Long parentId, Long objectId, Long folderId, Long ownerId) {
        this(new ArrayList<>(List.of(new Link(id, objectId != null ? LinkType.OBJECT : LinkType.FOLDER,
                ownerId, aclId, parentId, folderId, objectId, LinkResolver.FIXED))));
    }

    @Override
    public List<Link> list() {
        return links;
    }

    @Override
    public Wrapper<Link> fetchResponseWrapper() {
        return new LinkWrapper();
    }

    public boolean validated() {
        return !links.isEmpty() && links.stream().allMatch(Link::validated);
    }

    @Override
    public List<ApiRequest<UpdateLinkRequest>> examples() {
        return List.of(new UpdateLinkRequest(List.of(new Link(1L, LinkType.OBJECT, 2L, 3L, 4L, 5L, 6L, LinkResolver.FIXED))));
    }
}
