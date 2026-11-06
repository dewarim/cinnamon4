package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.dewarim.cinnamon.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("indexInfoResponse")
public class IndexInfoResponse extends BaseResponse implements ApiResponse {

    private Integer           documentsInIndex;
    private Integer           foldersInIndex;
    private Integer           failedJobCount;
    private List<IndexJob>    failedIndexJobs;
    private Integer           jobCount;
    private Integer           failedTikaExtractionCount;
    private List<TikaFailure> failedTikaExtractions;

    public IndexInfoResponse() {
    }

    public IndexInfoResponse(Integer documentsInIndex, Integer foldersInIndex, Integer failedJobCount, Integer jobCount) {
        this.documentsInIndex = documentsInIndex;
        this.foldersInIndex   = foldersInIndex;
        this.failedJobCount   = failedJobCount;
        this.jobCount         = jobCount;
    }

    /**
     * How many OSDs Tika could not extract metadata from. These are not index jobs: the documents
     * are indexed, they just carry no Tika metadata. 0 when Tika is not in use.
     */
    public Integer getFailedTikaExtractionCount() {
        return failedTikaExtractionCount;
    }

    public void setFailedTikaExtractionCount(Integer failedTikaExtractionCount) {
        this.failedTikaExtractionCount = failedTikaExtractionCount;
    }

    /**
     * The failures themselves, with the attempts made and the last error - only filled when the
     * request asked for them, since the list is as long as the number of broken documents.
     */
    public List<TikaFailure> getFailedTikaExtractions() {
        return failedTikaExtractions;
    }

    public void setFailedTikaExtractions(List<TikaFailure> failedTikaExtractions) {
        this.failedTikaExtractions = failedTikaExtractions;
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
                ", failedTikaExtractionCount=" + failedTikaExtractionCount +
                '}';
    }

    @Override
    public List<Object> examples() {
        IndexInfoResponse response = new IndexInfoResponse(100, 20, 2, 41);
        response.setFailedIndexJobs(List.of(new IndexJob(IndexJobType.OSD, 143L, IndexJobAction.CREATE)));
        response.setFailedTikaExtractionCount(1);
        response.setFailedTikaExtractions(List.of(new TikaFailure(143L, 3, LOCAL_DATE_TIME_EXAMPLE,
                "Tika answered with HTTP status 422")));
        return List.of(response);
    }
}
