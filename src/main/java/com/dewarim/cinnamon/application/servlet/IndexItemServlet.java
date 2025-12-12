package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.IndexService;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.index.CreateIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.DeleteIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.UpdateIndexItemRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.INDEX_SERVICE;

@WebServlet(name = "IndexItem", urlPatterns = "/")
public class IndexItemServlet extends HttpServlet implements CruddyServlet<IndexItem> {
    private static final Logger log = LogManager.getLogger(IndexItemServlet.class);

    private       IndexService indexService;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        IndexItemDao     indexItemDao     = new IndexItemDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case INDEX_ITEM__LIST -> {
                list(convertListRequest(cinnamonRequest, ListIndexItemRequest.class), indexItemDao, cinnamonResponse);
                log.info("Sending list index item response: {}", cinnamonRequest.getMapper().writeValueAsString(cinnamonResponse.getWrapper()));
            }
            case INDEX_ITEM__CREATE -> {
                superuserCheck();
                List<IndexItem> items = create(convertCreateRequest(cinnamonRequest, CreateIndexItemRequest.class), indexItemDao, cinnamonResponse);
                indexService.addIndexItems(items);
            }
            case INDEX_ITEM__UPDATE -> {
                superuserCheck();
                List<IndexItem> items = update(convertUpdateRequest(cinnamonRequest, UpdateIndexItemRequest.class), indexItemDao, cinnamonResponse);
                indexService.updateIndexItems(items);
            }
            case INDEX_ITEM__DELETE -> {
                superuserCheck();
                List<Long> items = delete(convertDeleteRequest(cinnamonRequest, DeleteIndexItemRequest.class), indexItemDao, cinnamonResponse);
                indexService.removeIndexItems(items);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public void init() {
        indexService = ((IndexService) getServletContext().getAttribute(INDEX_SERVICE));
    }
}