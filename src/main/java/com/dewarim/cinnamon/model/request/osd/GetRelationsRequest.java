package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "getRelationsRequest")
public class GetRelationsRequest implements ApiRequest<Relation> {

    private List<Long> ids;
    private boolean    includeMetadata;

    public GetRelationsRequest() {
    }

    public GetRelationsRequest(List<Long> ids, boolean includeMetadata) {
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

    public Optional<GetRelationsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<Relation>> examples() {
        return List.of(new GetRelationsRequest(List.of(1L,32L,4L), false));
    }
}
