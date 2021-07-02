package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class UpdateFolderTypeRequest implements UpdateRequest<FolderType>, ApiRequest {

    private List<FolderType> FolderTypes = new ArrayList<>();

    @Override
    public List<FolderType> list() {
        return FolderTypes;
    }

    public UpdateFolderTypeRequest() {
    }

    public UpdateFolderTypeRequest(Long id, String name) {
        FolderTypes.add(new FolderType(id,name));
    }

    public UpdateFolderTypeRequest(List<FolderType> FolderTypes) {
        this.FolderTypes = FolderTypes;
    }

    public List<FolderType> getFolderTypes() {
        return FolderTypes;
    }

    @Override
    public boolean validated() {
        return FolderTypes.stream().allMatch(FolderType ->
            FolderType != null && FolderType.getName() != null && !FolderType.getName().trim().isEmpty()
                    && FolderType.getId() != null && FolderType.getId() > 0);
    }

    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }
}
