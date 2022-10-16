package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "indexInfoResponse")
public class IndexInfoResponse implements ApiResponse {

    private Integer documentsInIndex;
    private Integer foldersInIndex;
    private Integer failedJobCount;

    private Integer jobCount;

    public IndexInfoResponse() {
    }

    public IndexInfoResponse(Integer documentsInIndex, Integer foldersInIndex) {
        this.documentsInIndex = documentsInIndex;
        this.foldersInIndex = foldersInIndex;
    }

    public Integer getJobCount() {
        return jobCount;
    }

    public IndexInfoResponse setJobCount(Integer jobCount) {
        this.jobCount = jobCount;
        return this;
    }

    public Integer getFailedJobCount() {
        return failedJobCount;
    }

    public void setFailedJobCount(Integer failedJobCount) {
        this.failedJobCount = failedJobCount;
    }

    public Integer getDocumentsInIndex() {
        return documentsInIndex;
    }

    public void setDocumentsInIndex(Integer documentsInIndex) {
        this.documentsInIndex = documentsInIndex;
    }

    public Integer getFoldersInIndex() {
        return foldersInIndex;
    }

    public void setFoldersInIndex(Integer foldersInIndex) {
        this.foldersInIndex = foldersInIndex;
    }

    @Override
    public String toString() {
        return "IndexInfoResponse{" +
                "documentsInIndex=" + documentsInIndex +
                ", foldersInIndex=" + foldersInIndex +
                ", failedJobCount=" + failedJobCount +
                ", jobCount=" + jobCount +
                '}';
    }
}
