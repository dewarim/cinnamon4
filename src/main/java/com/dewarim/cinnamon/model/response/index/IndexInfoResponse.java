package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.response.BaseResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "indexInfoResponse")
public class IndexInfoResponse extends BaseResponse implements ApiResponse {

    private Integer        documentsInIndex;
    private Integer        foldersInIndex;
    private Integer        failedJobCount;
    private List<IndexJob> failedIndexJobs;
    private Integer        jobCount;

    public IndexInfoResponse() {
    }

    public IndexInfoResponse(Integer documentsInIndex, Integer foldersInIndex, Integer failedJobCount, Integer jobCount) {
        this.documentsInIndex = documentsInIndex;
        this.foldersInIndex   = foldersInIndex;
        this.failedJobCount   = failedJobCount;
        this.jobCount         = jobCount;
    }

    public List<IndexJob> getFailedIndexJobs() {
        return failedIndexJobs;
    }

    public void setFailedIndexJobs(List<IndexJob> failedIndexJobs) {
        this.failedIndexJobs = failedIndexJobs;
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

    @Override
    public List<Object> examples() {
        IndexInfoResponse response = new IndexInfoResponse(100, 20, 2, 41);
        response.setFailedIndexJobs(List.of(new IndexJob(IndexJobType.OSD, 143L, IndexJobAction.CREATE)));
        return List.of(response);
    }
}
