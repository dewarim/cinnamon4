package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "setSummaryRequest")
public class SetSummaryRequest implements ApiRequest {
    
    private Long id;
    private String summary;

    public SetSummaryRequest() {
    }

    public SetSummaryRequest(Long id, String summary) {
        this.id = id;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public List<ApiRequest> examples() {
        return List.of(new SetSummaryRequest(45L, "<xml>summary</xml>"), new SetSummaryRequest(65L,"be careful when indexing non-xml summaries"));
    }
}
