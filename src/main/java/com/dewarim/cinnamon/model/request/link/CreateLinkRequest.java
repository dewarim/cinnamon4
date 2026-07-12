package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createLinkRequest")
public record CreateLinkRequest(
        @JacksonXmlElementWrapper(localName = "links")
        @JacksonXmlProperty(localName = "link")
        List<Link> links) implements CreateRequest<Link>, ApiRequest<CreateLinkRequest> {

    public CreateLinkRequest {
        if (links == null) {
            links = new ArrayList<>();
        }
    }

    public CreateLinkRequest(long parentId, LinkType linkType, long aclId, long ownerId, Long folderId, Long objectId, LinkResolver resolver) {
        this(new ArrayList<>(List.of(new Link(null, linkType, ownerId, aclId, parentId, folderId, objectId, resolver))));
    }

    public boolean validated() {
        return links.stream().allMatch(Link::validated);
    }

    @Override
    public List<Link> list() {
        return links;
    }

    @Override
    public Wrapper<Link> fetchResponseWrapper() {
        return new LinkWrapper();
    }

    @Override
    public List<ApiRequest<CreateLinkRequest>> examples() {
        return List.of(
                new CreateLinkRequest(5L, LinkType.OBJECT, 1L, 1L, 2L, 3L, LinkResolver.FIXED),
                new CreateLinkRequest(5L, LinkType.FOLDER, 1L, 1L, 2L, 3L, LinkResolver.LATEST_HEAD)
        );
    }
}
