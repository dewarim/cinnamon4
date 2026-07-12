package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("createFolderRequest")
public record CreateFolderRequest(
        @JacksonXmlElementWrapper(localName = "folders")
        @JacksonXmlProperty(localName = "folder")
        List<Folder> folders) implements CreateRequest<Folder>, ApiRequest<Folder> {

    public CreateFolderRequest {
        if (folders == null) {
            folders = new ArrayList<>();
        }
    }

    public CreateFolderRequest() {
        this(new ArrayList<>());
    }

    public CreateFolderRequest(String name, Long parentId, String summary, Long ownerId, Long aclId, Long typeId) {
        this(new ArrayList<>(List.of(new Folder(name, aclId, ownerId, parentId, typeId, summary))));
    }

    public boolean validated() {
        return folders != null && !folders.isEmpty() && folders.stream().allMatch(f ->
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
        f1.setCreated(LOCAL_DATE_TIME_EXAMPLE);
        f2.setCreated(LOCAL_DATE_TIME_EXAMPLE);
        f1.setMetasets(List.of(new Meta(1L, 2L, "<xml>some meta content</xml>")));
        return List.of(new CreateFolderRequest(List.of(f1, f2)));
    }
}
