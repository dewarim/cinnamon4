package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class OsdWrapper implements Wrapper<ObjectSystemData>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "osds")
    @JacksonXmlProperty(localName = "osd")
    private List<ObjectSystemData> osds = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "links")
    @JacksonXmlProperty(localName = "link")
    private List<Link> links = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "references")
    @JacksonXmlProperty(localName = "reference")
    private List<ObjectSystemData> references = new ArrayList<>();

    public OsdWrapper() {
    }

    public OsdWrapper(List<ObjectSystemData> osds) {
        this.osds = osds;
    }

    public List<ObjectSystemData> getOsds() {
        return osds;
    }

    public void setOsds(List<ObjectSystemData> osds) {
        this.osds = osds;
    }

    public List<Link> getLinks() {
        if(links == null){
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<ObjectSystemData> getReferences() {
        if(references == null){
            references = new ArrayList<>();
        }
        return references;
    }

    public void setReferences(List<ObjectSystemData> references) {
        this.references = references;
    }

    @Override
    public List<ObjectSystemData> list() {
        return osds;
    }

    @Override
    public Wrapper<ObjectSystemData> setList(List<ObjectSystemData> osds) {
        this.osds = osds;
        return this;
    }
}
