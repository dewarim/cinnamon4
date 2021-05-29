package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LinkResponseWrapper implements Wrapper<LinkResponse> {

    @JacksonXmlElementWrapper(localName = "links")
    @JacksonXmlProperty(localName = "link")
    List<LinkResponse> links = new ArrayList<>();

    public LinkResponseWrapper() {
    }

    public LinkResponseWrapper(LinkResponse linkResponse) {
        links.add(linkResponse);
    }

    public LinkResponseWrapper(List<LinkResponse> links) {
        this.links = links;
    }

    public List<LinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<LinkResponse> links) {
        this.links = links;
    }

    @Override
    public List<LinkResponse> list() {
        return links;
    }

    @Override
    public Wrapper<LinkResponse> setList(List<LinkResponse> linkResponses) {
        setLinks(linkResponses);
        return this;
    }
}
