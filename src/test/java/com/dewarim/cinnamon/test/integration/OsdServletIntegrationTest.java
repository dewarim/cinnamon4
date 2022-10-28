package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
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
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.ibatis.parsing.ParsingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.CREATE_NEW_VERSION;
import static com.dewarim.cinnamon.model.request.osd.VersionPredicate.*;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.apache.http.entity.mime.MIME.CONTENT_DISPOSITION;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OsdServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(OsdServletIntegrationTest.class);

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
        OsdByFolderRequest     osdRequest = new OsdByFolderRequest(4L, true, false, false, ALL);
        HttpResponse           response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        List<ObjectSystemData> dataList   = unwrapOsds(response, 2);
        List<Link>             links      = unwrapLinks(response, 1);
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals("<summary>child@archive</summary>")));
        // link.objectId is #10, because it's yet unresolved (latest_head would be osd#11).
        assertThat(links.get(0).getObjectId(), equalTo(10L));
    }

    @Test
    public void getObjectsByFolderIdOnlyHead() throws IOException {
        var holder = new TestObjectHolder(client);
        holder.setAcl(client.getAclByName("creators.acl"))
                .setUser(userId)
                .createFolder("only-head", createFolderId)
                .createOsd("get-objects-by-folder-only-head");
        ObjectSystemData       version1     = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        ObjectSystemData       head         = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData       branch       = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        List<ObjectSystemData> osdsInFolder = client.getOsdsInFolder(holder.folder.getId(), false, false, false, HEAD).getOsds();
        assertEquals(1, osdsInFolder.size());
        assertEquals(head, osdsInFolder.get(0));
    }

    @Test
    public void getObjectsByFolderIdOnlyBranches() throws IOException {
        var holder = new TestObjectHolder(client);
        holder.setAcl(client.getAclByName("creators.acl"))
                .setUser(userId)
                .createFolder("getObjectsByFolderIdOnlyBranches", createFolderId)
                .createOsd("get-objects-by-folder-branch");
        ObjectSystemData       version1     = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        ObjectSystemData       head         = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData       branch       = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        List<ObjectSystemData> osdsInFolder = client.getOsdsInFolder(holder.folder.getId(), false, false, false, BRANCH).getOsds();
        assertEquals(2, osdsInFolder.size());
        // head object is also considered a branch (for legacy reasons):
        assertTrue(osdsInFolder.contains(head));
        assertTrue(osdsInFolder.contains(branch));
    }

    @Test
    public void osdDateFieldsAreFormattedAsIso8601() throws IOException, ParseException {
        var holder = new TestObjectHolder(client);
        holder.setAcl(client.getAclByName("creators.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("osdDateFieldsAreFormattedAsIso8601");
        var osd = holder.osd;
        var response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID,
                new OsdRequest(List.of(osd.getId()), false, false));
        String           osdResponse      = new String(response.getEntity().getContent().readAllBytes(), Charset.defaultCharset());
        String           createdTimestamp = osdResponse.split("</?created>")[1];
        SimpleDateFormat df               = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date             created          = df.parse(createdTimestamp);
        // and that's the reason we want to migrate to LocalDate/Time (#228): Java will happily parse the date in
        // the _current_ timezone (so created vs osd.getCreated is off by 2 hours for CEST)
        assertEquals(created.getTime(), osd.getCreated().getTime());
        log.debug("createdTimestamp: " + createdTimestamp);
        String modifiedTimestamp = osdResponse.split("</?modified>")[1];
        Date   modified          = df.parse(modifiedTimestamp);
        assertEquals(modified.getTime(), osd.getModified().getTime());
        log.debug("modifiedTimestamp: " + modifiedTimestamp);
    }

    @Test
    public void getObjectsByFolderIdWithLinksAsOsds() throws IOException {
        OsdWrapper             wrapper    = client.getOsdsInFolderWrapped(4L, true, true, false);
        List<Link>             links      = wrapper.getLinks();
        List<ObjectSystemData> linkedOsds = wrapper.getReferences();
        assertEquals(1, links.size());
        links.forEach(link -> assertTrue(linkedOsds.stream().anyMatch(osd -> osd.getId().equals(link.getObjectId()))));
    }

    @Test
    public void createObjectWithCustomMetadata() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas       = List.of(new Meta(null, metasetType.getId(), "<meta>some data</meta>"));
        var osd         = holder.setMetas(metas).createOsd("object#1 with custom meta").osd;
        assertEquals(osd.getMetas().get(0).getContent(), holder.metas.get(0).getContent());
        var osdWithMeta = client.getOsdById(osd.getId(), false, true);
        assertEquals(1, osdWithMeta.getMetas().size());
        assertEquals(osd.getMetas().get(0), osdWithMeta.getMetas().get(0));
    }

    @Test
    public void createObjectWithNonUniqueMetasets() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas = List.of(new Meta(null, metasetType.getId(), "<meta><comment>first post</comment></meta>"),
                new Meta(null, metasetType.getId(), "<meta><comment>second post</comment></meta>"));
        var osd = holder.setMetas(metas).createOsd("object#2 with custom meta").osd;
        assertEquals(osd.getMetas().get(0).getContent(), holder.metas.get(0).getContent());
        var osdWithMeta = client.getOsdById(osd.getId(), false, true);
        assertEquals(2, osdWithMeta.getMetas().size());
        assertEquals(osd.getMetas().get(0), osdWithMeta.getMetas().get(0));
        assertEquals(osd.getMetas().get(1), osdWithMeta.getMetas().get(1));
    }

    @Test
    public void createObjectWithMultipelUniqueMetasets() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        // license is a unique metaset in our test database
        var metasetType = TestObjectHolder.getMetasetType("license");
        var metas = List.of(new Meta(null, metasetType.getId(), "<gpl/>"),
                new Meta(null, metasetType.getId(), "<proprietaryLicense/>"));
        assertClientError(() -> holder.setMetas(metas).createOsd("object#3 with custom meta"), METASET_UNIQUE_CHECK_FAILED);
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
        Header           contentDisposition = response.getFirstHeader(CONTENT_DISPOSITION);
        ObjectSystemData osd                = client.getOsdById(22L, false, false);
        assertEquals("attachment; filename=\"" + osd.getName() + "\"", contentDisposition.getValue());
        File tempFile = Files.createTempFile("cinnamon-test-get-content-", ".xml").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            response.getEntity().writeTo(fos);
        }
        String sha256Hex = DigestUtils.sha256Hex(new FileInputStream(tempFile));
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
        client.lockOsd(id);
        SetContentRequest contentRequest = new SetContentRequest(id, Long.MAX_VALUE);
        HttpResponse      response       = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, createMultipartEntityWithFileBody("setContentRequest", contentRequest));
        assertCinnamonError(response, ErrorCode.FORMAT_NOT_FOUND);
        client.unlockOsd(id);
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
    public void getMetaHappyPathAllMeta() throws IOException {
        MetaRequest  request  = new MetaRequest(36L, null);
        List<Meta>   osdMetas = client.getOsdMetas(36L);
        List<String> content  = osdMetas.stream().map(Meta::getContent).toList();
        assertTrue(content.contains("<metaset><p>Good Test</p></metaset>"));
        assertTrue(content.contains("<metaset><license>GPL</license></metaset>"));
    }

    @Test
    public void getMetaHappyPathSingleMeta() throws IOException, ParsingException {
        List<Meta> osdMetas = client.getOsdMetas(36L, List.of(2L));
        assertEquals(1, osdMetas.size());
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
        CreateMetaRequest request      = new CreateMetaRequest(38L, "foo", Long.MAX_VALUE);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(39L, "duplicate license", 2L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS);
    }

    // non-unique metasetType should allow appending new metasets.
    @Test
    public void createMetaMetasetHappyWithExistingMeta() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(40L, "duplicate comment", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertResponseOkay(metaResponse);
        MetaRequest  metaRequest     = new MetaRequest(40L, Collections.singletonList(1L));
        HttpResponse commentResponse = sendStandardRequest(UrlMapping.OSD__GET_META, metaRequest);
        assertResponseOkay(commentResponse);
        MetaWrapper metaWrapper = mapper.readValue(commentResponse.getEntity().getContent(), MetaWrapper.class);
        assertEquals(2, metaWrapper.getMetasets().size());
    }

    @Test
    public void createMetaMetasetHappyPath() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(38L, "new license meta", 2L);
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
    public void deleteMetaWithoutPermission() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(8L);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        client.deleteOsdMeta(7L);
    }

    @Test
    public void deleteAllMetas() throws IOException {
        Long id = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId)
                .createOsd("delete-all-metas").osd.getId();
        client.createOsdMeta(new CreateMetaRequest(id, "...", 1L));
        client.createOsdMeta(new CreateMetaRequest(id, "...", 1L));
        client.deleteAllOsdMeta(id);
        assertEquals(0, client.getOsdMetas(id).size());
    }

    @Test
    public void deleteAllVersionsHappyPath() throws IOException {
        var holder = new TestObjectHolder(client);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("delete-all-versions-root");
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData branch   = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        client.deleteOsd(version2.getId(), true, true);
        assertTrue(client.getOsds(List.of(holder.osd.getId(), version1.getId(), version2.getId(), branch.getId()), false, false).isEmpty());
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
    public void versionWithCopyOnLeftRelation() throws IOException {
        addUserToAclGroupWithPermissions("versionWithCopyOnLeftRelation", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, VERSION_OBJECT));
        var relationType = adminClient.createRelationType(new RelationType("clone-on-left-version",
                false, false, false, false, true, false));
        var toh             = new TestObjectHolder(client, "versionWithCopyOnLeftRelation", userId, createFolderId);
        var leftOsd         = toh.createOsd("leftOsd").osd;
        var rightOsd        = toh.createOsd("rightOsd").osd;
        var relation        = client.createRelation(leftOsd.getId(), rightOsd.getId(), relationType.getId(), "<meta/>");
        var leftVersion     = client.version(new CreateNewVersionRequest(leftOsd.getId()));
        var copiedRelations = client.getRelations(List.of(leftVersion.getId()));
        assertEquals(1, copiedRelations.size());
        var relationCopy = copiedRelations.get(0);
        assertEquals(relation.getTypeId(), relationCopy.getTypeId());
        assertEquals(relation.getRightId(), relationCopy.getRightId());
        assertEquals(leftVersion.getId(), relationCopy.getLeftId());
    }

    @Test
    public void versionWithCopyOnRightRelation() throws IOException {
        addUserToAclGroupWithPermissions("versionWithCopyOnRightRelation", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, VERSION_OBJECT));
        var relationType = adminClient.createRelationType(new RelationType("clone-on-right-version",
                false, false, false, false, false, true));
        var toh             = new TestObjectHolder(client, "versionWithCopyOnRightRelation", userId, createFolderId);
        var leftOsd         = toh.createOsd("leftOsd-version").osd;
        var rightOsd        = toh.createOsd("rightOsd-version").osd;
        var relation        = client.createRelation(leftOsd.getId(), rightOsd.getId(), relationType.getId(), "<meta/>");
        var rightVersion    = client.version(new CreateNewVersionRequest(rightOsd.getId()));
        var copiedRelations = client.getRelations(List.of(rightVersion.getId()));
        assertEquals(1, copiedRelations.size());
        var relationCopy = copiedRelations.get(0);
        assertEquals(relation.getTypeId(), relationCopy.getTypeId());
        assertEquals(relation.getLeftId(), relationCopy.getLeftId());
        assertEquals(rightVersion.getId(), relationCopy.getRightId());
    }

    @Test
    public void updateOsdWithChangeTracking() throws IOException, InterruptedException {
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

        // TODO: maybe configure tests with Thread.sleep() to only run on demand or for a "all tests" szenario
        Thread.sleep(1000);

        // admin without changeTracking
        SetSummaryRequest summaryRequest = new SetSummaryRequest(osd.getId(), "a summary");
        response = sendAdminRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);
        OsdRequest osdRequest = new OsdRequest(Collections.singletonList(osd.getId()), false, false);
        response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        ObjectSystemData updatedOsd = unwrapOsds(response, 1).get(0);
        assertThat(updatedOsd.getModifierId(), equalTo(osd.getModifierId()));
        assertThat(updatedOsd.getModified(), equalTo(osd.getModified()));

        Thread.sleep(1000);

        // standard user should have changeTracking
        response = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);
        response = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        updatedOsd = unwrapOsds(response, 1).get(0);
        assertThat(updatedOsd.getModifierId(), equalTo(osd.getModifierId()));
        assertThat(updatedOsd.getModified(), not(equalTo(osd.getModified())));
    }

    @Test
    public void updateOsdInvalidRequest() {
        UpdateOsdRequest request = new UpdateOsdRequest();
        assertClientError(() -> client.updateOsd(request), INVALID_REQUEST);
    }

    @Test
    public void updateOsdWithOsdNotFound() {
        UpdateOsdRequest request = new UpdateOsdRequest(Long.MAX_VALUE, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), OBJECT_NOT_FOUND);
    }

    @Test
    public void updateOsdWithoutWriteSysMetaPermission() throws IOException {
        var holder = new TestObjectHolder(adminClient, "no-permissions.acl", adminId, createFolderId);

        holder.createAcl("updateOsdWithoutWriteSysMetaPermission")
                .createGroup("test-updateOsdWithoutWriteSysMetaPermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK))
                .addUserToGroup(userId)
                .createOsd("osd-update-forbidden");

        var id = holder.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void updateOsdWithParentFolderNotFound() throws IOException {
        var holder = new TestObjectHolder(adminClient, "no-permissions.acl", adminId, createFolderId);

        holder.createAcl("updateOsdWithParentFolderNotFound")
                .createGroup("test-updateOsdWithParentFolderNotFound")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK, WRITE_OBJECT_SYS_METADATA))
                .addUserToGroup(userId)
                .createOsd("osd-new-parent-missing");

        var id = holder.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, Long.MAX_VALUE, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void updateOsdWithoutCreateInFolderPermission() throws IOException {
        var holder = new TestObjectHolder(adminClient, "no-permissions.acl", adminId, createFolderId);

        holder.createAcl("updateOsdWithoutCreateInFolderPermission")
                .createGroup("test-updateOsdWithoutCreateInFolderPermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK, WRITE_OBJECT_SYS_METADATA))
                .addUserToGroup(userId)
                .createOsd("update-osd-no-create-permission");

        var id = holder.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_CREATE_PERMISSION);
    }

    @Test
    public void updateOsdWithoutMovePermission() throws IOException {
        var holder = new TestObjectHolder(adminClient, "no-permissions.acl", adminId, createFolderId);

        holder.createAcl("updateOsdWithoutMovePermission")
                .createGroup("test-updateOsdWithoutMovePermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK, WRITE_OBJECT_SYS_METADATA))
                .addUserToGroup(userId)
                .createOsd("update-osd-no-move-permission");

        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, createFolderId, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_MOVE_PERMISSION);
    }

    @Test
    public void updateOsdWithMovePermission() throws IOException {
        var holder = new TestObjectHolder(adminClient, "no-permissions.acl", adminId, createFolderId);

        holder.createAcl("updateOsdWithMovePermission")
                .createGroup("test-updateOsdWithMovePermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, BROWSE_FOLDER, LOCK, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .addUserToGroup(userId)
                .createOsd("update-osd-move-permission")
                .createFolder("target-of-update-osd-by-move", createFolderId);

        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, holder.folder.getId(), null, null, null, null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(holder.folder.getId(), osd.getParentId());
    }

    @Test
    public void updateOsdChangeName() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", adminId, createFolderId);

        holder.createOsd("update-osd-rename")
                .lockOsd();
        var id = holder.osd.getId();

        var request = new UpdateOsdRequest(id, null, "new name", null, null, null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals("new name", osd.getName());
    }

    @Test
    public void updateOsdChangeTypeNotFound() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", adminId, createFolderId);

        holder.createOsd("update-osd-rename")
                .lockOsd();
        var id = holder.osd.getId();

        var request = new UpdateOsdRequest(id, null, null, null, null, Long.MAX_VALUE, null);
        assertClientError(() -> client.updateOsd(request), OBJECT_TYPE_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeType() throws IOException {
        var holder = new TestObjectHolder(adminClient, "reviewers.acl", adminId, createFolderId);

        holder.createOsd("update-osd-rename")
                .createObjectType("update-osd-type");

        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, null,
                holder.objectType.getId(), null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(holder.objectType.getId(), osd.getTypeId());
    }

    @Test
    public void updateOsdChangeAclNoPermission() throws IOException {
        var holder = new TestObjectHolder(adminClient, "reviewers.acl", adminId, createFolderId);

        holder.createAcl("updateOsdChangeAclNoPermission")
                .createGroup("test-updateOsdChangeAclNoPermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .addUserToGroup(userId)
                .createOsd("update-osd-updateOsdChangeAclNoPermission");

        holder.createOsd("updateOsdChangeAclNoPermission");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, holder.acl.getId(), null, null);
        assertClientError(() -> client.updateOsd(request), MISSING_SET_ACL_PERMISSION);
    }

    @Test
    public void updateOsdChangeAclNotFound() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("updateOsdChangeAclNoPermission");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, Long.MAX_VALUE, null, null);
        assertClientError(() -> client.updateOsd(request), ACL_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeAcl() throws IOException {
        var holder = new TestObjectHolder(adminClient, "reviewers.acl", adminId, createFolderId);
        holder.createAcl("updateOsdChangeAcl")
                .createGroup("test-updateOsdChangeAcl")
                .createAclGroup()
                .addPermissions(List.of(BROWSE_OBJECT, LOCK, SET_ACL, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .addUserToGroup(userId)
                .createOsd("update-osd-updateOsdChangeAcl");

        holder.createOsd("updateOsdChangeAclNoPermission");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, holder.acl.getId(), null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(holder.acl.getId(), osd.getAclId());
    }

    @Test
    public void updateOsdChangeUserNotFound() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("updateOsdChangeUserNotFound");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, Long.MAX_VALUE, null, null, null);
        assertClientError(() -> client.updateOsd(request), USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeUser() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("updateOsdChangeUserNotFound");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, adminId, null, null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(adminId, osd.getOwnerId());
    }

    @Test
    public void updateOsdChangeLanguageNotFound() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("updateOsdChangeLanguageNotFound");
        var id = holder.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, null, null, Long.MAX_VALUE);
        assertClientError(() -> client.updateOsd(request), LANGUAGE_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeLanguage() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("updateOsdChangeUserNotFound");
        var id = holder.osd.getId();
        client.lockOsd(id);

        List<Language> languages = client.listLanguages();
        var            newLang   = languages.get(1);
        assertNotEquals(holder.osd.getLanguageId(), newLang.getId());

        var request = new UpdateOsdRequest(id, null, null, null, null, null, newLang.getId());
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(newLang.getId(), osd.getLanguageId());
    }

    @Test
    public void updateOsdLockedByOtherUser() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("osd-update-forbidden");
        var id = holder.osd.getId();
        adminClient.lockOsd(id);
        var request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), OBJECT_LOCKED_BY_OTHER_USER);
    }

    @Test
    public void updateOsdWithoutLockingIt() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        holder.createOsd("osd-update-forbidden");
        var id      = holder.osd.getId();
        var request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), OBJECT_MUST_BE_LOCKED_BY_USER);
    }

    @Test
    public void deleteOsdHappyPath() throws IOException {
        assertTrue(client.deleteOsd(49L));
    }

    @Test
    public void deleteOsdWithCustomMetadata() throws IOException {
        var holder = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas       = List.of(new Meta(null, metasetType.getId(), "<meta>some data</meta>"));
        var osd         = holder.setMetas(metas).createOsd("object#1 with custom meta").osd;
        client.deleteOsd(osd.getId());
        assertClientError(() -> client.getOsdById(osd.getId(), false, false), OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteOsdsNoDeletePermission() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.createAcl("no-delete-perm-acl").createGroup("no-delete-group-acl").createAclGroup()
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("U-no-delete-me");
        ObjectSystemData osd = holder.osd;
        assertClientError(() -> client.deleteOsd(osd.getId()), CANNOT_DELETE_DUE_TO_ERRORS, NO_DELETE_PERMISSION);
    }

    @Test
    public void deleteOsdWithDescendantsFailsWithoutDeleteDescendantsFlag() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("version-me-for-descendant");
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        assertClientError(() -> client.deleteOsd(holder.osd.getId()), CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_DESCENDANTS);
    }

    @Test
    public void deleteOsdWithDescendantsHappyPath() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("version-me-for-descendant");
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(holder.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        client.deleteOsd(holder.osd.getId(), true);
    }

    @Test
    public void deleteOsdWithUnprotectedRelations() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdWithUnprotectedRelations", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteOsdWithUnprotectedRelations"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("left-osd-unprotected");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("left-rt-protected", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsd(rightOsd.getId(), false);
    }

    @Test
    public void deleteTwoOsdsWhoseRelationsProtectEachOther() throws IOException {
        addUserToAclGroupWithPermissions("deleteTwoOsdsWhoseRelationsProtectEachOther", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteTwoOsdsWhoseRelationsProtectEachOther"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-only-protected-relations");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected-all");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("all-protected-delete", true, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsThatAreProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteTwoOsdsThatAreProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteTwoOsdsThatAreProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-left-protected-relations");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("left-protected-delete", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsThatAreProtectedByRightObject() throws IOException {
        var aclId = addUserToAclGroupWithPermissions("deleteTwoOsdsThatAreProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclById(aclId))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-right-protected-relations");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("right-protected-delete", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsWithUnprotectedRelation() throws IOException {
        var aclId = addUserToAclGroupWithPermissions("deleteTwoOsdsWithUnprotectedRelation", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclById(aclId))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-right-unprotected-relations");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("un-protected-delete", false, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteOsdThatIsProtectedByRightObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteOsdThatIsProtectedByRightObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("right-only-protected-delete", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        assertClientError(() -> client.deleteOsds(List.of(rightOsd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_PROTECTED_RELATIONS);
    }


    @Test
    public void deleteOsdThatIsNotProtectedByRightObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsNotProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteOsdThatIsNotProtectedByRightObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("right-only-protected-delete2", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId()), false);
    }

    @Test
    public void deleteOsdThatIsNotProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsNotProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteOsdThatIsNotProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("left-only-protected-delete2", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(leftOsd.getId()), false);
    }

    @Test
    public void deleteLeftOsdThatIsProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteLeftOsdThatIsProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE_OBJECT));
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("deleteLeftOsdThatIsProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = holder.osd;
        holder.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = holder.osd;

        RelationType rt = new RelationType("left-only-protected-delete", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        assertClientError(() -> client.deleteOsds(List.of(leftOsd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_PROTECTED_RELATIONS);
    }

    @Test
    public void deleteWithLockedObject() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        var osd = holder.osd;
        adminClient.lockOsd(osd.getId());
        assertClientError(() -> client.deleteOsds(List.of(osd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_LOCKED_BY_OTHER_USER);
    }

    @Test
    public void deleteWithLockedObjectsAsAdmin() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        var osd = holder.osd;
        adminClient.lockOsd(osd.getId());
        adminClient.deleteOsds(List.of(osd.getId()), false);
    }

    @Test
    public void deleteWithLockedObjectByCurrentUser() throws IOException {
        var holder = new TestObjectHolder(adminClient);
        holder.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        var osd = holder.osd;
        client.lockOsd(osd.getId());
        client.deleteOsds(List.of(osd.getId()), false);
    }

    @Test
    public void getOsdWithCustomMetadata() throws IOException {
        var toh     = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        var content = "<xml>test</xml>";
        toh.createOsd("with-custom-meta").createOsdMeta(content);
        ObjectSystemData osd = client.getOsdById(toh.osd.getId(), false, true);
        assertEquals(content, osd.getMetas().get(0).getContent());
    }

    @Test
    public void getOsdWithUnbrowsableCustomMetadata() throws IOException {
        var content = "<xml>test</xml>";
        var toh     = new TestObjectHolder(adminClient, "reviewers.acl", userId, createFolderId);
        toh.createAcl("no-browse-custom-meta")
                .createGroup("unbrowsable-meta")
                .createAclGroup()
                .addPermissionsByName(List.of(BROWSE_OBJECT.getName()))
                .addUserToGroup(toh.user.getId())
                .createOsd("with-custom-meta")
                .createOsdMeta(content);
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.getOsdMetas(toh.osd.getId()));
        assertEquals(NO_READ_CUSTOM_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void getOsdByFolderWithCustomMetadata() throws IOException {
        var content = "<xml>test</xml>";
        var toh = new TestObjectHolder(adminClient, "reviewers.acl", userId, createFolderId)
                .createFolder("custom-metadata-test", createFolderId)
                .createOsd("with-custom-meta")
                .createOsdMeta(content);
        ObjectSystemData osd = client.getOsdsInFolder(toh.osd.getParentId(), false, false, true).get(0);
        assertEquals(content, osd.getMetas().get(0).getContent());
    }

    @Test
    public void copyWithInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.createOsd(new CreateOsdRequest()));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
    }

    private TestObjectHolder createCopySourceObject(String testName) throws IOException {
        return new TestObjectHolder(adminClient)
                .createAcl(testName)
                .createGroup(testName)
                .createAclGroup()
                .setUser(userId)
                .addUserToGroup(userId)
                .addPermissionsByName(List.of(BROWSE_OBJECT.getName()))
                .createFolder(testName, createFolderId)
                .createOsd(testName);
    }

    @Test
    public void copyWithoutReadContentPermission() throws IOException {
        var toh = createCopySourceObject("copyWithoutReadContentPermission");
        var ex  = assertThrows(CinnamonClientException.class, () -> client.copyOsds(createFolderId, List.of(toh.osd.getId())));
        assertEquals(NO_READ_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void copyWithoutReadCustomMetadataPermission() throws IOException {
        var toh = createCopySourceObject("copyWithoutReadCustomMetadataPermission")
                .addPermissionsByName(List.of(READ_OBJECT_CONTENT.getName()));
        var ex = assertThrows(CinnamonClientException.class, () -> client.copyOsds(createFolderId, List.of(toh.osd.getId())));
        assertEquals(NO_READ_CUSTOM_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void copyWithoutReadSysMetaPermission() throws IOException {
        var toh = createCopySourceObject("copyWithoutReadSysMetaPermission")
                .addPermissionsByName(List.of(READ_OBJECT_CONTENT.getName(), READ_OBJECT_CUSTOM_METADATA.getName()));
        var ex = assertThrows(CinnamonClientException.class, () -> client.copyOsds(createFolderId, List.of(toh.osd.getId())));
        assertEquals(NO_READ_OBJECT_SYS_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void copyWithoutTargetFolder() throws IOException {
        var toh = createCopySourceObject("copyWithoutTargetFolder")
                .addPermissionsByName(List.of(READ_OBJECT_CONTENT.getName(),
                        READ_OBJECT_CUSTOM_METADATA.getName(),
                        READ_OBJECT_SYS_METADATA.getName()));
        var ex = assertThrows(CinnamonClientException.class, () -> client.copyOsds(Long.MAX_VALUE, List.of(toh.osd.getId())));
        assertEquals(FOLDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void copyWithoutCreateObjectPermission() throws IOException {
        var toh = createCopySourceObject("copyWithoutCreateObjectPermission")
                .addPermissionsByName(List.of(READ_OBJECT_CONTENT.getName(),
                        READ_OBJECT_CUSTOM_METADATA.getName(),
                        READ_OBJECT_SYS_METADATA.getName()))
                .createFolder("copyWithoutCreateObjectPermission-target", createFolderId);
        var ex = assertThrows(CinnamonClientException.class, () -> client.copyOsds(toh.folder.getId(), List.of(toh.osd.getId())));
        assertEquals(NO_CREATE_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void copyWithLeftRelationOnCopy() throws IOException {
        var toh = createCopySourceObject("copyWithLeftRelationOnCopy");
        var acl = adminClient.getAclByName("reviewers.acl");
        var relatedOsd = adminClient.createOsd(new CreateOsdRequest("copyWithLeftRelationOnCopy",
                toh.folder.getId(), userId, acl.getId(), toh.objectType.getId(), null, toh.language.getId(), null, null));
        var relationType = adminClient.createRelationTypes(List.of(
                new RelationType("copyWithLeftRelationOnCopy", false, false, false, true, false, false))).get(0);
        var originalRelation = adminClient.createRelation(toh.osd.getId(), relatedOsd.getId(), relationType.getId(), "<meta/>");
        var copy             = adminClient.copyOsds(createFolderId, List.of(toh.osd.getId())).get(0);
        adminClient.lockOsd(copy.getId());
        List<Relation> relations = adminClient.getRelations(List.of(copy.getId()));
        assertEquals(1, relations.size());
        var copiedRelation = relations.get(0);
        assertEquals(originalRelation.getTypeId(), copiedRelation.getTypeId());
        assertEquals(originalRelation.getMetadata(), copiedRelation.getMetadata());
        assertEquals(relatedOsd.getId(), copiedRelation.getRightId());
    }

    @Test
    public void copyWithLeftRelationNonCopy() throws IOException {
        var toh = createCopySourceObject("copyWithLeftRelationNonCopy");
        var acl = client.getAclByName("reviewers.acl");
        var relatedOsd = adminClient.createOsd(new CreateOsdRequest("copyWithLeftRelationNonCopy",
                toh.folder.getId(), userId, acl.getId(), toh.objectType.getId(), null, toh.language.getId(), null, null));
        var relationType = adminClient.createRelationTypes(List.of(
                new RelationType("copyWithLeftRelationNonCopy", false, false, true, false, true, true))).get(0);
        var            originalRelation = adminClient.createRelation(toh.osd.getId(), relatedOsd.getId(), relationType.getId(), "<meta/>");
        var            copy             = adminClient.copyOsds(createFolderId, List.of(toh.osd.getId())).get(0);
        List<Relation> relations        = adminClient.getRelations(List.of(copy.getId()));
        assertEquals(0, relations.size());
    }

    @Test
    public void copyWithRightRelationOnCopy() throws IOException {
        var toh = createCopySourceObject("copyWithRightRelationOnCopy");
        var acl = client.getAclByName("reviewers.acl");
        var relatedOsd = adminClient.createOsd(new CreateOsdRequest("copyWithRightRelationOnCopy",
                toh.folder.getId(), userId, acl.getId(), toh.objectType.getId(), null, toh.language.getId(), null, null));
        var relationType = adminClient.createRelationTypes(List.of(
                new RelationType("copyWithRightRelationOnCopy", false, false, true, true, true, true))).get(0);
        var            originalRelation = adminClient.createRelation(relatedOsd.getId(), toh.osd.getId(), relationType.getId(), "<meta/>");
        var            copy             = adminClient.copyOsds(createFolderId, List.of(toh.osd.getId())).get(0);
        List<Relation> relations        = adminClient.getRelations(List.of(copy.getId()));
        assertEquals(1, relations.size());
        var copiedRelation = relations.get(0);
        assertEquals(originalRelation.getTypeId(), copiedRelation.getTypeId());
        assertEquals(originalRelation.getMetadata(), copiedRelation.getMetadata());
        assertEquals(relatedOsd.getId(), copiedRelation.getLeftId());
    }

    @Test
    public void copyWithRightRelationNonCopy() throws IOException {
        var toh = createCopySourceObject("copyWithRightRelationNonCopy");
        var acl = client.getAclByName("reviewers.acl");
        var relatedOsd = adminClient.createOsd(new CreateOsdRequest("copyWithRightRelationNonCopy",
                toh.folder.getId(), userId, acl.getId(), toh.objectType.getId(), null, toh.language.getId(), null, null));
        var relationType = adminClient.createRelationTypes(List.of(
                new RelationType("copyWithRightRelationNonCopy", false, false, false, false, true, true))).get(0);
        var            originalRelation = adminClient.createRelation(toh.osd.getId(), relatedOsd.getId(), relationType.getId(), "<meta/>");
        var            copy             = adminClient.copyOsds(createFolderId, List.of(toh.osd.getId())).get(0);
        List<Relation> relations        = adminClient.getRelations(List.of(copy.getId()));
        assertEquals(0, relations.size());
    }

    @Test
    public void copyWithMetasets() throws IOException {
        var                    toh      = createCopySourceObject("copyWithMetasets");
        MetasetType            foo      = adminClient.createMetasetType("foo", false);
        long                   osdId    = toh.osd.getId();
        Meta                   osdMeta  = adminClient.createOsdMeta(osdId, "some meta", foo.getId());
        List<ObjectSystemData> copies   = adminClient.copyOsds(createFolderId, List.of(osdId), List.of(foo.getId()));
        ObjectSystemData       copy     = copies.get(0);
        Meta                   copyMeta = adminClient.getOsdMetas(copy.getId()).get(0);
        assertEquals(osdMeta.getContent(), copyMeta.getContent());
        assertEquals(osdMeta.getTypeId(), copyMeta.getTypeId());
    }

    @Test
    public void copyWithContent() throws IOException {
        var toh = createCopySourceObject("copyWithContent");
        var id  = toh.osd.getId();
        adminClient.lockOsd(id);
        var xmlFormat = TestObjectHolder.formats.stream().filter(format -> format.getName().equals("xml")).findFirst().orElseThrow();
        var result    = adminClient.setContentOnLockedOsd(id, xmlFormat.getId(), new File("pom.xml"));
        assertTrue(result);
        ObjectSystemData copy          = adminClient.copyOsds(createFolderId, List.of(id), List.of()).get(0);
        String           sha256HexOrig = DigestUtils.sha256Hex(new FileInputStream("pom.xml"));
        String           sha256HexCopy = DigestUtils.sha256Hex(adminClient.getContent(copy.getId()).readAllBytes());
        assertEquals(sha256HexOrig, sha256HexCopy);
        var original = adminClient.getOsdById(id, false, false);
        assertEquals(original.getContentHash(), copy.getContentHash());
        // at the moment, we do not have content de-duplication, so copying
        // an OSD should result in the content being replicated in storage with new path:
        assertNotEquals(original.getContentPath(), copy.getContentPath());
        assertEquals(original.getFormatId(), copy.getFormatId());
    }

    @Test
    public void copyWithLifecycleStateForCopyId() throws IOException {
        var            toh        = createCopySourceObject("copyWithLifecycleStateForCopyId");
        Lifecycle      lifecycle  = adminClient.createLifecycle("copy-cycle");
        LifecycleState copyState  = adminClient.createLifecycleState(new LifecycleState("copy-state", "<config/>", NopState.class.getName(), lifecycle.getId(), null));
        LifecycleState startState = adminClient.createLifecycleState(new LifecycleState("start-copy-test", "<config/>", NopState.class.getName(), lifecycle.getId(), copyState.getId()));
        // skip update to lifecycle for now, just use AttachLifecycleRequest
        // lifecycle.setDefaultStateId(startState.getId());
        var id = toh.osd.getId();
        adminClient.attachLifecycle(id, lifecycle.getId(), startState.getId());
        ObjectSystemData copy = adminClient.copyOsds(createFolderId, List.of(id), List.of()).get(0);
        assertEquals(copyState.getId(), copy.getLifecycleStateId());
    }

    @Test
    public void deleteOsdWithContent() throws IOException, InterruptedException {
        var toh = new TestObjectHolder(adminClient, "reviewers.acl", adminId, createFolderId);
        toh.createOsd("deleteOsdWithContent - deletionTask test");
        var osd = toh.osd;
        createTestContentOnOsd(osd.getId(), true);
        toh.lockOsd();
        adminClient.deleteOsd(osd.getId());
        Thread.sleep(500);
        assertEquals(1, CinnamonServer.cinnamonStats.getDeletions().get());
    }

    @Test
    public void newOsdHasRootId() throws IOException {
        // new OSD should have its own id as rootId
        ObjectSystemData osd = client.createOsd(new CreateOsdRequest("osd-with-root-id", createFolderId, userId, 1L,
                1L, null, 1L, null, "<sum/>"));
        assertEquals(osd.getId(), osd.getRootId());
    }

    @Test
    public void newVersionHasValidRootId() throws IOException{
        // new version of an OSD should have predecessor as rootId:
        ObjectSystemData osd = client.createOsd(new CreateOsdRequest("osd for new-version with root id test", createFolderId, userId, 1L,
                1L, null, 1L, null, "<sum/>"));
        ObjectSystemData newVersion = adminClient.version(new CreateNewVersionRequest(osd.getId()));
        assertEquals(osd.getId(), newVersion.getRootId());
    }

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
        if (asSuperuser) {
            adminClient.lockOsd(osdId);
            adminClient.setContentOnLockedOsd(osdId, 1L, new File("pom.xml"));
            adminClient.unlockOsd(osdId);
        } else {
            client.lockOsd(osdId);
            client.setContentOnLockedOsd(osdId, 1L, new File("pom.xml"));
            client.unlockOsd(osdId);
        }
    }


    public ObjectSystemData fetchSingleOsd(Long id) throws IOException {
        OsdRequest   osdRequest  = new OsdRequest(Collections.singletonList(id), true, false);
        HttpResponse osdResponse = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        assertResponseOkay(osdResponse);
        return unwrapOsds(osdResponse, 1).get(0);
    }

}
