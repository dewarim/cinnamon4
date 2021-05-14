package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void fetchIndexHtml() throws IOException {
        String localPart = "/doc/index.html";
        String indexHtml = getStaticTextContent(localPart, "text/html", "UTF-8");

        InputStream inputStream         = getClass().getResourceAsStream("/static/doc/index.html");
        byte[]      originalFileContent = inputStream.readAllBytes();
        String      sourceFile          = new String(originalFileContent);

        assertThat(indexHtml, equalTo(sourceFile));
    }

    @Test
    public void tryIllegalAccess() throws IOException {
        String       url      = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + "/../sql/mybatis-properties.xml";
        HttpResponse response = Request.Get(url).execute().returnResponse();
        // Jetty automatically filters ../
        //  assertCinnamonError(response, ErrorCode.STATIC__NO_PATH_TRAVERSAL, SC_FORBIDDEN);
        assertThat(response.getStatusLine().getStatusCode(),equalTo(SC_NOT_FOUND));
    }

    @Test
    public void handle404() throws IOException {
        String       url      = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + "does-not-exist.txt";
        HttpResponse response = Request.Get(url).execute().returnResponse();
        assertCinnamonError(response, ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    public void checkContentTypeForCss() throws IOException {
        String localPart = "/doc/main.css";
        getStaticTextContent(localPart, "text/css", "UTF-8");
    }

    private String getStaticTextContent(String path, String contentType, String encoding) throws IOException {
        String       url      = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + path;
        HttpResponse response = Request.Get(url).execute().returnResponse();
        assertResponseOkay(response);
        ByteArrayOutputStream bof = new ByteArrayOutputStream();
        response.getEntity().writeTo(bof);
        String[] contentTypeElements = response.getEntity().getContentType().getValue().split(";");
        if (contentType != null) {
            assertThat(contentTypeElements[0], equalTo(contentType));
        }
        if (encoding != null) {
            assertThat(contentTypeElements[1].trim(), equalTo("charset=" + encoding.toLowerCase()));
            return bof.toString(encoding);
        }
        return bof.toString(StandardCharsets.UTF_8);
    }

}
