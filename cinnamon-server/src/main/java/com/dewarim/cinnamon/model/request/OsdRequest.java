package com.dewarim.cinnamon.model.request;

import java.util.ArrayList;
import java.util.List;

public class OsdRequest {
    
    boolean includeSummary;
    
    List<Long> ids = new ArrayList<>();

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
