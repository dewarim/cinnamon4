package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.links.Link;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.FOLDER_EXAMPLE;

@JacksonXmlRootElement(localName = "cinnamon")
public class FolderWrapper extends BaseResponse implements Wrapper<Folder>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "folders")
    @JacksonXmlProperty(localName = "folder")
    List<Folder> folders = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "links")
    @JacksonXmlProperty(localName = "link")
    private List<Link> links = new ArrayList<>();

    public FolderWrapper() {
    }

    public FolderWrapper(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @Override
    public List<Folder> list() {
        return getFolders();
    }

    @Override
    public Wrapper<Folder> setList(List<Folder> folders) {
        setFolders(folders);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new FolderWrapper(List.of(FOLDER_EXAMPLE)));
    }
}
