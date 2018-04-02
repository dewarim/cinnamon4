package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class OsdWrapper {

    @JacksonXmlElementWrapper(localName = "osds")
    @JacksonXmlProperty(localName = "osd")
    List<ObjectSystemData> osds = new ArrayList<>();

    public List<ObjectSystemData> getOsds() {
        return osds;
    }

    public void setOsds(List<ObjectSystemData> osds) {
        this.osds = osds;
    }
}
