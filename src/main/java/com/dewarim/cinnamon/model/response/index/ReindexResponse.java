package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "reindexResponse")
public class ReindexResponse implements ApiResponse {

    private int documentsToIndex;
    private int foldersToIndex;

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

    public int getFoldersToIndex() {
        return foldersToIndex;
    }

    public void setFoldersToIndex(int foldersToIndex) {
        this.foldersToIndex = foldersToIndex;
    }

    @Override
    public List<Object> examples() {
        return List.of(new ReindexResponse(1000, 123));
    }

    @Override
    public String toString() {
        return "ReindexResponse{" +
                "documentsToIndex=" + documentsToIndex +
                ", foldersToIndex=" + foldersToIndex +
                '}';
    }
}
