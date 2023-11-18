package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.FOLDER_EXAMPLE;
import static com.dewarim.cinnamon.api.Constants.OSD_EXAMPLE;

@JacksonXmlRootElement(localName = "cinnamon")
public class LinkResponseWrapper implements Wrapper<LinkResponse>, ApiResponse {

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

    @Override
    public List<Object> examples() {
        LinkResponse osdLink = new LinkResponse();
        osdLink.setOsd(OSD_EXAMPLE);
        LinkResponse folderLink = new LinkResponse();
        folderLink.setFolder(FOLDER_EXAMPLE);
        return List.of(new LinkResponseWrapper(List.of(osdLink, folderLink)));
    }
}
