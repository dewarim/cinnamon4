package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LinkWrapper implements Wrapper<Link>, ApiResponse {

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
}
