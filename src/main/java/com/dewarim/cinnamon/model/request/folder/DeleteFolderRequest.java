package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.*;

@JacksonXmlRootElement(localName = "deleteFolderRequest")
public class DeleteFolderRequest implements ApiRequest<Folder> {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private Set<Long> ids               = new HashSet<>();
    private boolean   deleteRecursively = false;
    private boolean    deleteContent     = false;

    public DeleteFolderRequest() {
    }

    public DeleteFolderRequest(List<Long> ids, boolean deleteRecursively, boolean deleteContent) {
        this.ids = new HashSet<>(ids);
        this.deleteRecursively = deleteRecursively;
        this.deleteContent = deleteContent;
    }

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    public boolean isDeleteRecursively() {
        return deleteRecursively;
    }

    public void setDeleteRecursively(boolean deleteRecursively) {
        this.deleteRecursively = deleteRecursively;
    }

    public boolean isDeleteContent() {
        return deleteContent;
    }

    public void setDeleteContent(boolean deleteContent) {
        this.deleteContent = deleteContent;
    }

    @Override
    public String toString() {
        return "DeleteFolderRequest{" +
                "ids=" + ids +
                ", deleteRecursively=" + deleteRecursively +
                ", deleteContent=" + deleteContent +
                '}';
    }

    @Override
    public List<ApiRequest<Folder>> examples() {
        return List.of(new DeleteFolderRequest(List.of(1L,2L,3L),true,false));
    }

    private boolean validated() {
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> Objects.nonNull(id) && id > 0);
    }

    public Optional<DeleteFolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        }
        return Optional.empty();
    }
}
