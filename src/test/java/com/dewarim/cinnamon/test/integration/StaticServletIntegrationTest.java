package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.StandardResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StaticServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void fetchIndexHtml() throws IOException {
        String localPart = "doc/index.html";
        String indexHtml = getStaticTextContent(localPart, "text/html");

        InputStream inputStream         = getClass().getResourceAsStream("/static/doc/index.html");
        assertNotNull(inputStream);
        byte[]      originalFileContent = inputStream.readAllBytes();
        String      sourceFile          = new String(originalFileContent);

        assertThat(indexHtml, equalTo(sourceFile));
    }

    @Test
    public void tryIllegalAccess() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + "/../sql/mybatis-properties.xml";
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.get(url).build(), StandardResponse::new)) {
            // Jetty automatically filters ../
            //  assertCinnamonError(response, ErrorCode.STATIC__NO_PATH_TRAVERSAL, SC_FORBIDDEN);
            assertThat(response.getCode(), equalTo(SC_BAD_REQUEST));
        }
    }

    @Test
    public void handle404() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + "does-not-exist.txt";
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.get(url).build(), StandardResponse::new)) {
            assertCinnamonError(response, ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Test
    public void checkContentTypeForCss() throws IOException {
        String localPart = "doc/main.css";
        getStaticTextContent(localPart, "text/css");
    }

    private String getStaticTextContent(String path, String contentType) throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.STATIC__ROOT.getPath() + path;
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.get(url).build(), StandardResponse::new)) {
            assertResponseOkay(response);
            ByteArrayOutputStream bof = new ByteArrayOutputStream();
            response.getEntity().writeTo(bof);
            String[] contentTypeElements = response.getEntity().getContentType().split(";");
            if (contentType != null) {
                assertThat(contentTypeElements[0], equalTo(contentType));
            }
            assertThat(contentTypeElements[1].trim(), equalTo("charset=" + "UTF-8".toLowerCase()));
            return bof.toString(StandardCharsets.UTF_8);
        }
    }

}
