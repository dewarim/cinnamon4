package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LinkWrapper extends BaseResponse implements Wrapper<Link>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "links")
    @JacksonXmlProperty(localName = "link")
    private List<Link> links = new ArrayList<>();

    public LinkWrapper() {
    }

    public LinkWrapper(List<Link> links) {
        this.links = links;
    }

    @Override
    public List<Link> list() {
        return links;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @Override
    public Wrapper<Link> setList(List<Link> links) {
        setLinks(links);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new LinkWrapper(
                List.of(
                        new Link(1L, LinkType.OBJECT, 2L, 3L, 4L, null, 123L),
                        new Link(1L, LinkType.FOLDER, 2L, 3L, 4L, 321L, null)
                )
        ));
    }
}
