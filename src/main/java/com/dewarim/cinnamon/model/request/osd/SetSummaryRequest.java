package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("setSummaryRequest")
public record SetSummaryRequest(Long id, String summary) implements ApiRequest<SetSummaryRequest> {

    @Override
    public List<ApiRequest<SetSummaryRequest>> examples() {
        return List.of(
                new SetSummaryRequest(45L, "<xml>summary</xml>"),
                new SetSummaryRequest(65L, "be careful when indexing non-xml summaries")
        );
    }
}
