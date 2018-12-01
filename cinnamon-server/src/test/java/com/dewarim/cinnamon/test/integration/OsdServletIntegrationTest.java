package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import nu.xom.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.*;
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
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals(new ObjectSystemData().getSummary())));
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
    public void getContentHappyPath() throws IOException {
        createTestContentOnOsd(22L, false);

        IdRequest    idRequest = new IdRequest(22L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertResponseOkay(response);
        Header contentType = response.getFirstHeader(CONTENT_TYPE);
        assertEquals(APPLICATION_XML.getMimeType(), contentType.getValue());
        File             tempFile = Files.createTempFile("cinnamon-test-get-content-", ".xml").toFile();
        FileOutputStream fos      = new FileOutputStream(tempFile);
        response.getEntity().writeTo(fos);
        fos.close();
        String sha256Hex = DigestUtils.sha256Hex(new FileInputStream(tempFile));

        OsdRequest             osdRequest       = new OsdRequest(List.of(22L), false);
        HttpResponse           osdResponse      = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> objectSystemData = unwrapOsds(osdResponse, 1);
        ObjectSystemData       osd              = objectSystemData.get(0);
        assertEquals(osd.getContentHash(), sha256Hex);
    }

    @Test
    public void getContentWithoutReadPermission() throws IOException {
        createTestContentOnOsd(24L, true);

        IdRequest    idRequest = new IdRequest(24L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.NO_READ_PERMISSION, SC_FORBIDDEN);
    }

    @Test
    public void getContentWithoutInvalidRequest() throws IOException {
        IdRequest    idRequest = new IdRequest(0L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getContentWithoutValidObject() throws IOException {
        IdRequest    idRequest = new IdRequest(Long.MAX_VALUE);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getContentWithoutContent() throws IOException {
        IdRequest    idRequest = new IdRequest(25L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_HAS_NO_CONTENT, SC_NOT_FOUND);
    }

    @Test
    public void setContentWithDefaultContentProviderHappyPath() throws IOException {
        createTestContentOnOsd(22L, false);

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

    @Test
    public void lockAndUnlockObject() throws IOException {
        IdRequest    idRequest    = new IdRequest(26L);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void lockTwice() throws IOException {
        IdRequest    idRequest    = new IdRequest(26L);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);
        lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);

        // cleanup
        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void unlockTwice() throws IOException {
        IdRequest    idRequest      = new IdRequest(26L);
        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
        unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void overwriteOtherUsersLockShouldFail() throws IOException {
        // first, make sure it is unlocked:
        IdRequest    idRequest      = new IdRequest(26L);
        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);

        // lock by admin:
        HttpResponse lockResponse = sendAdminRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);

        // try to overwrite admin's lock:
        lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER, SC_FORBIDDEN);

        // cleanup:
        unlockResponse = sendAdminRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void unlockOtherUsersLockShouldFail() throws IOException {
        // lock by first user:
        IdRequest    idRequest    = new IdRequest(26L);
        HttpResponse lockResponse = sendAdminRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);

        // try to unlock other user's lock:
        lockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER, SC_FORBIDDEN);

        // cleanup:
        HttpResponse unlockResponse = sendAdminRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void lockAndUnlockShouldFailWithInvalidRequest() throws IOException {
        IdRequest    idRequest    = new IdRequest(0L);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.INVALID_REQUEST);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(unlockResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void lockAndUnlockShouldFailWithoutPermission() throws IOException {
        IdRequest    idRequest    = new IdRequest(27L);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.NO_LOCK_PERMISSION, SC_FORBIDDEN);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(unlockResponse, ErrorCode.NO_LOCK_PERMISSION, SC_FORBIDDEN);
    }

    @Test
    public void lockAndUnlockShouldFailWithNonExistantObject() throws IOException {
        IdRequest    idRequest    = new IdRequest(Long.MAX_VALUE);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(unlockResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void superuserHasMasterKeyForUnlock() throws IOException {
        IdRequest    idRequest    = new IdRequest(25L);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);

        HttpResponse unlockResponse = sendAdminRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    @Test
    public void getMetaInvalidRequest() throws IOException {
        MetaRequest  request      = new MetaRequest();
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getMetaObjectNotFound() throws IOException {
        MetaRequest  request      = new MetaRequest(Long.MAX_VALUE, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getMetaWithoutReadPermission() throws IOException {
        MetaRequest  request      = new MetaRequest(37L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void getMetaCompatibilityMode() throws IOException, ParsingException {
        MetaRequest request = new MetaRequest(36L, null);
        request.setVersion3CompatibilityRequired(true);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), Charset.forName("UTF-8"));
        Document metaDoc = new Builder().build(content, null);
        Nodes    nodes   = metaDoc.query("/meta/metaset[@type='comment']/p");
        Node     node    = nodes.get(0);
        assertEquals("Good Test", node.getValue());
    }

    @Test
    public void getMetaHappyPathAllMeta() throws IOException, ParsingException {
        MetaRequest  request      = new MetaRequest(36L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), Charset.forName("UTF-8"));
        Document metaDoc = new Builder().build(content, null);
        Node     comment = metaDoc.query("//metasets/metaset[typeId/text()='1']/content").get(0);
        assertEquals("<metaset><p>Good Test</p></metaset>", comment.getValue());
        Node license = metaDoc.query("//metasets/metaset[typeId/text()='2']/content").get(0);
        assertEquals("<metaset><license>GPL</license></metaset>", license.getValue());
    }

    @Test
    public void getMetaHappyPathSingleMeta() throws IOException, ParsingException {
        MetaRequest  request      = new MetaRequest(36L, Collections.singletonList("license"));
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content  = new String(metaResponse.getEntity().getContent().readAllBytes(), Charset.forName("UTF-8"));
        Document metaDoc  = new Builder().build(content, null);
        Nodes    metasets = metaDoc.query("//metasets/metaset");
        assertEquals(1, metasets.size());
    }

    @Test
    public void createMetaInvalidRequest() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest();
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createMetaObjectNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(Long.MAX_VALUE, "foo", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaObjectNotWritable() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(37L, "foo", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "foo", Long.MAX_VALUE);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetTypeByNameNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "foo", "unknown");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(39L, "duplicate license", "license");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS, SC_BAD_REQUEST);
    }

    // non-unique metasetType should allow appending new metasets.
    @Test
    public void createMetaMetasetHappyWithExistingMeta() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(40L, "duplicate comment", "comment");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertResponseOkay(metaResponse);
        MetaRequest  metaRequest     = new MetaRequest(40L, Collections.singletonList("comment"));
        HttpResponse commentResponse = sendStandardRequest(UrlMapping.OSD__GET_META, metaRequest);
        assertResponseOkay(commentResponse);
        MetaWrapper metaWrapper = mapper.readValue(commentResponse.getEntity().getContent(), MetaWrapper.class);
        assertEquals(2, metaWrapper.getMetasets().size());
    }

    @Test
    public void createMetaMetasetHappyPath() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "new license meta", "license");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertResponseOkay(metaResponse);
        List<Meta> metas = unwrapMeta(metaResponse, 1);
        Meta       meta  = metas.get(0);
        assertEquals("new license meta", meta.getContent());
        assertEquals(2, meta.getTypeId().longValue());
    }

    @Test
    public void deleteMetaInvalidRequest() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest();
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELEET_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteMetaObjectNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE, 1L);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELEET_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(42L, "license");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELEET_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(41L, "unknown-type");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELEET_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        DeleteMetaRequest request  = new DeleteMetaRequest(41L, 7L);
        HttpResponse      response = sendStandardRequest(UrlMapping.OSD__DELEET_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
    }

    @Test
    public void deleteMetaHappyPathByName() throws IOException {
        DeleteMetaRequest request  = new DeleteMetaRequest(41L, "comment");
        HttpResponse      response = sendStandardRequest(UrlMapping.OSD__DELEET_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
    }

    private HttpResponse sendStandardMultipartRequest(String url, MultipartEntity multipartEntity) throws IOException {
        return Request.Post(url)
                .addHeader("ticket", getDoesTicket(false))
                .body(multipartEntity).execute().returnResponse();
    }

    private HttpResponse sendAdminMultipartRequest(String url, MultipartEntity multipartEntity) throws IOException {
        return Request.Post(url)
                .addHeader("ticket", getAdminTicket())
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

    private List<Meta> unwrapMeta(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<Meta> metas = mapper.readValue(response.getEntity().getContent(), MetaWrapper.class).getMetasets();
        if (expectedSize != null) {
            assertNotNull(metas);
            assertFalse(metas.isEmpty());
            assertThat(metas.size(), equalTo(expectedSize));
        }
        return metas;
    }

    private void createTestContentOnOsd(Long osdId, boolean asSuperuser) throws IOException {
        SetContentRequest setContentRequest  = new SetContentRequest(22L, 1L);
        MultipartEntity   multipartEntity    = createMultipartEntity(setContentRequest);
        HttpResponse      setContentResponse = null;
        if (asSuperuser) {
            setContentResponse = sendAdminMultipartRequest(SET_CONTENT_URL, multipartEntity);
        } else {
            setContentResponse = sendStandardMultipartRequest(SET_CONTENT_URL, multipartEntity);
        }
        assertResponseOkay(setContentResponse);
    }

    public ObjectSystemData fetchSingleOsd(Long id) throws IOException {
        OsdRequest   osdRequest  = new OsdRequest(Collections.singletonList(id), true);
        HttpResponse osdResponse = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        assertResponseOkay(osdResponse);
        return unwrapOsds(osdResponse, 1).get(0);
    }

}
