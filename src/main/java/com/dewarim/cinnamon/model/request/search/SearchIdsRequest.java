package com.dewarim.cinnamon.model.request.search;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.model.request.search.SearchType.*;

@JacksonXmlRootElement(localName = "searchIdsRequest")
public class SearchIdsRequest implements ApiRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private SearchType searchType;
    private String     query;

    public SearchIdsRequest() {
    }

    public SearchIdsRequest(SearchType searchType, String query) {
        this.searchType = searchType;
        this.query = query;
    }

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    @Override
    public List<ApiRequest> examples() {
        return List.of(new SearchIdsRequest(OSD, "<BooleanQuery><Clause+occurs='must'><TermQuery+fieldName='name'>test</TermQuery></Clause></BooleanQuery>"),
                new SearchIdsRequest(FOLDER,"<BooleanQuery><Clause+occurs='must'><TermQuery+fieldName='acl'>123</TermQuery></Clause></BooleanQuery>" ),
                new SearchIdsRequest(ALL,"<BooleanQuery><Clause+occurs='must'><TermQuery+fieldName='owner'>1337</TermQuery></Clause></BooleanQuery>" )
                );
    }

}
