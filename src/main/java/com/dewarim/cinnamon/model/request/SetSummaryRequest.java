package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

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
    
}
