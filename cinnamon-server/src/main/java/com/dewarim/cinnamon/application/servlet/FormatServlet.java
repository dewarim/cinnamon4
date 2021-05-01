package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.format.ListFormatRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
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

@WebServlet(name = "Format", urlPatterns = "/")
public class FormatServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listFormats":
                listFormats(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listFormats(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        xmlMapper.readValue(request.getInputStream(), ListFormatRequest.class);
        FormatDao     formatDao = new FormatDao();
        List<Format>  formats   = formatDao.listFormats();
        FormatWrapper wrapper   = new FormatWrapper();
        wrapper.setFormats(formats);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}
