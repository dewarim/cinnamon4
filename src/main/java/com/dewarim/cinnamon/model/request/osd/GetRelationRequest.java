package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "getRelationRequest")
public class GetRelationRequest implements ApiRequest {

    private List<Long> ids;
    private boolean    includeMetadata;

    public GetRelationRequest() {
    }

    public GetRelationRequest(List<Long> ids, boolean includeMetadata) {
        this.ids = ids;
        this.includeMetadata = includeMetadata;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public boolean validated(){
        return ids != null && ids.size() > 0;
    }

    public Optional<GetRelationRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

}
