package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("reindexResponse")
public class ReindexResponse extends BaseResponse implements ApiResponse {

    private int documentsToIndex;
    private int foldersToIndex;
    private int tikaMetasetsDeleted;

    public ReindexResponse() {
    }

    public ReindexResponse(int documentsToIndex, int foldersToIndex) {
        this.documentsToIndex = documentsToIndex;
        this.foldersToIndex=foldersToIndex;
    }

    public int getDocumentsToIndex() {
        return documentsToIndex;
    }

    public void setDocumentsToIndex(int documentsToIndex) {
        this.documentsToIndex = documentsToIndex;
    }

    /**
     * How many tika metasets were deleted, which is how a reindex with updateTikaMetaset makes
     * TikaService extract those documents again. 0 unless updateTikaMetaset was set.
     */
    public int getTikaMetasetsDeleted() {
        return tikaMetasetsDeleted;
    }

    public void setTikaMetasetsDeleted(int tikaMetasetsDeleted) {
        this.tikaMetasetsDeleted = tikaMetasetsDeleted;
    }

    public int getFoldersToIndex() {
        return foldersToIndex;
    }

    public void setFoldersToIndex(int foldersToIndex) {
        this.foldersToIndex = foldersToIndex;
    }

    @Override
    public List<Object> examples() {
        ReindexResponse response = new ReindexResponse(1000, 123);
        response.setTikaMetasetsDeleted(1000);
        return List.of(response);
    }

    @Override
    public String toString() {
        return "ReindexResponse{" +
                "documentsToIndex=" + documentsToIndex +
                ", foldersToIndex=" + foldersToIndex +
                ", tikaMetasetsDeleted=" + tikaMetasetsDeleted +
                '}';
    }
}
