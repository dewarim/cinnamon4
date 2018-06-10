package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.IdleConnectionEvictor;
import org.junit.Test;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class OsdServletIntegrationTest extends CinnamonIntegrationTest {

    private static final String SET_CONTENT_URL = HOST + UrlMapping.OSD__SET_CONTENT.getPath();

    @Test
    public void getObjectsById() throws IOException {
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(1L, 2L, 3L, 4L, 5L, 6L));
        HttpResponse           response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> dataList = unwrapOsds(response, 5);
        assertFalse(dataList.stream().anyMatch(osd -> osd.getName().equals("unbrowsable-test")));

        // test for dynamic groups:
        assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("owned-by-doe")));
        assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("acl-for-everyone")));

    }

    @Test
    public void getObjectsByIdForAdmin() throws IOException {
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(1L, 2L, 3L, 4L, 5L, 6L));
        HttpResponse           response = sendAdminRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> dataList = unwrapOsds(response, 6);
        // admin is exempt from permission checks, should get everything:
        assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("unbrowsable-test")));

        // test for dynamic groups:
        assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("owned-by-doe")));
        assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("acl-for-everyone")));
    }

    @Test
    public void getObjectsByIdWithDefaultSummary() throws IOException {
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(1L));
        osdRequest.setIncludeSummary(false);
        HttpResponse           response = sendAdminRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> dataList = unwrapOsds(response, 1);
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals(new ObjectSystemData().getSummary()) ));
    }

    @Test
    public void getObjectsByIdIncludingSummary() throws IOException {
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(1L));
        osdRequest.setIncludeSummary(true);
        HttpResponse           response = sendAdminRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> dataList = unwrapOsds(response, 1);
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals("<summary>sum of sum</summary>")));
    }

    @Test
    public void getObjectsByFolderId() throws IOException {
        OsdByFolderRequest     osdRequest = new OsdByFolderRequest(4L, true);
        HttpResponse           response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        List<ObjectSystemData> dataList   = unwrapOsds(response, 2);
        List<Link>             links      = unwrapLinks(response, 1);
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals("<summary>child@archive</summary>")));
        // link.objectId is #10, because it's yet unresolved (latest_head would be osd#11).
        assertThat(links.get(0).getObjectId(), equalTo(10L));
    }

    @Test
    public void setSummaryHappyPath() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(17L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);

        IdListRequest idListRequest  = new IdListRequest(Collections.singletonList(17L));
        HttpResponse  verifyResponse = sendStandardRequest(UrlMapping.OSD__GET_SUMMARIES, idListRequest);
        assertResponseOkay(verifyResponse);
        SummaryWrapper wrapper = mapper.readValue(verifyResponse.getEntity().getContent(), SummaryWrapper.class);
        assertThat(wrapper.getSummaries().get(0), equalTo("a summary"));
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(18L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION, HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(16L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.OSD__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper   = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        List<String>   summaries = wrapper.getSummaries();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(wrapper.getSummaries().get(0), equalTo("<sum>7</sum>"));
    }

    @Test
    public void getSummariesMissingPermission() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(18L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.OSD__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        // when all ids are non-readable, return an empty list:
        assertNull(wrapper.getSummaries());
    }

    @Test
    public void setContentWithDefaultContentProviderHappyPath() throws IOException {
        SetContentRequest setContentRequest = new SetContentRequest(22L, 1L);
        MultipartEntity   multipartEntity   = createMultipartEntity(setContentRequest);
        HttpResponse      response          = sendStandardMultipartRequest(SET_CONTENT_URL, multipartEntity);
        assertResponseOkay(response);

        // check data is in content store:
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(22L));
        HttpResponse osdResponse = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        assertResponseOkay(osdResponse);
        ObjectSystemData osd         = unwrapOsds(osdResponse, 1).get(0);
        String           contentPath = osd.getContentPath();
        String           dataRoot    = CinnamonServer.config.getServerConfig().getDataRoot();
        File             content     = new File(dataRoot, contentPath);
        assertTrue(content.exists());
        assertEquals((long) osd.getContentSize(), content.length());
        String sha256Hex = DigestUtils.sha256Hex(new FileInputStream(content));
        assertEquals(osd.getContentHash(), sha256Hex);
    }


    @Test
    public void setContentWithWrongContentType() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.OSD__SET_CONTENT, null);
        assertCinnamonError(response, ErrorCode.NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void setContentWithoutProperRequest() throws IOException {
        File            pomXml          = new File("pom.xml");
        FileBody        fileBody        = new FileBody(pomXml);
        MultipartEntity multipartEntity = new MultipartEntity();
        multipartEntity.addPart("file", fileBody);
        HttpResponse response = sendStandardMultipartRequest(SET_CONTENT_URL, multipartEntity);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void setContentWithoutFile() throws IOException {
        SetContentRequest contentRequest  = new SetContentRequest(22L, 1L);
        StringBody        setContentBody  = new StringBody(mapper.writeValueAsString(contentRequest), APPLICATION_XML.getMimeType(), Charset.forName("UTF-8"));
        MultipartEntity   multipartEntity = new MultipartEntity();
        multipartEntity.addPart("setContentRequest", setContentBody);
        HttpResponse response = sendStandardMultipartRequest(SET_CONTENT_URL, multipartEntity);
        assertCinnamonError(response, ErrorCode.MISSING_FILE_PARAMETER);
    }

    @Test
    public void setContentWithInvalidParameters() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(-1L, 0L);
        HttpResponse      response       = sendStandardMultipartRequest(SET_CONTENT_URL, createMultipartEntity(contentRequest));
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void setContentWithUnknownOsdId() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(Long.MAX_VALUE, 1L);
        HttpResponse      response       = sendStandardMultipartRequest(SET_CONTENT_URL, createMultipartEntity(contentRequest));
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void setContentWithUnknownFormatId() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(22L, Long.MAX_VALUE);
        HttpResponse      response       = sendStandardMultipartRequest(SET_CONTENT_URL, createMultipartEntity(contentRequest));
        assertCinnamonError(response, ErrorCode.FORMAT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void setContentWithoutWritePermission() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(23L, 1L);
        HttpResponse      response       = sendStandardMultipartRequest(SET_CONTENT_URL, createMultipartEntity(contentRequest));
        assertCinnamonError(response, ErrorCode.NO_WRITE_PERMISSION, SC_FORBIDDEN);
    }


    private HttpResponse sendStandardMultipartRequest(String url, MultipartEntity multipartEntity) throws IOException {
        return Request.Post(url)
                .addHeader("ticket", getDoesTicket(false))
                .body(multipartEntity).execute().returnResponse();
    }

    private MultipartEntity createMultipartEntity(SetContentRequest contentRequest) throws IOException {
        File       pomXml         = new File("pom.xml");
        FileBody   fileBody       = new FileBody(pomXml);
        StringBody setContentBody = new StringBody(mapper.writeValueAsString(contentRequest), APPLICATION_XML.getMimeType(), Charset.forName("UTF-8"));

        MultipartEntity multipartEntity = new MultipartEntity();
        multipartEntity.addPart("setContentRequest", setContentBody);
        multipartEntity.addPart("file", fileBody);
        return multipartEntity;
    }

    private List<ObjectSystemData> unwrapOsds(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<ObjectSystemData> osds = mapper.readValue(response.getEntity().getContent(), OsdWrapper.class).getOsds();
        if (expectedSize != null) {
            assertNotNull(osds);
            assertFalse(osds.isEmpty());
            assertThat(osds.size(), equalTo(expectedSize));
        }
        return osds;
    }

    private List<Link> unwrapLinks(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<Link> links = mapper.readValue(response.getEntity().getContent(), OsdWrapper.class).getLinks();
        if (expectedSize != null) {
            assertNotNull(links);
            assertFalse(links.isEmpty());
            assertThat(links.size(), equalTo(expectedSize));
        }
        return links;
    }

}
