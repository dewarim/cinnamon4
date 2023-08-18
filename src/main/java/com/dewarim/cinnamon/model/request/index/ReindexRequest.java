package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "reindexRequest")
public class ReindexRequest implements ApiRequest<ReindexRequest> {

    private List<Long> osdIds;
    private List<Long> folderIds;

    public ReindexRequest() {
    }

    public ReindexRequest(List<Long> osdIds, List<Long> folderIds) {
        this.osdIds = osdIds;
        this.folderIds = folderIds;
    }

    public boolean doFullReindex() {
        return getOsdIds().isEmpty() && getFolderIds().isEmpty();
    }

    public List<Long> getOsdIds() {
        if (osdIds == null) {
            return List.of();
        }
        return osdIds;
    }

    public void setOsdIds(List<Long> osdIds) {
        this.osdIds = osdIds;
    }

    public List<Long> getFolderIds() {
        if (folderIds == null) {
            return List.of();
        }
        return folderIds;
    }

    public void setFolderIds(List<Long> folderIds) {
        this.folderIds = folderIds;
    }


    private boolean validated() {
        boolean validFolderIds = getFolderIds().stream().noneMatch(id -> id < 1);
        boolean validOsdIds    = getOsdIds().stream().noneMatch(id -> id < 1);
        return validFolderIds && validOsdIds;
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

    @Override
    public String toString() {
        return "ReIndexRequest{" +
                "osdIds=" + osdIds +
                ", folderIds=" + folderIds +
                '}';
    }
}
