package com.dewarim.cinnamon.model.request.search;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.model.request.search.SearchType.*;

@JsonRootName("searchIdsRequest")
public record SearchIdsRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull SearchType searchType,
        @NotEmpty String query) implements ApiRequest<SearchIdsRequest> {

    public Optional<SearchIdsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<SearchIdsRequest>> examples() {
        return List.of(new SearchIdsRequest(OSD, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>test</TermQuery></Clause></BooleanQuery>"),
                new SearchIdsRequest(FOLDER, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='acl'>123</TermQuery></Clause></BooleanQuery>"),
                new SearchIdsRequest(ALL, "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='owner'>1337</TermQuery></Clause></BooleanQuery>")
        );
    }
}
