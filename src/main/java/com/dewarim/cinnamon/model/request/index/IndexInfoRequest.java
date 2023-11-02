package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "indexInfoRequest")

public class IndexInfoRequest implements ApiRequest<IndexInfoRequest> {
    private boolean countDocuments      = true;
    private boolean listFailedIndexJobs =false;

    public IndexInfoRequest() {
    }

    public IndexInfoRequest(boolean countDocuments, boolean listFailedIndexJobs) {
        this.countDocuments      = countDocuments;
        this.listFailedIndexJobs = listFailedIndexJobs;
    }

    public boolean isCountDocuments() {
        return countDocuments;
    }

    public void setCountDocuments(boolean countDocuments) {
        this.countDocuments = countDocuments;
    }

    public boolean isListFailedIndexJobs() {
        return listFailedIndexJobs;
    }

    @Override
    public String toString() {
        return "IndexInfoRequest{" +
                "countDocuments=" + countDocuments +
                ", listFailedIndexJobs=" + listFailedIndexJobs +
                '}';
    }

    @Override
    public List<ApiRequest<IndexInfoRequest>> examples() {
        return List.of(new IndexInfoRequest(true,false));
    }
}
