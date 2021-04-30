package com.dewarim.cinnamon.model.request.link;

public class LinkRequest {
    
    private Long id;
    private boolean includeSummary;

    public LinkRequest() {
    }

    public LinkRequest(long id, boolean includeSummary) {
        this.id = id;
        this.includeSummary = includeSummary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
    
    public boolean validated(){
        return id != null && id > 0;
    }
}
