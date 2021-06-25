package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateFolderTypeRequest implements CreateRequest<FolderType> {

    private List<String> names = new ArrayList<>();

    @Override
    public List<FolderType> list() {
        return names.stream().map(name -> new FolderType(null, name)).collect(Collectors.toList());
    }

    public CreateFolderTypeRequest() {
    }

    public CreateFolderTypeRequest(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean validated() {
        return names.stream().noneMatch(name -> name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }
}
