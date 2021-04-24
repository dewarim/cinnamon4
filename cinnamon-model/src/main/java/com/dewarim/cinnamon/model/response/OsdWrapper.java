package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class OsdWrapper implements Wrapper<ObjectSystemData>{

    @JacksonXmlElementWrapper(localName = "osds")
    @JacksonXmlProperty(localName = "osd")
    private List<ObjectSystemData> osds = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "links")
    @JacksonXmlProperty(localName = "link")
    private List<Link> links = new ArrayList<>();

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
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @Override
    public List<ObjectSystemData> get() {
        return osds;
    }
}
