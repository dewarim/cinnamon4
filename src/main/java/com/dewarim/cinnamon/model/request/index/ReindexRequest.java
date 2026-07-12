package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("reindexRequest")
public record ReindexRequest(List<Long> osdIds, List<Long> folderIds, Boolean updateTikaMetaset) implements ApiRequest<ReindexRequest> {

    public ReindexRequest {
        if (osdIds == null) {
            osdIds = new ArrayList<>();
        }
        if (folderIds == null) {
            folderIds = new ArrayList<>();
        }
        if (updateTikaMetaset == null) {
            updateTikaMetaset = false;
        }
    }

    public ReindexRequest() {
        this(new ArrayList<>(), new ArrayList<>(), false);
    }

    public ReindexRequest(List<Long> osdIds, List<Long> folderIds) {
        this(osdIds, folderIds, false);
    }

    public boolean doFullReindex() {
        return osdIds.isEmpty() && folderIds.isEmpty();
    }

    private boolean validated() {
        boolean validFolderIds = folderIds.stream().noneMatch(id -> id < 1);
        boolean validOsdIds    = osdIds.stream().noneMatch(id -> id < 1);
        return validFolderIds && validOsdIds && updateTikaMetaset != null;
    }

    public Optional<ReindexRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<ReindexRequest>> examples() {
        return List.of(new ReindexRequest(), new ReindexRequest(List.of(13L, 23L), List.of(43L, 2L)));
    }
}
