package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateLinkRequest implements UpdateRequest<Link> {

    private List<Link> links = new ArrayList<>();

    public UpdateLinkRequest(List<Link> links) {
        this.links = links.stream().map(link -> {
            // during testing, we fetch LinkResponses, update the object and submit them again.
            // The extra OSD / Folder fields confuse the XmlMapper, so here we map LinkResponse to Link.
            if(link instanceof LinkResponse){
                return new Link((LinkResponse) link);
            }
            return link;
        }).collect(Collectors.toList());
    }

    public UpdateLinkRequest() {
    }

    public UpdateLinkRequest(long id, Long aclId, Long parentId, Long objectId, Long folderId, Long ownerId) {
        var type = objectId != null ? LinkType.OBJECT : LinkType.FOLDER;
        var link = new Link(id, type, ownerId, aclId, parentId, folderId, objectId);
        links.add(link);
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
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
        return links.size() > 0 &&  links.stream().allMatch(Link::validated);
    }

    @Override
    public String toString() {
        return "UpdateLinkRequest{" +
                "links=" + links +
                '}';
    }
}
