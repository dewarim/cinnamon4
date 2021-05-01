package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "IndexItem", urlPatterns = "/")
public class IndexItemServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listIndexItems":
                listIndexItems(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listIndexItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest      listRequest = xmlMapper.readValue(request.getInputStream(), ListIndexItemRequest.class);
        IndexItemDao     typeDao     = new IndexItemDao();
        List<IndexItem>  types       = typeDao.listIndexItems();
        IndexItemWrapper wrapper     = new IndexItemWrapper();
        wrapper.setIndexItems(types);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}