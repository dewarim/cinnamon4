package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "linkResponse")
public class LinkResponse extends Link {

    private ObjectSystemData osd;
    private Folder folder;

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

    @Override
    public String toString() {
        return "LinkResponse{" +
                "osd=" + osd +
                ", folder=" + folder +
                '}';
    }
}
