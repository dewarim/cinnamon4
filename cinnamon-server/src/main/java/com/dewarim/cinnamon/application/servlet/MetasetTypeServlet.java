package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
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

@WebServlet(name = "MetasetType", urlPatterns = "/")
public class MetasetTypeServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listMetasetTypes":
                listMetasetTypes(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listMetasetTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest        listRequest = xmlMapper.readValue(request.getInputStream(), ListMetasetTypeRequest.class);
        MetasetTypeDao     typeDao     = new MetasetTypeDao();
        List<MetasetType>  types       = typeDao.listMetasetTypes();
        MetasetTypeWrapper wrapper     = new MetasetTypeWrapper();
        wrapper.setMetasetTypes(types);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}