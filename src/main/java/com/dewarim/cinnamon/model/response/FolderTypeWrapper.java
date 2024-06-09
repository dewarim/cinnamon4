package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.FolderType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class FolderTypeWrapper extends BaseResponse implements Wrapper<FolderType>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "folderTypes")
    @JacksonXmlProperty(localName = "folderType")
    List<FolderType> folderTypes = new ArrayList<>();

    public List<FolderType> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<FolderType> folderTypes) {
        this.folderTypes = folderTypes;
    }

    @Override
    public List<FolderType> list() {
        return getFolderTypes();
    }

    @Override
    public Wrapper<FolderType> setList(List<FolderType> folderTypes) {
        setFolderTypes(folderTypes);
        return this;
    }

    @Override
    public List<Object> examples() {
        FolderTypeWrapper folderTypeWrapper = new FolderTypeWrapper();
        folderTypeWrapper.getFolderTypes().add(new FolderType(1L, "system-folder"));
        return List.of(folderTypeWrapper);
    }
}
