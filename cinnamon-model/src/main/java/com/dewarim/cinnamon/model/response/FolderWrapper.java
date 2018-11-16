package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class FolderWrapper {

    @JacksonXmlElementWrapper(localName = "folders")
    @JacksonXmlProperty(localName = "folders")
    List<Folder> folders = new ArrayList<>();

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }
}