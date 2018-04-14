package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.LinkType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "link")
public class LinkResponse {

    private LinkType linkType;
    private ObjectSystemData osd; 
    private Folder folder;

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setOsd(ObjectSystemData osd) {
        this.osd = osd;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
