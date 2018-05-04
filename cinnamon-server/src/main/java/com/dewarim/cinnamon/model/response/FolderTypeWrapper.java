package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.model.FolderType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class FolderTypeWrapper {

    @JacksonXmlElementWrapper(localName = "folderTypes")
    @JacksonXmlProperty(localName = "folderType")
    List<FolderType> folderTypes = new ArrayList<>();

    public List<FolderType> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<FolderType> folderTypes) {
        this.folderTypes = folderTypes;
    }
}
