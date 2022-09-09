package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.model.request.index.IndexInfoRequest;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.SEARCH_SERVICE;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Index", urlPatterns = "/")
public class IndexServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(IndexServlet.class);

    private final ObjectMapper  xmlMapper = XML_MAPPER;
    private       SearchService searchService;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case INDEX__INFO -> info(request, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void info(HttpServletRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        IndexInfoRequest createRequest = xmlMapper.readValue(request.getInputStream(), IndexInfoRequest.class);
        if(createRequest.isCountDocuments()) {
            Integer docCount = searchService.countDocs();
            cinnamonResponse.setResponse(new IndexInfoResponse(docCount));
        }
        else{
            cinnamonResponse.setResponse(new IndexInfoResponse());
        }
    }

    public ObjectMapper getMapper() {
        return xmlMapper;
    }

    @Override
    public void init() {
        searchService = ((SearchService) getServletContext().getAttribute(SEARCH_SERVICE));
    }
}