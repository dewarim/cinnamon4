package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("indexInfoRequest")
public record IndexInfoRequest(Boolean countDocuments, boolean listFailedIndexJobs) implements ApiRequest<IndexInfoRequest> {

    public IndexInfoRequest {
        if (countDocuments == null) {
            countDocuments = true;
        }
    }

    @Override
    public List<ApiRequest<IndexInfoRequest>> examples() {
        return List.of(new IndexInfoRequest(true, false));
    }
}
