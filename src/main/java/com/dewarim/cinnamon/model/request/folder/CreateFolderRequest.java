package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JacksonXmlRootElement(localName = "createFolderRequest")
public class CreateFolderRequest implements CreateRequest<Folder>, ApiRequest<Folder> {

    private static final String DEFAULT_SUMMARY = "<summary />";

    @JacksonXmlElementWrapper(localName = "folders")
    @JacksonXmlProperty(localName = "folder")
    private List<Folder> folders = new ArrayList<>();

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(String name, Long parentId, String summary, Long ownerId, Long aclId, Long typeId) {
        folders.add(new Folder(name, aclId, ownerId, parentId, typeId, summary));
    }

    public CreateFolderRequest(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public boolean validated() {
        return folders != null && folders.size() > 0 && folders.stream().allMatch(f ->
                f.getName() != null && f.getName().trim().length() > 0
                        && f.getParentId() != null && f.getParentId() > 0
                        && (f.getTypeId() == null || f.getTypeId() > 0)
                        && (f.getAclId() == null || f.getAclId() > 0)
                        && (f.getOwnerId() == null || f.getOwnerId() > 0)
        );
    }

    @Override
    public List<Folder> list() {
        return folders;
    }

    @Override
    public Wrapper<Folder> fetchResponseWrapper() {
        return new FolderWrapper();
    }

    @Override
    public List<ApiRequest<Folder>> examples() {
        Folder f1 = new Folder("images", 1L, 2L, 3L, 4L, "<summary><description>contains images</description></summary>");
        Folder f2 = new Folder("archive", 2L, 2L, 2L, 2L, null);
        Date date = Date.from(LocalDateTime.of(2022,8,10,3,21).atZone(ZoneId.systemDefault()).toInstant());
        f1.setCreated(date);
        f2.setCreated(date);
        return List.of(new CreateFolderRequest(List.of(f1, f2)));
    }
}
