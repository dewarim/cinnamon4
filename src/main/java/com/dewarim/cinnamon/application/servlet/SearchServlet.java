package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.SearchResult;
import com.dewarim.cinnamon.model.request.search.SearchIdsRequest;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.SEARCH_SERVICE;

@WebServlet(name = "Search", urlPatterns = "/")
public class SearchServlet extends BaseServlet {
    private static final Logger log = LogManager.getLogger(SearchServlet.class);

    private       SearchService searchService;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case SEARCH__IDS -> searchIds(cinnamonRequest, cinnamonResponse, user);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void searchIds(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user) throws IOException {
        SearchIdsRequest searchRequest = request.getMapper().readValue(request.getInputStream(), SearchIdsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        SearchResult searchResult = searchService.doSearch(searchRequest.getQuery(), searchRequest.getSearchType(), user);
        cinnamonResponse.setResponse(new SearchIdsResponse(searchResult.osdIds(), searchResult.folderIds()));
    }

    @Override
    public void init() {
        searchService = ((SearchService) getServletContext().getAttribute(SEARCH_SERVICE));
    }
}