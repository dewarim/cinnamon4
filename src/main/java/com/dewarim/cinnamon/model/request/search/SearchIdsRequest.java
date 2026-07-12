package com.dewarim.cinnamon.model.request.search;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.model.request.search.SearchType.*;

@JsonRootName("searchIdsRequest")
public record SearchIdsRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        SearchType searchType,
        String query) implements ApiRequest<SearchIdsRequest> {

    private boolean validated() {
        return query != null && !query.isEmpty() && searchType != null;
    }

    public Optional<SearchIdsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SearchIdsRequest>> examples() {
        return List.of(new SearchIdsRequest(OSD, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>test</TermQuery></Clause></BooleanQuery>"),
                new SearchIdsRequest(FOLDER, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='acl'>123</TermQuery></Clause></BooleanQuery>"),
                new SearchIdsRequest(ALL, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='owner'>1337</TermQuery></Clause></BooleanQuery>")
        );
    }
}
