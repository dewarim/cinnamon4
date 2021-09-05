package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "getLinksRequest")
public class GetLinksRequest implements ApiRequest {
    
    private boolean includeSummary;

    List<Long> ids = new ArrayList<>();

    public GetLinksRequest() {
    }

    public GetLinksRequest(Long id, boolean includeSummary) {
        ids.add(id);
        this.includeSummary=includeSummary;
    }

    public GetLinksRequest(List<Long> ids, boolean includeSummary) {
        this.ids = ids;
        this.includeSummary = includeSummary;
    }

    public List<Long> getIds() {
        return ids;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
    
    public boolean validated(){
        if(ids == null || ids.isEmpty()){
            return false;
        }
        return ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<GetLinksRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }


}
