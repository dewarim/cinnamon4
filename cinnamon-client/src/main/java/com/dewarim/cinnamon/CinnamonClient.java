package com.dewarim.cinnamon;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class CinnamonClient {

    private int       cinnamonPort = 8080;
    private String    ticket;
    private String    username     = "doe";
    private String    password     = "admin";
    private XmlMapper mapper       = new XmlMapper();

    private HttpResponse sendStandardMultipartRequest(UrlMapping urlMapping, HttpEntity multipartEntity) throws IOException {
        return Request.Post("http://localhost:" + cinnamonPort + urlMapping.getPath())
                .addHeader("ticket", getTicket(false))
                .body(multipartEntity).execute().returnResponse();
    }

    protected String getTicket(boolean newTicket) throws IOException {
        if (ticket == null || newTicket) {
            String url = "http://localhost:" + cinnamonPort + UrlMapping.CINNAMON__CONNECT.getPath();
            String tokenRequestResult = Request.Post(url)
                    .bodyForm(Form.form().add("user", username).add("pwd", password).build())
                    .execute().returnContent().asString();
            CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
            ticket = cinnamonConnection.getTicket();
            return ticket;
        } else {
            return ticket;
        }
    }

}
