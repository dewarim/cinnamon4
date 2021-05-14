package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.NOT_MULTIPART_UPLOAD;
import static com.dewarim.cinnamon.api.Constants.CREATE_NEW_VERSION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OsdServletIntegrationTest extends CinnamonIntegrationTest {

    private static final Long CREATE_ACL_ID                     = 8L;
    /**
     * Non-Admin user id
     */
    private static final Long STANDARD_USER_ID                  = 2L;
    private static final Long PLAINTEXT_FORMAT_ID               = 2L;
    private static final Long DEFAULT_OBJECT_TYPE_ID            = 1L;
    /**
     * This folder's ACL does not permit object creation inside.
     */
    private static final Long NO_CREATE_FOLDER_ID               = 8L;
    /**
     * This folder's ACL allows object creation.
     */
    private static final Long CREATE_FOLDER_ID                  = 6L;
    private static final Long NEW_RENDERTASK_LIFECYCLE_STATE_ID = 1L;
    private static final Long GERMAN_LANGUAGE_ID                = 1L;


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
        assertThat(wrapper.getSummaries().get(0).getContent(), equalTo("a summary"));
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(18L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(16L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.OSD__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper   = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        List<Summary>  summaries = wrapper.getSummaries();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(wrapper.getSummaries().get(0).getContent(), equalTo("<sum>7</sum>"));
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
    public void setContentWithoutLockingOsd() throws IOException {
        SetContentRequest setContentRequest  = new SetContentRequest(43L, 1L);
        HttpEntity        multipartEntity    = createMultipartEntityWithFileBody("setContentRequest", setContentRequest);
        HttpResponse      setContentResponse = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        assertCinnamonError(setContentResponse, ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER);
    }

    @Test
    public void getContentWithoutReadPermission() throws IOException {
        IdRequest    idRequest = new IdRequest(24L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.NO_READ_PERMISSION);
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
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getContentWithoutContent() throws IOException {
        IdRequest    idRequest = new IdRequest(25L);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_HAS_NO_CONTENT);
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
        assertCinnamonError(response, NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void setContentWithoutProperRequest() throws IOException {
        File     pomXml   = getPomXml();
        FileBody fileBody = new FileBody(pomXml);
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .addPart("file", fileBody).build();
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    private File getPomXml() {
        return new File("pom.xml");
    }

    @Test
    public void setContentWithoutFile() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(22L, 1L);
        StringBody        setContentBody = new StringBody(mapper.writeValueAsString(contentRequest), APPLICATION_XML);
        HttpEntity multipartEntity = MultipartEntityBuilder.create().
                addPart("setContentRequest", setContentBody).build();
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        assertCinnamonError(response, ErrorCode.MISSING_FILE_PARAMETER);
    }

    @Test
    public void setContentWithInvalidParameters() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(-1L, 0L);
        HttpResponse      response       = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, createMultipartEntityWithFileBody("setContentRequest", contentRequest));
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void setContentWithUnknownOsdId() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(Long.MAX_VALUE, 1L);
        HttpResponse      response       = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, createMultipartEntityWithFileBody("setContentRequest", contentRequest));
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void setContentWithUnknownFormatId() throws IOException {
        Long id = 22L;
        lockOsd(id);
        SetContentRequest contentRequest = new SetContentRequest(id, Long.MAX_VALUE);
        HttpResponse      response       = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, createMultipartEntityWithFileBody("setContentRequest", contentRequest));
        assertCinnamonError(response, ErrorCode.FORMAT_NOT_FOUND);
        unLockOsd(id);
    }

    @Test
    public void setContentWithoutWritePermission() throws IOException {
        SetContentRequest contentRequest = new SetContentRequest(23L, 1L);
        HttpResponse      response       = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, createMultipartEntityWithFileBody("setContentRequest", contentRequest));
        assertCinnamonError(response, ErrorCode.NO_WRITE_PERMISSION);
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
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER);

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
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER);

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
        assertCinnamonError(lockResponse, ErrorCode.NO_LOCK_PERMISSION);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(unlockResponse, ErrorCode.NO_LOCK_PERMISSION);
    }

    @Test
    public void lockAndUnlockShouldFailWithNonExistentObject() throws IOException {
        IdRequest    idRequest    = new IdRequest(Long.MAX_VALUE);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertCinnamonError(lockResponse, ErrorCode.OBJECT_NOT_FOUND);

        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertCinnamonError(unlockResponse, ErrorCode.OBJECT_NOT_FOUND);
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
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getMetaWithoutReadPermission() throws IOException {
        MetaRequest  request      = new MetaRequest(37L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void getMetaCompatibilityMode() throws IOException, ParsingException {
        MetaRequest request = new MetaRequest(36L, null);
        request.setVersion3CompatibilityRequired(true);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
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
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
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
        String   content  = new String(metaResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
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
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void createMetaObjectNotWritable() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(37L, "foo", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "foo", Long.MAX_VALUE);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetTypeByNameNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "foo", "unknown");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(39L, "duplicate license", "license");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS);
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
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteMetaObjectNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE, 1L);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(42L, "license");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(41L, "unknown-type");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        DeleteMetaRequest request  = new DeleteMetaRequest(41L, 7L);
        HttpResponse      response = sendStandardRequest(UrlMapping.OSD__DELETE_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
    }

    @Test
    public void deleteMetaHappyPathByName() throws IOException {
        DeleteMetaRequest request  = new DeleteMetaRequest(41L, "comment");
        HttpResponse      response = sendStandardRequest(UrlMapping.OSD__DELETE_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
    }

    @Test
    public void createOsdNoContentTypeInHeader() throws IOException {
        Request      request  = createStandardRequestHeader(UrlMapping.OSD__CREATE_OSD);
        HttpResponse response = request.execute().returnResponse();
        assertCinnamonError(response, ErrorCode.NO_CONTENT_TYPE_IN_HEADER);
    }

    @Test
    public void createOsdNotMultipartRequest() throws IOException {
        Request request = createStandardRequestHeader(UrlMapping.OSD__CREATE_OSD);
        HttpResponse response = request
                .addHeader("Content-Type", APPLICATION_XML.getMimeType())
                .execute().returnResponse();
        assertCinnamonError(response, NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void createOsdRequestWithoutPayload() throws IOException {
        HttpEntity   multipartEntity = MultipartEntityBuilder.create().build();
        HttpResponse response        = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, multipartEntity);
        assertCinnamonError(response, ErrorCode.MISSING_REQUEST_PAYLOAD);
    }

    // TODO: create tests from PARENT_FOLDER_NOT_FOUND onwards
    @Test
    public void createOsdInvalidRequest() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createOsdParentFolderNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(Long.MAX_VALUE);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void createOsdNoCreatePermission() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(NO_CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.NO_CREATE_PERMISSION);
    }

    @Test
    public void createOsdAclNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(Long.MAX_VALUE);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.ACL_NOT_FOUND);
    }

    @Test
    public void createOsdUserNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(Long.MAX_VALUE);
        request.setParentId(CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void createOsdObjectTypeNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(Long.MAX_VALUE);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.OBJECT_TYPE_NOT_FOUND);
    }

    @Test
    public void createOsdLifecycleStateNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        request.setLifecycleStateId(Long.MAX_VALUE);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void createOsdLanguageNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        request.setLifecycleStateId(NEW_RENDERTASK_LIFECYCLE_STATE_ID);
        request.setLanguageId(Long.MAX_VALUE);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.LANGUAGE_NOT_FOUND);
    }

    @Test
    public void createOsdHappyCaseNoFile() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertResponseOkay(response);
        List<ObjectSystemData> objectSystemData = unwrapOsds(response, 1);
        ObjectSystemData       osd              = objectSystemData.get(0);
        assertEquals("new osd", osd.getName());
        assertEquals(STANDARD_USER_ID, osd.getOwnerId());
        assertEquals(STANDARD_USER_ID, osd.getModifierId());
        assertEquals(CREATE_ACL_ID, osd.getAclId());
        assertEquals(DEFAULT_OBJECT_TYPE_ID, osd.getTypeId());
        assertEquals(CREATE_FOLDER_ID, osd.getParentId());
    }

    @Test
    public void createOsdUploadedFileWithoutFormat() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        request.setLifecycleStateId(NEW_RENDERTASK_LIFECYCLE_STATE_ID);
        request.setLanguageId(GERMAN_LANGUAGE_ID);
        File     pomXml   = new File("pom.xml");
        FileBody fileBody = new FileBody(pomXml);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.FORMAT_NOT_FOUND);
    }

    @Test
    public void createOsdUploadedFileHappyCase() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        request.setLifecycleStateId(NEW_RENDERTASK_LIFECYCLE_STATE_ID);
        request.setLanguageId(GERMAN_LANGUAGE_ID);
        request.setFormatId(PLAINTEXT_FORMAT_ID);
        File     pomXml   = new File("pom.xml");
        FileBody fileBody = new FileBody(pomXml);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertResponseOkay(response);
        List<ObjectSystemData> objectSystemData = unwrapOsds(response, 1);
        ObjectSystemData       osd              = objectSystemData.get(0);
        assertEquals(Long.valueOf(getPomXml().length()), osd.getContentSize());
        assertEquals(PLAINTEXT_FORMAT_ID, osd.getFormatId());
    }

    @Test
    public void versionWithStandardRequest() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.OSD__VERSION, null);
        assertCinnamonError(response, NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void versionNoContentTypeInHeader() throws IOException {
        Request      request  = createStandardRequestHeader(UrlMapping.OSD__VERSION);
        HttpResponse response = request.execute().returnResponse();
        assertCinnamonError(response, ErrorCode.NO_CONTENT_TYPE_IN_HEADER);
    }

    @Test
    public void versionWithInvalidRequest() throws IOException {
        CreateNewVersionRequest versionRequest  = new CreateNewVersionRequest(-1L);
        HttpEntity              request         = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse            versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        assertCinnamonError(versionResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void versionWithoutValidTarget() throws IOException {
        CreateNewVersionRequest versionRequest  = new CreateNewVersionRequest(Long.MAX_VALUE);
        HttpEntity              request         = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse            versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        assertCinnamonError(versionResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void versionWithoutVersionPermission() throws IOException {
        CreateNewVersionRequest versionRequest  = new CreateNewVersionRequest(44L);
        HttpEntity              request         = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse            versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        assertCinnamonError(versionResponse, ErrorCode.NO_VERSION_PERMISSION);
    }

    @Test
    public void versionWithBrokenMetaRequests() throws IOException {
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(45L);
        CreateNewVersionRequest.Metadata metadata       = new CreateNewVersionRequest.Metadata();
        metadata.setContent("");
        metadata.setTypeId(Long.MAX_VALUE);
        versionRequest.getMetaRequests().add(metadata);
        HttpEntity   entity          = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entity);
        assertCinnamonError(versionResponse, ErrorCode.METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void versionWithMetaRequests() throws IOException {
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(48L);
        CreateNewVersionRequest.Metadata metadata       = new CreateNewVersionRequest.Metadata();
        metadata.setContent("<comment>cool</comment>");
        metadata.setTypeId(1L);
        versionRequest.getMetaRequests().add(metadata);
        HttpEntity       entity          = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        HttpResponse     versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entity);
        ObjectSystemData version2        = unwrapOsds(versionResponse, 1).get(0);
        assertEquals("2", version2.getCmnVersion());
    }

    @Test
    public void versionWithoutMultipartContentType() throws IOException {
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(45L);
        CreateNewVersionRequest.Metadata metadata       = new CreateNewVersionRequest.Metadata();
        versionRequest.getMetaRequests().add(metadata);
        HttpResponse versionResponse = sendStandardRequest(UrlMapping.OSD__VERSION, versionRequest);
        assertCinnamonError(versionResponse, NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void happyVersionWithMultipartWihFileUploadAndLifecycleState() throws IOException {
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(45L);
        versionRequest.setFormatId(PLAINTEXT_FORMAT_ID);
        File     pomXml   = new File("pom.xml");
        FileBody fileBody = new FileBody(pomXml);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody(CREATE_NEW_VERSION, mapper.writeValueAsString(versionRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entityBuilder.build());
        assertResponseOkay(response);
        List<ObjectSystemData> objectSystemData = unwrapOsds(response, 1);
        ObjectSystemData       osd              = objectSystemData.get(0);
        assertEquals(Long.valueOf(getPomXml().length()), osd.getContentSize());
        assertEquals(PLAINTEXT_FORMAT_ID, osd.getFormatId());
    }

    @Test
    public void versionWithMultipartWihFileUploadButWithoutFormat() throws IOException {
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(45L);
        File                    pomXml         = new File("pom.xml");
        FileBody                fileBody       = new FileBody(pomXml);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody(CREATE_NEW_VERSION, mapper.writeValueAsString(versionRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entityBuilder.build());
        assertCinnamonError(response, ErrorCode.FORMAT_NOT_FOUND);
    }

    @Disabled("Currently no easy way to setup DB to ignore FK-constraint on lifecycle so we would get a missing LC exception")
    @Test
    public void versionWithLifecycleStateNotFoundError() {
        // so it should not be possible for this to happen unless the DB tables (or DAO) are broken.
        throw new IllegalStateException("implementation pending");
    }

    @Disabled("Currently no easy way to setup DB fail to generate Internal Server Error")
    @Test
    public void versionWithInternalServerErrorWhenCreatingMetaset() {
        // this can only happen when DB cannot insert a new Metaset for an object. (or DAO is broken)
        throw new IllegalStateException("implementation pending");
    }

    @Test
    public void testVersionNumbering() throws IOException {
        ObjectSystemData initialVersion = fetchSingleOsd(47L);
        assertEquals("1", initialVersion.getCmnVersion());
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(initialVersion.getId());
        HttpEntity              request        = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        HttpResponse            response       = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        ObjectSystemData        v2             = unwrapOsds(response, 1).get(0);

        assertEquals("2", v2.getCmnVersion());
        versionRequest.setId(v2.getId());
        request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);

        ObjectSystemData v3 = unwrapOsds(response, 1).get(0);
        assertEquals("3", v3.getCmnVersion());

        // create another version of v2
        response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        ObjectSystemData v2Branch = unwrapOsds(response, 1).get(0);
        assertEquals("2.1-1", v2Branch.getCmnVersion());

        // create next version in branch 2
        versionRequest.setId(v2Branch.getId());
        request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        ObjectSystemData v2Branchv2 = unwrapOsds(response, 1).get(0);
        assertEquals("2.1-2", v2Branchv2.getCmnVersion());

        // create next version of v2 (second parallel branch)
        versionRequest.setId(v2.getId());
        request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        ObjectSystemData v2parallelBranch = unwrapOsds(response, 1).get(0);
        assertEquals("2.2-1", v2parallelBranch.getCmnVersion());

        // create branch of 1st branch of v2
        versionRequest.setId(v2Branch.getId());
        request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        response = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request);
        ObjectSystemData branchOfBranch = unwrapOsds(response, 1).get(0);
        assertEquals("2.1-1.1-1", branchOfBranch.getCmnVersion());
    }

    @Test
    public void versionWithFailingLifecycleStateChange() throws IOException {
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(46L);
        versionRequest.setFormatId(PLAINTEXT_FORMAT_ID);
        HttpEntity   entity          = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entity);
        assertCinnamonError(versionResponse, ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED);
    }

    @Test
    public void updateOsdWithChangeTracking() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(CREATE_FOLDER_ID);
        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
        assertResponseOkay(response);
        ObjectSystemData osd = unwrapOsds(response, 1).get(0);
        assertThat(osd.getModifierId(), notNullValue());
        assertThat(osd.getModified(), notNullValue());

        // admin without changeTracking
        SetSummaryRequest summaryRequest = new SetSummaryRequest(osd.getId(), "a summary");
        response = sendAdminRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);
        OsdRequest osdRequest = new OsdRequest(Collections.singletonList(osd.getId()), false);
        response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        ObjectSystemData updatedOsd = unwrapOsds(response, 1).get(0);
        assertThat(updatedOsd.getModifierId(), equalTo(osd.getModifierId()));
        assertThat(updatedOsd.getModified(), equalTo(osd.getModified()));

        // standard user should have changeTracking
        response = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);
        response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        updatedOsd = unwrapOsds(response, 1).get(0);
        assertThat(updatedOsd.getModifierId(), equalTo(osd.getModifierId()));
        assertThat(updatedOsd.getModified(), not(equalTo(osd.getModified())));
    }

    @Test
    public void deleteOsdHappyPath() throws IOException {
        client.deleteOsd(49L);
    }

//    @Test
//    public void deleteOsdsNoDeletePermission() throws IOException{
//
//        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(Collections.singletonList(49L));
//        HttpResponse           response = sendStandardRequest(UrlMapping.OSD__DELETE_OSDS, deleteRequest);
//        assertCinnamonError(response, ErrorCode.NO_DELETE_PERMISSION);
//    }

    // TODO: deleteOsdWithDescendantsHappyPath
    // TODO: deleteOsdWithDescendantsFails
    // TODO: deleteOsdWithProtectedRelations

    private HttpResponse sendAdminMultipartRequest(UrlMapping url, HttpEntity multipartEntity) throws IOException {
        return Request.Post("http://localhost:" + cinnamonTestPort + url.getPath())
                .addHeader("ticket", getAdminTicket())
                .body(multipartEntity).execute().returnResponse();
    }

    private HttpResponse sendStandardMultipartRequest(UrlMapping urlMapping, HttpEntity multipartEntity) throws IOException {
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", getDoesTicket(false))
                .body(multipartEntity).execute().returnResponse();
    }

    private HttpEntity createSimpleMultipartEntity(String fieldname, Object contentRequest) throws IOException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody(fieldname, mapper.writeValueAsString(contentRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return entityBuilder.build();
    }

    private HttpEntity createMultipartEntityWithFileBody(String fieldname, Object contentRequest) throws IOException {
        File     pomXml   = new File("pom.xml");
        FileBody fileBody = new FileBody(pomXml);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody(fieldname, mapper.writeValueAsString(contentRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8))
                .addPart("file", fileBody);
        return entityBuilder.build();
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
        // lock before setContent:
        lockOsd(osdId);

        SetContentRequest setContentRequest = new SetContentRequest(osdId, 1L);
        HttpEntity        multipartEntity   = createMultipartEntityWithFileBody("setContentRequest", setContentRequest);
        HttpResponse      setContentResponse;
        if (asSuperuser) {
            setContentResponse = sendAdminMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        } else {
            setContentResponse = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        }
        assertResponseOkay(setContentResponse);
        unLockOsd(osdId);
    }

    private void lockOsd(Long osdId) throws IOException {
        IdRequest    idRequest    = new IdRequest(osdId);
        HttpResponse lockResponse = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        assertResponseOkay(lockResponse);
    }

    private void unLockOsd(Long osdId) throws IOException {
        IdRequest    idRequest      = new IdRequest(osdId);
        HttpResponse unlockResponse = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        assertResponseOkay(unlockResponse);
    }

    public ObjectSystemData fetchSingleOsd(Long id) throws IOException {
        OsdRequest   osdRequest  = new OsdRequest(Collections.singletonList(id), true);
        HttpResponse osdResponse = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        assertResponseOkay(osdResponse);
        return unwrapOsds(osdResponse, 1).get(0);
    }

}
