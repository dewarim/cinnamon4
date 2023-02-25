package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Group;
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
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.Summary;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.*;
import static com.dewarim.cinnamon.model.request.osd.VersionPredicate.*;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.apache.http.entity.mime.MIME.CONTENT_DISPOSITION;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OsdServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(OsdServletIntegrationTest.class);

    private static final Long CREATE_ACL_ID          = 8L;
    /**
     * Non-Admin user id
     */
    private static final Long STANDARD_USER_ID       = 2L;
    private static final Long PLAINTEXT_FORMAT_ID    = 2L;
    private static final Long DEFAULT_OBJECT_TYPE_ID = 1L;

    /**
     * This folder's ACL allows object creation.
     */
    private static final Long NEW_RENDERTASK_LIFECYCLE_STATE_ID = 1L;
    private static final Long GERMAN_LANGUAGE_ID                = 1L;


    @Test
    public void getObjectsById() throws IOException {
        var toh  = new TestObjectHolder(client, userId);
        var osd1 = toh.createOsd().osd;
        var osd2 = toh.createOsd().osd;
        var osd3 = toh.createOsd().osd;
        var owned = prepareAclGroupWithOwnerPermissions(List.of(BROWSE))
                .createOsd().osd;
        Acl   everyoneAcl   = adminClient.createAcl("everyone-group-acl");
        Group everyoneGroup = adminClient.listGroups().stream().filter(g -> g.getName().equals(ALIAS_EVERYONE)).findFirst().orElseThrow();
        var tohAdmin = new TestObjectHolder(adminClient, userId)
                .setAcl(everyoneAcl)
                .setGroup(everyoneGroup)
                .createAclGroup()
                .addUserToGroup(userId)
                .addPermissions(List.of(BROWSE))
                .createOsd();
        ObjectSystemData everyone     = tohAdmin.osd;
        ObjectSystemData notBrowsable = prepareAclGroupWithPermissions(List.of()).createOsd("unbrowsable").osd;
        List<Long> osdIds = List.of(osd1.getId(), osd2.getId(), osd3.getId(),
                owned.getId(), everyone.getId(), notBrowsable.getId());
        List<ObjectSystemData> osds = client.getOsdsById(osdIds, false, false);
        assertFalse(osds.stream().anyMatch(osd -> osd.getName().equals("unbrowsable")));
        assertEquals(5, osds.size());
        assertTrue(osds.containsAll(List.of(osd1, osd2, osd3, owned, everyone)));
        var adminOsds = adminClient.getOsdsById(osdIds, false, false);
        assertEquals(6, adminOsds.size());
        assertTrue(adminOsds.containsAll(List.of(osd1, osd2, osd3, owned, everyone, notBrowsable)));
    }

    @Test
    public void getObjectsByIdWithDefaultSummary() throws IOException {
        var        osdId      = new TestObjectHolder(client, userId).createOsd().osd.getId();
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(osdId));
        osdRequest.setIncludeSummary(false);
        HttpResponse           response = sendAdminRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        List<ObjectSystemData> dataList = unwrapOsds(response, 1);
        assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals(new ObjectSystemData().getSummary())));
    }

    @Test
    public void getObjectsByIdIncludingSummary() throws IOException {
        var osdId = new TestObjectHolder(client, userId)
                .createOsd().setSummaryOnOsd("xxx").osd.getId();
        assertTrue(client.getOsdSummaries(List.of(osdId)).stream().anyMatch(s -> s.getContent().equals("xxx")));
    }

    @Test
    public void getObjectsByFolderId() throws IOException {
        var toh = prepareAclGroupWithPermissions("getObjectsByFolderId",
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        var osdAsLinkTarget = toh.createOsd("osdAsLinkTarget").osd;
        var folder          = toh.createFolder("getObjectsByFolderId", createFolderId).folder;
        var osd1            = toh.createOsd("osd-1-by-folder-id").osd;
        client.setSummary(osd1.getId(), "<summary>child@archive</summary>");
        var                    osd2         = toh.createOsd("osd-2-by-folder-id").osd;
        var                    link         = toh.createLinkToOsd(osdAsLinkTarget).link;
        OsdWrapper             osdWrapper   = client.getOsdsInFolder(folder.getId(), true, false, false, ALL);
        List<ObjectSystemData> osdsInFolder = osdWrapper.getOsds();

        assertEquals(2, osdsInFolder.size());
        assertEquals(1, osdWrapper.getLinks().size());
        assertTrue(osdsInFolder.stream().anyMatch(osd -> osd.getSummary().equals("<summary>child@archive</summary>")));
        assertTrue(osdsInFolder.stream().map(ObjectSystemData::getId).anyMatch(o -> o.equals(osd1.getId())));
        assertTrue(osdsInFolder.stream().map(ObjectSystemData::getId).anyMatch(o -> o.equals(osd2.getId())));
        assertEquals(link.getId(), osdWrapper.getLinks().get(0).getId());
        assertEquals(link.getObjectId(), osdAsLinkTarget.getId());
    }

    @Test
    public void getObjectsByFolderIdOnlyHead() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createFolder("only-head", createFolderId)
                .createOsd("get-objects-by-folder-only-head");
        ObjectSystemData       version1     = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        ObjectSystemData       head         = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData       branch       = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        List<ObjectSystemData> osdsInFolder = client.getOsdsInFolder(toh.folder.getId(), false, false, false, HEAD).getOsds();
        assertEquals(1, osdsInFolder.size());
        assertEquals(head, osdsInFolder.get(0));
    }

    @Test
    public void getObjectsByFolderIdOnlyBranches() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createFolder("getObjectsByFolderIdOnlyBranches", createFolderId)
                .createOsd("get-objects-by-folder-branch");
        ObjectSystemData       version1     = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        ObjectSystemData       head         = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData       branch       = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        List<ObjectSystemData> osdsInFolder = client.getOsdsInFolder(toh.folder.getId(), false, false, false, BRANCH).getOsds();
        assertEquals(2, osdsInFolder.size());
        // head object is also considered a branch (for legacy reasons):
        assertTrue(osdsInFolder.contains(head));
        assertTrue(osdsInFolder.contains(branch));
    }

    @Test
    public void osdDateFieldsAreFormattedAsIso8601() throws IOException, ParseException {
        var osd = new TestObjectHolder(client, userId)
                .createOsd("osdDateFieldsAreFormattedAsIso8601").osd;
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
        var toh = prepareAclGroupWithPermissions("getObjectsByFolderIdWithLinksAsOsds",
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        var osdAsLinkTarget = toh.createOsd("osdAsLinkTarget").osd;
        var folder          = toh.createFolder("getObjectsByFolderIdWithLinksAsOsds", createFolderId).folder;
        toh.createOsd("osd-x-by-folder-id")
                .createLinkToOsd(osdAsLinkTarget);
        OsdWrapper             osdWrapper = client.getOsdsInFolder(folder.getId(), true, true, false, ALL);
        List<Link>             links      = osdWrapper.getLinks();
        List<ObjectSystemData> linkedOsds = osdWrapper.getReferences();
        assertEquals(1, links.size());
        assertEquals(1, linkedOsds.size());
        assertEquals(toh.link.getObjectId(), linkedOsds.get(0).getId());
    }

    @Test
    public void createObjectWithCustomMetadata() throws IOException {
        var toh = new TestObjectHolder(client, userId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas       = List.of(new Meta(null, metasetType.getId(), "<meta>some data</meta>"));
        var osd         = toh.setMetas(metas).createOsd("object#1 with custom meta").osd;
        assertEquals(osd.getMetas().get(0).getContent(), toh.metas.get(0).getContent());
        var osdWithMeta = client.getOsdById(osd.getId(), false, true);
        assertEquals(1, osdWithMeta.getMetas().size());
        assertEquals(osd.getMetas().get(0), osdWithMeta.getMetas().get(0));
    }

    @Test
    public void createObjectWithNonUniqueMetasets() throws IOException {
        var toh = new TestObjectHolder(client, userId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas = List.of(new Meta(null, metasetType.getId(), "<meta><comment>first post</comment></meta>"),
                new Meta(null, metasetType.getId(), "<meta><comment>second post</comment></meta>"));
        var osd = toh.setMetas(metas).createOsd("object#2 with custom meta").osd;
        assertEquals(osd.getMetas().get(0).getContent(), toh.metas.get(0).getContent());
        var osdWithMeta = client.getOsdById(osd.getId(), false, true);
        assertEquals(2, osdWithMeta.getMetas().size());
        assertEquals(osd.getMetas().get(0), osdWithMeta.getMetas().get(0));
        assertEquals(osd.getMetas().get(1), osdWithMeta.getMetas().get(1));
    }

    @Test
    public void createObjectWithMultipelUniqueMetasets() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        // license is a unique metaset in our test database
        var metasetType = TestObjectHolder.getMetasetType("license");
        var metas = List.of(new Meta(null, metasetType.getId(), "<gpl/>"),
                new Meta(null, metasetType.getId(), "<proprietaryLicense/>"));
        assertClientError(() -> toh.setMetas(metas).createOsd("object#3 with custom meta"), METASET_UNIQUE_CHECK_FAILED);
    }

    @Test
    public void setSummaryHappyPath() throws IOException {
        var osdId = prepareAclGroupWithPermissions("setSummaryHappyPath", List.of(
                WRITE_OBJECT_SYS_METADATA, READ_OBJECT_SYS_METADATA
        )).createOsd("setSummaryHappyPath").osd.getId();
        client.setSummary(osdId, "<sum>sum</sum>");
        List<Summary> summaries = client.getOsdSummaries(List.of(osdId));
        assertFalse(summaries.isEmpty());
        assertEquals(summaries.get(0).getContent(), "<sum>sum</sum>");
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("setSummaryMissingPermission", List.of(BROWSE))
                .createOsd("setSummaryMissingPermission");
        var ex = assertThrows(CinnamonClientException.class, () -> client.setSummary(toh.osd.getId(), "a summary"));
        assertEquals(NO_WRITE_SYS_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("getSummaryHappyPath").osd.getId();
        client.setSummary(osdId, "<my-sum/>");
        List<Summary> summaries = client.getOsdSummaries(List.of(osdId));
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(summaries.get(0).getContent(), equalTo("<my-sum/>"));
    }

    @Test
    public void getSummariesMissingPermission() throws IOException {
        long osdId = prepareAclGroupWithPermissions("getSummariesMissingPermission", List.of(WRITE_OBJECT_SYS_METADATA))
                .createOsd("getSummariesMissingPermission")
                .osd.getId();
        client.setSummary(osdId, "<foo/>");
        List<Summary> summaries = client.getOsdSummaries(List.of(osdId));
        // when all ids are non-readable, return an empty list:
        // TODO: would it be better to throw an Exception if missing READ_OBJECT_SYS_METADATA permission?
        assertTrue(summaries.isEmpty());
    }

    @Test
    public void getContentHappyPath() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("getContentHappyPath").osd.getId();
        createTestContentOnOsd(osdId, false);

        // do not use cinnamonClient as we want to verify content-type header.
        HttpResponse response = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, new IdRequest(osdId));
        assertResponseOkay(response);
        Header contentType = response.getFirstHeader(CONTENT_TYPE);
        assertEquals(APPLICATION_XML.getMimeType(), contentType.getValue());
        Header           contentDisposition = response.getFirstHeader(CONTENT_DISPOSITION);
        ObjectSystemData osd                = client.getOsdById(osdId, false, false);
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
        var toh = new TestObjectHolder(client, userId)
                .createOsd("setContentWithoutLockingOsd");
        SetContentRequest setContentRequest  = new SetContentRequest(toh.osd.getId(), 1L);
        HttpEntity        multipartEntity    = createMultipartEntityWithFileBody("setContentRequest", setContentRequest);
        HttpResponse      setContentResponse = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, multipartEntity);
        assertCinnamonError(setContentResponse, ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER);
    }

    @Test
    public void getContentWithoutReadPermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, userId)
                .createAcl("no-content-read-permission")
                .createOsd("getContentWithoutReadPermission");
        assertClientError(() -> client.getContent(toh.osd.getId()), NO_READ_PERMISSION);
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
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("getContentWithoutContent").osd.getId();
        var ex = assertThrows(CinnamonClientException.class, () -> client.getContent(osdId));
        assertEquals(OBJECT_HAS_NO_CONTENT, ex.getErrorCode());
    }

    @Test
    public void setContentWithDefaultContentProviderHappyPath() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("setContentWithDefaultContentProviderHappyPath").osd.getId();
        createTestContentOnOsd(osdId, false);

        // check data is in content store:
        ObjectSystemData osd         = client.getOsdById(osdId, false, false);
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
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("setContentWithDefaultContentProviderHappyPath").osd.getId();

        SetContentRequest contentRequest = new SetContentRequest(osdId, 1L);
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
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("setContentWithDefaultContentProviderHappyPath").osd.getId();
        client.lockOsd(osdId);
        var ex = assertThrows(CinnamonClientException.class, () -> client.setContentOnLockedOsd(osdId, Long.MAX_VALUE, new File("pom.xml")));
        assertEquals(FORMAT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void setContentWithoutWritePermission() throws IOException {
        long osdId = prepareAclGroupWithPermissions(List.of(LOCK))
                .createOsd("setContentWithoutWritePermission").osd.getId();
        client.lockOsd(osdId);
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.setContentOnLockedOsd(osdId, 1L, new File("pom.xml")));
        assertEquals(NO_WRITE_PERMISSION, ex.getErrorCode());

    }

    @Test
    public void lockAndUnlockObject() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("lockAndUnlockObject").osd.getId();
        client.lockOsd(osdId);
        // verify:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertEquals(userId, osd.getLockerId());

        client.unlockOsd(osdId);
        osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertNull(osd.getLockerId());
    }

    @Test
    public void lockTwice() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("lockTwice").osd.getId();
        client.lockOsd(osdId);
        client.lockOsd(osdId);
        // verify:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertEquals(userId, osd.getLockerId());
    }

    @Test
    public void unlockTwice() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("unlockTwice").osd.getId();
        client.lockOsd(osdId);
        client.unlockOsd(osdId);
        client.unlockOsd(osdId);
        // verify:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertNull(osd.getLockerId());
    }

    @Test
    public void overwriteOtherUsersLockShouldFail() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("overwriteOtherUsersLockShouldFail").osd.getId();
        adminClient.lockOsd(osdId);

        // try to overwrite admin's lock:
        var ex = assertThrows(CinnamonClientException.class, () -> client.lockOsd(osdId));
        assertEquals(OBJECT_LOCKED_BY_OTHER_USER, ex.getErrorCode());
    }

    @Test
    public void unlockOtherUsersLockShouldFail() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("unlockOtherUsersLockShouldFail").osd.getId();
        adminClient.lockOsd(osdId);

        // try to unlock admin's lock:
        var ex = assertThrows(CinnamonClientException.class, () -> client.unlockOsd(osdId));
        assertEquals(OBJECT_LOCKED_BY_OTHER_USER, ex.getErrorCode());
    }

    @Test
    public void lockAndUnlockShouldFailWithInvalidRequest() {
        var exLock = assertThrows(CinnamonClientException.class, () -> client.lockOsd(null));
        assertEquals(INVALID_REQUEST, exLock.getErrorCode());
        var exUnlock = assertThrows(CinnamonClientException.class, () -> client.unlockOsd(null));
        assertEquals(INVALID_REQUEST, exUnlock.getErrorCode());
    }

    @Test
    public void lockAndUnlockShouldFailWithoutPermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId)
                .createAcl("lockAndUnlockShouldFailWithoutPermission")
                .createGroup("lockAndUnlockShouldFailWithoutPermission")
                .createAclGroup()
                .addUserToGroup(userId)
                .addPermissions(List.of(DefaultPermission.READ_OBJECT_SYS_METADATA))
                .createOsd("lockAndUnlockShouldFailWithoutPermission");
        long osdId = toh.osd.getId();

        var ex = assertThrows(CinnamonClientException.class,
                () -> client.lockOsd(osdId));
        assertEquals(NO_LOCK_PERMISSION, ex.getErrorCode());

        toh.addPermissions(List.of(LOCK));
        client.lockOsd(osdId);
        toh.removePermissions(List.of(LOCK));

        ex = assertThrows(CinnamonClientException.class,
                () -> client.unlockOsd(osdId));
        assertEquals(NO_LOCK_PERMISSION, ex.getErrorCode());
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
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("superuserHasMasterKeyForUnlock").osd.getId();
        client.lockOsd(osdId);
        adminClient.unlockOsd(osdId);
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
        MetasetType metasetType = adminClient.createMetasetType("not-readable", true);
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd("getMetaWithoutReadPermission")
                .setMetasetType(metasetType)
                .createOsdMeta("foo");
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.getOsdMetas(toh.osd.getId()));
        assertEquals(NO_READ_CUSTOM_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void getMetaHappyPathAllMeta() throws IOException {
        MetasetType metasetType1 = adminClient.createMetasetType("type1", true);
        MetasetType metasetType2 = adminClient.createMetasetType("type2", true);
        var         toh          = new TestObjectHolder(client, userId);
        toh.createOsd("getMetaHappyPathAllMeta")
                .setMetasetType(metasetType1)
                .createOsdMeta("<metaset><p>Good Test</p></metaset>")
                .setMetasetType(metasetType2)
                .createOsdMeta("<metaset><license>GPL</license></metaset>");

        List<Meta>   osdMetas = client.getOsdMetas(toh.osd.getId());
        List<String> content  = osdMetas.stream().map(Meta::getContent).toList();
        assertTrue(content.contains("<metaset><p>Good Test</p></metaset>"));
        assertTrue(content.contains("<metaset><license>GPL</license></metaset>"));
    }

    @Test
    public void getMetaHappyPathSingleMeta() throws IOException, ParsingException {
        MetasetType metasetType1 = adminClient.createMetasetType("getMetaHappyPathSingleMeta", true);
        var toh = new TestObjectHolder(client, userId)
                .createOsd("getMetaHappyPathAllMeta")
                .setMetasetType(metasetType1)
                .createOsdMeta("<metaset><p>Good Test</p></metaset>");

        List<Meta> osdMetas = client.getOsdMetas(toh.osd.getId(), List.of(metasetType1.getId()));
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
        MetasetType metasetType = adminClient.createMetasetType("not-writable", true);
        var         toh         = new TestObjectHolder(adminClient, userId);
        toh.createAcl("createMetaObjectNotWritable")
                .createGroup("createMetaObjectNotWritable")
                .createAclGroup().addUserToGroup(userId)
                .createOsd("createMetaObjectNotWritable");
        var ex = assertThrows(CinnamonClientException.class, () -> client.createOsdMeta(toh.osd.getId(), "unwritten", metasetType.getId()));
        assertEquals(NO_WRITE_CUSTOM_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("createMetaMetasetTypeByNameNotFound");
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.createOsdMeta(toh.osd.getId(), "type does not exist", Long.MAX_VALUE));
        assertEquals(METASET_TYPE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        MetasetType metasetType = adminClient.createMetasetType("unique-metaset", true);
        var toh = new TestObjectHolder(client, userId)
                .createOsd("createMetaMetasetHappyWithExistingMeta")
                .setMetasetType(metasetType)
                .createOsdMeta("1st");
        var ex = assertThrows(CinnamonClientException.class, () -> client.createOsdMeta(toh.osd.getId(),
                "forbidden duplicate", metasetType.getId()));
        assertEquals(METASET_IS_UNIQUE_AND_ALREADY_EXISTS, ex.getErrorCode());
    }

    // non-unique metasetType should allow appending new metasets.
    @Test
    public void createMetaMetasetHappyWithExistingMeta() throws IOException {
        MetasetType metasetType = adminClient.createMetasetType("non-unique-metaset", false);
        var toh = new TestObjectHolder(client, userId)
                .createOsd("createMetaMetasetHappyWithExistingMeta")
                .setMetasetType(metasetType)
                .createOsdMeta("1st")
                .createOsdMeta("2nd");
        List<Meta> osdMetas = client.getOsdMetas(toh.osd.getId());
        assertEquals(2, osdMetas.size());
    }

    @Test
    public void createMetaMetasetHappyPath() throws IOException {
        MetasetType metasetType = adminClient.createMetasetType("happy metaset", false);
        var toh = new TestObjectHolder(client, userId)
                .createOsd("createMetaMetasetHappyPath")
                .setMetasetType(metasetType)
                .createOsdMeta("smile");
        assertEquals("smile", toh.meta.getContent());
        assertEquals(metasetType.getId(), toh.meta.getTypeId());
    }

    @Test
    public void deleteMetaInvalidRequest() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest();
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT))
                .createOsd("deleteMetaWithoutPermission")
                .createOsdMeta("test");
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteOsdMeta(toh.meta.getId()));
        assertEquals(NO_WRITE_CUSTOM_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.OSD__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        Long metaId = new TestObjectHolder(client, userId)
                .createOsd("delete-all-metas")
                .createOsdMeta("<some><meta/></some>").meta.getId();
        client.deleteOsdMeta(metaId);
    }

    @Test
    public void deleteAllMetas() throws IOException {
        Long id = new TestObjectHolder(client, userId)
                .createOsd("delete-all-metas").osd.getId();
        client.createOsdMeta(new CreateMetaRequest(id, "...", 1L));
        client.createOsdMeta(new CreateMetaRequest(id, "...", 1L));
        client.deleteAllOsdMeta(id);
        assertEquals(0, client.getOsdMetas(id).size());
    }

    @Test
    public void deleteAllVersionsHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT, VERSION_OBJECT, DELETE))
                .createOsd("delete-all-versions-root");
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        ObjectSystemData branch   = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        client.deleteOsd(version2.getId(), true, true);
        assertTrue(client.getOsds(List.of(toh.osd.getId(), version1.getId(), version2.getId(), branch.getId()), false, false).isEmpty());
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
        var parentId = prepareAclGroupWithPermissions(List.of())
                .createFolder().folder.getId();
        assertClientError(() -> client.createOsd(new CreateOsdRequest("forbidden", parentId, userId,
                        1L, 1L, null, 1L, null, DEFAULT_SUMMARY)),
                NO_CREATE_PERMISSION);
    }

    @Test
    public void createOsdAclNotFound() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(Long.MAX_VALUE);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        assertEquals(createFolderId, osd.getParentId());
    }

    @Test
    public void createOsdUploadedFileWithoutFormat() throws IOException {
        CreateOsdRequest request = new CreateOsdRequest();
        request.setAclId(CREATE_ACL_ID);
        request.setName("new osd");
        request.setOwnerId(STANDARD_USER_ID);
        request.setParentId(createFolderId);
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
        request.setParentId(createFolderId);
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
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT))
                .createOsd("version without version permission");
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        var                     ex             = assertThrows(CinnamonClientException.class, () -> client.version(versionRequest));
        assertEquals(NO_VERSION_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void versionWithoutCreatePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createFolder()
                .createOsd("version without create permission");
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        var                     ex             = assertThrows(CinnamonClientException.class, () -> client.version(versionRequest));
        assertEquals(NO_CREATE_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void versionWithBrokenMetaRequests() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("version with broken meta");
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        CreateNewVersionRequest.Metadata metadata       = new CreateNewVersionRequest.Metadata();
        metadata.setContent("");
        metadata.setTypeId(Long.MAX_VALUE);
        versionRequest.getMetaRequests().add(metadata);
        var ex = assertThrows(CinnamonClientException.class, () -> client.version(versionRequest));
        assertEquals(METASET_TYPE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void versionWithMetaRequests() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("versionWithMetaRequests");
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(toh.osd.getId());
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
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("version without multipart content type");
        CreateNewVersionRequest          versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        CreateNewVersionRequest.Metadata metadata       = new CreateNewVersionRequest.Metadata();
        versionRequest.getMetaRequests().add(metadata);
        HttpResponse versionResponse = sendStandardRequest(UrlMapping.OSD__VERSION, versionRequest);
        assertCinnamonError(versionResponse, NOT_MULTIPART_UPLOAD);
    }

    @Test
    public void happyVersionWithMultipartWihFileUploadAndLifecycleState() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("happy version with content and lifecycle state");
        adminClient.attachLifecycle(toh.osd.getId(), 1L, 1L, true);
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        versionRequest.setFormatId(PLAINTEXT_FORMAT_ID);
        ObjectSystemData osd = client.versionWithContent(versionRequest, new File("pom.xml"));
        assertEquals(Long.valueOf(getPomXml().length()), osd.getContentSize());
        assertEquals(PLAINTEXT_FORMAT_ID, osd.getFormatId());
    }

    @Test
    public void versionWithMultipartWihFileUploadButWithoutFormat() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("multipart version without format");
        adminClient.attachLifecycle(toh.osd.getId(), 1L, 1L, true);
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        var                     ex             = assertThrows(CinnamonClientException.class, () -> client.versionWithContent(versionRequest, new File("pom.xml")));
        assertEquals(FORMAT_NOT_FOUND, ex.getErrorCode());
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
        var toh = new TestObjectHolder(client, userId);

        ObjectSystemData initialVersion = toh.createOsd("testVersionNumbering").osd;
        assertEquals("1", initialVersion.getCmnVersion());
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(initialVersion.getId());
        ObjectSystemData        v2             = client.version(versionRequest);

        assertEquals("2", v2.getCmnVersion());
        versionRequest.setId(v2.getId());

        ObjectSystemData v3 = client.version(versionRequest);
        assertEquals("3", v3.getCmnVersion());

        // create another version of v2
        versionRequest.setId(v2.getId());
        ObjectSystemData v2Branch = client.version(versionRequest);
        assertEquals("2.1-1", v2Branch.getCmnVersion());

        // create next version in branch 2
        versionRequest.setId(v2Branch.getId());
        ObjectSystemData v2BranchV2 = client.version(versionRequest);
        assertEquals("2.1-2", v2BranchV2.getCmnVersion());

        // create next version of v2 (second parallel branch)
        versionRequest.setId(v2.getId());
        ObjectSystemData v2parallelBranch = client.version(versionRequest);
        assertEquals("2.2-1", v2parallelBranch.getCmnVersion());

        // create branch of 1st branch of v2
        versionRequest.setId(v2Branch.getId());
        ObjectSystemData branchOfBranch = client.version(versionRequest);
        assertEquals("2.1-1.1-1", branchOfBranch.getCmnVersion());
    }

    @Test
    public void versionWithFailingLifecycleStateChange() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        var osd = toh.createOsd("versionWithFailingLifecycleStateChange").osd;
        adminClient.attachLifecycle(osd.getId(), 4L, 4L, true);
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(osd.getId());
        versionRequest.setFormatId(PLAINTEXT_FORMAT_ID);
        HttpEntity   entity          = createMultipartEntityWithFileBody(CREATE_NEW_VERSION, versionRequest);
        HttpResponse versionResponse = sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entity);
        assertCinnamonError(versionResponse, ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED);
        var ex = assertThrows(CinnamonClientException.class, () -> client.version(versionRequest));
        assertEquals(ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED, ex.getErrorCode());
    }

    @Test
    public void versionWithCopyOnLeftRelation() throws IOException {
        var tohAdmin = prepareAclGroupWithPermissions(List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, VERSION_OBJECT));
        var relationType = adminClient.createRelationType(new RelationType("clone-on-left-version",
                false, false, false, false, true, false));
        var toh             = new TestObjectHolder(client, userId).setAcl(tohAdmin.acl);
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
        var adminToh = prepareAclGroupWithPermissions("versionWithCopyOnRightRelation", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, VERSION_OBJECT));
        var relationType = adminClient.createRelationType(new RelationType("clone-on-right-version",
                false, false, false, false, false, true));
        var toh             = new TestObjectHolder(client, userId).setAcl(adminToh.acl);
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
        var osd = new TestObjectHolder(client, userId).createOsd().osd;
        assertThat(osd.getModifierId(), notNullValue());
        assertThat(osd.getModified(), notNullValue());

        // TODO: maybe configure tests with Thread.sleep() to only run on demand or for a "all tests" szenario
        // we need to sleep as modified value tracks only down to second granularity
        Thread.sleep(1000);

        // admin without changeTracking
        SetSummaryRequest summaryRequest = new SetSummaryRequest(osd.getId(), "a summary");
        adminClient.setSummary(osd.getId(), "a summary");
        var updatedOsd = client.getOsdById(osd.getId(), false, false);
        assertThat(updatedOsd.getModifierId(), equalTo(osd.getModifierId()));
        assertThat(updatedOsd.getModified(), equalTo(osd.getModified()));

        Thread.sleep(1000);

        // standard user should have changeTracking
        client.setSummary(osd.getId(), "new summary");
        osd = client.getOsdById(osd.getId(), false, false);
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
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK))
                .createOsd("osd-update-forbidden");

        var id = toh.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void updateOsdWithParentFolderNotFound() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK, WRITE_OBJECT_SYS_METADATA))
                .createOsd("osd-new-parent-missing");

        var id = toh.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, Long.MAX_VALUE, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void updateOsdWithoutCreateInFolderPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK, WRITE_OBJECT_SYS_METADATA))
                .createOsd("update-osd-no-create-permission");

        var id = toh.osd.getId();
        client.lockOsd(id);

        UpdateOsdRequest request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_CREATE_PERMISSION);
    }

    @Test
    public void updateOsdWithoutMovePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK, WRITE_OBJECT_SYS_METADATA))
                .createOsd("update-osd-no-move-permission");

        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, createFolderId, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), NO_MOVE_PERMISSION);
    }

    @Test
    public void updateOsdWithMovePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .createOsd("update-osd-move-permission")
                .createFolder("target-of-update-osd-by-move", createFolderId);

        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, toh.folder.getId(), null, null, null, null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(toh.folder.getId(), osd.getParentId());
    }

    @Test
    public void updateOsdChangeName() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(NAME_WRITE, LOCK, BROWSE))
                .createOsd("update-osd-rename");
        var id = toh.osd.getId();

        var request = new UpdateOsdRequest(id, null, "new name", null, null, null, null);
        client.lockOsd(id);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals("new name", osd.getName());
    }

    @Test
    public void updateOsdChangeNameNoRenamePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(LOCK))
                .createOsd("update-osd-rename-no-perm");
        var id = toh.osd.getId();
        client.lockOsd(id);
        var request = new UpdateOsdRequest(id, null, "new name", null, null, null, null);
        assertClientError(() -> client.updateOsd(request), NO_NAME_WRITE_PERMISSION);
    }

    @Test
    public void updateOsdChangeTypeNotFound() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(TYPE_WRITE, LOCK)).createOsd("update-osd-rename");
        var id  = toh.osd.getId();

        var request = new UpdateOsdRequest(id, null, null, null, null, Long.MAX_VALUE, null);
        client.lockOsd(id);
        assertClientError(() -> client.updateOsd(request), OBJECT_TYPE_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeTypeNoTypeWritePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(LOCK))
                .createOsd()
                .createObjectType();

        var id = toh.osd.getId();
        client.lockOsd(id);
        var request = new UpdateOsdRequest(id, null, null, null, null,
                toh.objectType.getId(), null);
        assertClientError(() -> client.updateOsd(request), NO_TYPE_WRITE_PERMISSION);
    }

    @Test
    public void updateOsdChangeType() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(LOCK, TYPE_WRITE, BROWSE))
                .createOsd()
                .createObjectType();
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, null,
                toh.objectType.getId(), null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(toh.objectType.getId(), osd.getTypeId());
    }

    @Test
    public void updateOsdChangeAclNoPermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId);

        toh.createAcl("updateOsdChangeAclNoPermission")
                .createGroup("test-updateOsdChangeAclNoPermission")
                .createAclGroup()
                .addPermissions(List.of(BROWSE, LOCK, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .addUserToGroup(userId)
                .createOsd("update-osd-updateOsdChangeAclNoPermission");

        toh.createOsd("updateOsdChangeAclNoPermission");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, toh.acl.getId(), null, null);
        assertClientError(() -> client.updateOsd(request), MISSING_SET_ACL_PERMISSION);
    }

    @Test
    public void updateOsdChangeAclNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("updateOsdChangeAclNoPermission");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, Long.MAX_VALUE, null, null);
        assertClientError(() -> client.updateOsd(request), ACL_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeAcl() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, LOCK, SET_ACL, WRITE_OBJECT_SYS_METADATA, MOVE, CREATE_OBJECT))
                .createOsd("update-osd-updateOsdChangeAcl");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, toh.acl.getId(), null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(toh.acl.getId(), osd.getAclId());
    }

    @Test
    public void updateOsdChangeUserNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("updateOsdChangeUserNotFound");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, Long.MAX_VALUE, null, null, null);
        assertClientError(() -> client.updateOsd(request), USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeUser() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("updateOsdChangeUserNotFound");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, adminId, null, null, null);
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(adminId, osd.getOwnerId());
    }

    @Test
    public void updateOsdChangeLanguageNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("updateOsdChangeLanguageNotFound");
        var id = toh.osd.getId();
        client.lockOsd(id);

        var request = new UpdateOsdRequest(id, null, null, null, null, null, Long.MAX_VALUE);
        assertClientError(() -> client.updateOsd(request), LANGUAGE_NOT_FOUND);
    }

    @Test
    public void updateOsdChangeLanguage() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("updateOsdChangeUserNotFound");
        var id = toh.osd.getId();
        client.lockOsd(id);

        List<Language> languages = client.listLanguages();
        var            newLang   = languages.get(1);
        assertNotEquals(toh.osd.getLanguageId(), newLang.getId());

        var request = new UpdateOsdRequest(id, null, null, null, null, null, newLang.getId());
        client.updateOsd(request);
        var osd = client.getOsdById(id, false, false);
        assertEquals(newLang.getId(), osd.getLanguageId());
    }

    @Test
    public void updateOsdLockedByOtherUser() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("osd-update-forbidden");
        var id = toh.osd.getId();
        adminClient.lockOsd(id);
        var request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), OBJECT_LOCKED_BY_OTHER_USER);
    }

    @Test
    public void updateOsdWithoutLockingIt() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("osd-update-forbidden");
        var id      = toh.osd.getId();
        var request = new UpdateOsdRequest(id, 1L, "-", 1L, 1L, 1L, 1L);
        assertClientError(() -> client.updateOsd(request), OBJECT_MUST_BE_LOCKED_BY_USER);
    }

    @Test
    public void deleteOsdHappyPath() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        toh.createOsd("deleteOsdHappyPath");
        assertTrue(client.deleteOsd(toh.osd.getId()));
    }

    @Test
    public void deleteOsdWithCustomMetadata() throws IOException {
        var toh = new TestObjectHolder(client, userId);

        var metasetType = TestObjectHolder.getMetasetType("comment");
        var metas       = List.of(new Meta(null, metasetType.getId(), "<meta>some data</meta>"));
        var osd         = toh.setMetas(metas).createOsd("object#1 with custom meta").osd;
        client.deleteOsd(osd.getId());
        assertClientError(() -> client.getOsdById(osd.getId(), false, false), OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteOsdsNoDeletePermission() throws IOException {
        var toh = new TestObjectHolder(adminClient);
        toh.createAcl("no-delete-perm-acl").createGroup("no-delete-group-acl").createAclGroup()
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("U-no-delete-me");
        ObjectSystemData osd = toh.osd;
        assertClientError(() -> client.deleteOsd(osd.getId()), CANNOT_DELETE_DUE_TO_ERRORS, NO_DELETE_PERMISSION);
    }

    @Test
    public void deleteOsdWithDescendantsFailsWithoutDeleteDescendantsFlag() throws IOException {
        var              toh      = prepareAclGroupWithPermissions(List.of(DELETE, VERSION_OBJECT)).createOsd();
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        assertClientError(() -> client.deleteOsd(toh.osd.getId()), CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_DESCENDANTS);
    }

    @Test
    public void deleteOsdWithDescendantsHappyPath() throws IOException {
        var              toh      = prepareAclGroupWithPermissions(List.of(DELETE, VERSION_OBJECT)).createOsd();
        ObjectSystemData version1 = client.version(new CreateNewVersionRequest(toh.osd.getId()));
        ObjectSystemData version2 = client.version(new CreateNewVersionRequest(version1.getId()));
        client.deleteOsd(toh.osd.getId(), true);
    }

    @Test
    public void deleteOsdWithUnprotectedRelations() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdWithUnprotectedRelations", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteOsdWithUnprotectedRelations"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("left-osd-unprotected");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("left-rt-protected", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsd(rightOsd.getId(), false);
    }

    @Test
    public void deleteTwoOsdsWhoseRelationsProtectEachOther() throws IOException {
        addUserToAclGroupWithPermissions("deleteTwoOsdsWhoseRelationsProtectEachOther", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteTwoOsdsWhoseRelationsProtectEachOther"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-only-protected-relations");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected-all");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("all-protected-delete", true, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsThatAreProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteTwoOsdsThatAreProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteTwoOsdsThatAreProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-left-protected-relations");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("left-protected-delete", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsThatAreProtectedByRightObject() throws IOException {
        var aclId = addUserToAclGroupWithPermissions("deleteTwoOsdsThatAreProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclById(aclId))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-right-protected-relations");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("right-protected-delete", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteTwoOsdsWithUnprotectedRelation() throws IOException {
        var aclId = addUserToAclGroupWithPermissions("deleteTwoOsdsWithUnprotectedRelation", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclById(aclId))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("has-right-unprotected-relations");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("un-protected-delete", false, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId(), leftOsd.getId()), false);
    }

    @Test
    public void deleteOsdThatIsProtectedByRightObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteOsdThatIsProtectedByRightObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("right-only-protected-delete", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        assertClientError(() -> client.deleteOsds(List.of(rightOsd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_PROTECTED_RELATIONS);
    }


    @Test
    public void deleteOsdThatIsNotProtectedByRightObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsNotProtectedByRightObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteOsdThatIsNotProtectedByRightObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("right-only-protected-delete2", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(rightOsd.getId()), false);
    }

    @Test
    public void deleteOsdThatIsNotProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteOsdThatIsNotProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteOsdThatIsNotProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("left-only-protected-delete2", false, true, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        client.deleteOsds(List.of(leftOsd.getId()), false);
    }

    @Test
    public void deleteLeftOsdThatIsProtectedByLeftObject() throws IOException {
        addUserToAclGroupWithPermissions("deleteLeftOsdThatIsProtectedByLeftObject", List.of(READ_OBJECT_SYS_METADATA,
                RELATION_CHILD_ADD, RELATION_PARENT_ADD, DELETE));
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("deleteLeftOsdThatIsProtectedByLeftObject"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        ObjectSystemData leftOsd = toh.osd;
        toh.createOsd("right-osd-protected-by-left");
        ObjectSystemData rightOsd = toh.osd;

        RelationType rt = new RelationType("left-only-protected-delete", true, false, false, false, false, false);
        rt = adminClient.createRelationTypes(List.of(rt)).get(0);
        client.createRelation(leftOsd.getId(), rightOsd.getId(), rt.getId(), "<meta/>");
        assertClientError(() -> client.deleteOsds(List.of(leftOsd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_HAS_PROTECTED_RELATIONS);
    }

    @Test
    public void deleteWithLockedObject() throws IOException {
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        var osd = toh.osd;
        adminClient.lockOsd(osd.getId());
        assertClientError(() -> client.deleteOsds(List.of(osd.getId()), false),
                CANNOT_DELETE_DUE_TO_ERRORS, OBJECT_LOCKED_BY_OTHER_USER);
    }

    @Test
    public void deleteWithLockedObjectsAsAdmin() throws IOException {
        var toh = new TestObjectHolder(adminClient);
        toh.setAcl(client.getAclByName("reviewers.acl"))
                .setUser(userId)
                .setFolder(createFolderId)
                .createOsd("protects-right");
        var osd = toh.osd;
        adminClient.lockOsd(osd.getId());
        adminClient.deleteOsds(List.of(osd.getId()), false);
    }

    @Test
    public void deleteWithLockedObjectByCurrentUser() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT, LOCK, DELETE))
                .createOsd();
        var osd = toh.osd;
        client.lockOsd(osd.getId());
        client.deleteOsds(List.of(osd.getId()), false);
    }

    @Test
    public void getOsdWithCustomMetadata() throws IOException {
        var toh     = new TestObjectHolder(client, userId);
        var content = "<xml>test</xml>";
        toh.createOsd("with-custom-meta").createOsdMeta(content);
        ObjectSystemData osd = client.getOsdById(toh.osd.getId(), false, true);
        assertEquals(content, osd.getMetas().get(0).getContent());
    }

    @Test
    public void getOsdWithUnbrowsableCustomMetadata() throws IOException {
        var content = "<xml>test</xml>";
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE))
                .createOsd("with-custom-meta")
                .createOsdMeta(content);
        assertClientError(() -> client.getOsdMetas(toh.osd.getId()),
                NO_READ_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void getOsdByFolderWithCustomMetadata() throws IOException {
        var content = "<xml>test</xml>";
        var toh = new TestObjectHolder(client, userId)
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
                .addPermissionsByName(List.of(BROWSE.getName()))
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
        assertClientError(() -> client.copyOsds(createFolderId, List.of(toh.osd.getId())),
                NO_READ_OBJECT_SYS_METADATA_PERMISSION);
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
        adminClient.attachLifecycle(id, lifecycle.getId(), startState.getId(), false);
        ObjectSystemData copy = adminClient.copyOsds(createFolderId, List.of(id), List.of()).get(0);
        assertEquals(copyState.getId(), copy.getLifecycleStateId());
    }

    @Test
    public void deleteOsdWithContent() throws IOException, InterruptedException {
        var toh = new TestObjectHolder(adminClient, adminId);
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
    public void newVersionHasValidRootId() throws IOException {
        // new version of an OSD should have predecessor as rootId:
        ObjectSystemData osd = client.createOsd(new CreateOsdRequest("osd for new-version with root id test", createFolderId, userId, 1L,
                1L, null, 1L, null, "<sum/>"));
        ObjectSystemData newVersion = adminClient.version(new CreateNewVersionRequest(osd.getId()));
        assertEquals(osd.getId(), newVersion.getRootId());
    }

    @Test
    public void newObjectIsLatestHead() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(client, userId);
        var              osd = toh.createOsd("should-be-latest-head").osd;
        assertTrue(osd.isLatestHead());
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

    @Test
    public void fetchLargeCountOfOsds() throws IOException {
        List<Long> ids = new ArrayList<>(2000);
        for (long i = 0; i < 2000; i++) {
            ids.add(i);
        }
        // exercise the batchMode of CrudDao.partitionLongList()
        client.getOsds(ids, false, false);
        // TODO: maybe create PerformanceIntegrationTest and actually measure how long it takes to do CRUD on a 1000+ objects
    }

    // TODO: maybe move OsdMeta & FolderMeta into a separate test and remove redundant tests?
    // on the other hand, below the MetaService used by both, there are separate database tables
    // for osd_meta and folder_meta, so it's a good idea to verify both work.
    @Test
    public void updateOsdMetaHappyPath() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd()
                .createOsdMeta("some meta content");
        Meta meta = toh.meta;
        meta.setContent("updated meta");
        client.updateOsdMeta(meta);
        Meta updatedMeta = client.getOsdMetas(toh.osd.getId()).get(0);
        assertEquals(meta, updatedMeta);
    }

    @Test
    public void updateOsdMetaNoWritePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE))
                .createOsd()
                .createOsdMeta("some meta content");
        Meta meta = toh.meta;
        meta.setContent("updated meta");
        assertClientError(() -> client.updateOsdMeta(meta), NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void updateOsdMetaUpdateOsdIdFail() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd()
                .createOsdMeta("some meta content");
        Meta meta = toh.meta;
        meta.setObjectId(1L);
        assertClientError(() -> client.updateOsdMeta(meta), INVALID_UPDATE);
    }

    @Test
    public void updateOsdMetaUpdateTypeFail() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd()
                .createOsdMeta("some meta content");
        Meta meta = toh.meta;
        meta.setTypeId(Long.MAX_VALUE);
        assertClientError(() -> client.updateOsdMeta(meta), INVALID_UPDATE);
    }

    @Test
    public void updateOsdMetaUpdateMetaNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd()
                .createOsdMeta("some meta content");
        Meta meta = toh.meta;
        meta.setId(Long.MAX_VALUE);
        assertClientError(() -> client.updateOsdMeta(meta), METASET_NOT_FOUND);
    }

    @Test
    public void updateOsdMetaInvalidRequest() {
        assertClientError(() -> client.updateOsdMeta(new Meta()), INVALID_REQUEST);
    }

}
