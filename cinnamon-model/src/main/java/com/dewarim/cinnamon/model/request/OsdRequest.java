package com.dewarim.cinnamon.model.request;

import java.util.ArrayList;
import java.util.List;

public class OsdRequest {

    private List<Long> ids = new ArrayList<>();

    private boolean includeSummary;

    public OsdRequest() {
    }

    public OsdRequest(List<Long> ids, boolean includeSummary) {
        this.includeSummary = includeSummary;
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
}
