package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.ALIAS_OWNER;
import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class LinkServletIntegrationTest extends CinnamonIntegrationTest {

    private LinkResponseWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        return mapper.readValue(response.getEntity().getContent(), LinkResponseWrapper.class);
    }

    @Test
    public void getLinkByIdForObject() throws IOException {
        String summary = "<summary>sum of a sum</summary>";
        var toh = prepareAclGroupWithPermissions("getLinkByIdForObject",
                List.of(BROWSE_OBJECT, WRITE_OBJECT_SYS_METADATA));
        ObjectSystemData linkTarget = toh.createOsd("getLinkByIdForObject-target").osd;
        Long linkId = toh.createFolder("getLinkByIdForObject", createFolderId)
                .createLinkToOsd(linkTarget)
                .link.getId();
        client.setSummary(linkTarget.getId(), summary);
        Link link = new Link(client.getLinkById(linkId, false));

        assertNull(link.getFolderId());
        assertEquals(LinkType.OBJECT,link.getType());
        assertNotNull(link.getObjectId());
        ObjectSystemData osd = client.getOsdById(link.getObjectId(), true, false);
        assertEquals(summary, osd.getSummary());
    }

    @Test
    public void getLinkByIdForObjectWithoutSummary() throws IOException {
        var toh = prepareAclGroupWithPermissions("getLinkByIdForObjectWithoutSummary",
                List.of(BROWSE_OBJECT));
        ObjectSystemData linkTarget = toh.createOsd("getLinkByIdForObjectWithoutSummary-target").osd;
        Long linkId = toh.createFolder("getLinkByIdForObjectWithoutSummary", createFolderId)
                .createLinkToOsd(linkTarget)
                .link.getId();
        Link link = new Link(client.getLinkById(linkId, false));
        ObjectSystemData osd          = client.getOsdById(link.getObjectId(), false, false);
        assertEquals(DEFAULT_SUMMARY, osd.getSummary());
    }

    @Test
    public void getLinkByIdForFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions("getLinkByIdForFolder",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder("folderAsLinkTarget", createFolderId).folder;
        toh.createFolder("getLinkByIdForFolder", createFolderId)
                .createLinkToFolder(linkTarget);
        client.setFolderSummary(linkTarget.getId(), "<summary>stuff</summary>");
        client.getLinkById(toh.link.getId(), true);

        LinkResponse linkResponse = client.getLinkById(toh.link.getId(), true);
        assertNull(linkResponse.getObjectId());
        assertEquals(LinkType.FOLDER, linkResponse.getType());
        Folder folder = client.getFolderById(linkResponse.getFolderId(), true);
        assertNotNull(folder);
        assertThat(folder.getName(), equalTo("folderAsLinkTarget"));
        assertThat(folder.getSummary(), equalTo("<summary>stuff</summary>"));
    }

    @Test
    public void getLinkByIdForFolderWithoutSummary() throws IOException {
        var toh = prepareAclGroupWithPermissions("getLinkByIdForFolderWithoutSummary",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder("getLinkByIdForFolderWithoutSummary-target", createFolderId).folder;
        toh.createFolder("getLinkByIdForFolderWithoutSummary-container", createFolderId)
                .createLinkToFolder(linkTarget);
        client.setFolderSummary(linkTarget.getId(), "<summary>stuff</summary>");
        LinkResponse linkResponse = client.getLinkById(toh.link.getId(), true);
        Link link = new Link(linkResponse);
        assertEquals(LinkType.FOLDER,link.getType());
        Folder folder = client.getFolderById(link.getFolderId(), false);
        assertEquals(DEFAULT_SUMMARY, folder.getSummary());
    }

    @Test
    public void getLinkWhereOnlyOwnerHasBrowsePermissionForOsd() throws IOException {
        var toh = new TestObjectHolder(adminClient, "reviewers.acl", userId, createFolderId)
                .createAcl("owner-acl-for-link");
        // set current group to owner-group:
        toh.group = TestObjectHolder.groups.stream()
                .filter(group -> group.getName().equals(ALIAS_OWNER)).findFirst().orElseThrow();
        // join acl to owner group:
        toh.createAclGroup()
                .addUserToGroup(userId)
                // add browse permission to the given acl-owner-group combination:
                .addPermissions(List.of(BROWSE_OBJECT))
                // create link target
                .createOsd("getLinkWhereOnlyOwnerHasBrowsePermissionForOsd")
                // create folder which will contain the link (cannot have link + target in same folder)
                .createFolder("getLinkWhereOnlyOwnerHasBrowsePermissionForOsd", createFolderId);
        ;
        toh.createLinkToOsd(toh.osd);

        var linkResponse = client.getLinkById(toh.link.getId(), false);
        var link         = new Link(linkResponse);
        assertEquals(LinkType.OBJECT, link.getType());
    }

    @Test
    public void aclOnLinkForbidsAccess() throws IOException {
        var toh = prepareAclGroupWithPermissions("aclOnLinkForbidsAccess", List.of())
                .createOsd("aclOnLinkForbidsAccess")
                .createFolder("aclOnLinkForbidsAccess", createFolderId);
        toh.createLinkToOsd(toh.osd);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLinkById(toh.link.getId(), false));
        assertEquals(NO_BROWSE_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void aclOnObjectForbidsAccess() throws IOException {
        // we need 2 acls: the link itself is browsable, but the object behind it is not
        var toh = prepareAclGroupWithPermissions("aclOnObjectForbidsAccess", List.of())
                .createOsd("aclOnObjectForbidsAccess")
                .createFolder("aclOnObjectForbidsAccess", createFolderId);
        var toh2 = prepareAclGroupWithPermissions("aclOnObjectForbidsAccess2",
                List.of(BROWSE_OBJECT));
        toh2.createLinkToOsd(toh.osd);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLinkById(toh2.link.getId(), false));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void aclOnFolderForbidsAccess() throws IOException {
        // we need 2 acls: the link itself is browsable, but the folder behind it is not
        var toh          = prepareAclGroupWithPermissions("aclOnFolderForbidsAccess", List.of());
        var targetFolder = toh.createFolder("aclOnFolderForbidsAccess-target", createFolderId).folder;
        toh.createFolder("aclOnFolderForbidsAccess-container", createFolderId);
        var toh2 = prepareAclGroupWithPermissions("aclOnFolderForbidsAccess2",
                List.of(BROWSE_FOLDER));
        toh2.createLinkToFolder(targetFolder);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLinkById(toh2.link.getId(), false));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void deleteLinkWithInvalidId() {
        assertClientError(() -> client.deleteLinks(List.of(-1L)), INVALID_REQUEST);
    }

    @Test
    public void deleteLinkWithNonExistentLink() {
        assertClientError(() -> client.deleteLinks(List.of(Long.MAX_VALUE)), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteLinkWithMissingBrowsePermission() throws IOException {
        DeleteLinkRequest deleteRequest = new DeleteLinkRequest(11L);
        HttpResponse      response      = sendStandardRequest(UrlMapping.LINK__DELETE, deleteRequest);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void deleteLinkToObjectWithoutPermission() throws IOException {
        DeleteLinkRequest deleteRequest = new DeleteLinkRequest(12L);
        HttpResponse      response      = sendStandardRequest(UrlMapping.LINK__DELETE, deleteRequest);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void deleteLinkToObjectWithOwnerPermission() throws IOException {
        assertTrue(client.deleteLinks(List.of(15L)));
    }

    @Test
    public void deleteLinkToFolderWithOwnerPermission() throws IOException {
        assertTrue(client.deleteLinks(List.of(16L)));
    }

    @Test
    public void deleteLinkToFolderWithoutPermission() {
        assertClientError(() -> client.deleteLinks(List.of(13L)), ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void deleteObjectLinkHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions("deleteObjectLinkHappyPath",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, DELETE_OBJECT));
        ObjectSystemData linkTarget = toh.createOsd("deleteObjectLinkHappyPathTarget").osd;
        Long linkId = toh.createFolder("deleteObjectLinkHappyPath", createFolderId)
                .createLinkToOsd(linkTarget)
                .link.getId();
        assertTrue(client.deleteLinks(List.of(linkId)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(linkId), true), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteFolderLinkHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions("deleteFolderLinkHappyPath",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, DELETE_FOLDER));
        Folder linkTarget = toh.createFolder("deleteFolderLinkHappyPathTarget", createFolderId).folder;
        Long linkId = toh.createFolder("deleteFolderLinkHappyPath", createFolderId)
                .createLinkToFolder(linkTarget)
                .link.getId();
        assertTrue(client.deleteLinks(List.of(linkId)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(linkId), true), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void createLinkToObjectHappyPath() throws IOException {
        var linkParam = new Link(LinkType.OBJECT, 1L, 1L, 6L, null, 13L);
        var link      = client.createLinks(List.of(linkParam)).get(0);

        // link to object in creation folder#6
        assertThat(link.getOwnerId(), equalTo(1L));
        assertThat(link.getParentId(), equalTo(6L));
        assertThat(link.getType(), equalTo(LinkType.OBJECT));
        assertThat(link.getAclId(), equalTo(1L));
        assertThat(link.getObjectId(), equalTo(13L));
    }

    @Test
    public void createLinkToFolderHappyPath() throws IOException {
        // link to folder in creation folder#9
        CreateLinkRequest   createLinkRequest = new CreateLinkRequest(6, LinkType.FOLDER, 1, 1, 9L, null);
        HttpResponse        response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        LinkResponseWrapper linkWrapper       = parseResponse(response);
        Link                linkResponse      = linkWrapper.getLinks().get(0);

        assertThat(linkResponse.getOwnerId(), equalTo(1L));
        assertThat(linkResponse.getParentId(), equalTo(6L));
        assertThat(linkResponse.getType(), equalTo(LinkType.FOLDER));
        assertThat(linkResponse.getAclId(), equalTo(1L));
        assertThat(linkResponse.getFolderId(), equalTo(9L));
    }

    @Test
    public void createLinkWithInvalidRequest() throws IOException {
        // invalid target id
        assertClientError(() -> client.createLinkToOsd(6L, 1L, 1L, 0L), INVALID_REQUEST);

        // invalid parent folder id
        CreateLinkRequest crlParentId      = new CreateLinkRequest(0L, LinkType.OBJECT, 1, 1, null, 13L);
        HttpResponse      responseParentId = sendStandardRequest(UrlMapping.LINK__CREATE, crlParentId);
        assertCinnamonError(responseParentId, INVALID_REQUEST);

        // invalid link type
        CreateLinkRequest crlLinkType      = new CreateLinkRequest(6L, null, 1, 1, null, 13L);
        HttpResponse      responseLinkType = sendStandardRequest(UrlMapping.LINK__CREATE, crlParentId);
        assertCinnamonError(responseLinkType, INVALID_REQUEST);

        // invalid link type
        CreateLinkRequest crlAcl      = new CreateLinkRequest(6L, LinkType.OBJECT, 0, 1, null, 13L);
        HttpResponse      responseAcl = sendStandardRequest(UrlMapping.LINK__CREATE, crlParentId);
        assertCinnamonError(responseAcl, INVALID_REQUEST);

        // invalid ownerId
        CreateLinkRequest crlOwnerId      = new CreateLinkRequest(6L, LinkType.OBJECT, 1, 0, null, 13L);
        HttpResponse      responseOwnerId = sendStandardRequest(UrlMapping.LINK__CREATE, crlOwnerId);
        assertCinnamonError(responseOwnerId, INVALID_REQUEST);
    }

    @Test
    public void createLinkWithUnknownParentFolder() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(Long.MAX_VALUE, LinkType.OBJECT, 1, 1, null, 13L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void createLinkWithUnknownAcl() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(6L, LinkType.OBJECT, Long.MAX_VALUE, 1, null, 13L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.ACL_NOT_FOUND);
    }

    @Test
    public void createLinkWithUnknownOwner() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(6L, LinkType.OBJECT, 1, Long.MAX_VALUE, null, 13L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.OWNER_NOT_FOUND);
    }

    @Test
    public void createLinkWithinParentFolderWithoutBrowsePermission() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(7, LinkType.OBJECT, 1, 1, null, 13L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void createLinkWithinParentFolderWithoutCreatePermission() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(8, LinkType.OBJECT, 1, 1, null, 13L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void createLinkToFolderWithoutBrowseFolderPermission() throws IOException {
        var toh    = prepareAclGroupWithPermissions("createLinkToFolderWithoutBrowseFolderPermission", List.of());
        var folder = toh.createFolder("createLinkToFolderWithoutBrowsePermission", createFolderId);
        var ex     = assertThrows(CinnamonClientException.class, () -> client.createLinkToFolder(createFolderId, 1L, adminId, toh.folder.getId()));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void createLinkToFolderWithBrowsePermissionByOwner() throws IOException {
        CreateLinkRequest   crlId        = new CreateLinkRequest(6, LinkType.FOLDER, 5, 2, 10L, null);
        HttpResponse        response     = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        LinkResponseWrapper linkWrapper  = parseResponse(response);
        Link                linkResponse = linkWrapper.getLinks().get(0);
        assertThat(linkResponse.getFolderId(), equalTo(10L));
    }

    @Test
    public void createLinkToObjectWithoutBrowsePermission() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(6, LinkType.OBJECT, 1, 1, null, 4L);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void createLinkToObjectWithBrowsePermissionByOwner() throws IOException {
        CreateLinkRequest   crlId        = new CreateLinkRequest(6, LinkType.OBJECT, 5, 2, null, 5L);
        HttpResponse        response     = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        LinkResponseWrapper linkWrapper  = parseResponse(response);
        Link                linkResponse = linkWrapper.getLinks().get(0);
        assertThat(linkResponse.getObjectId(), equalTo(5L));
    }

    @Test
    public void createLinkWithNonExistentTargetObject() throws IOException {
        CreateLinkRequest createLinkRequest = new CreateLinkRequest(6L, LinkType.OBJECT, 1, 1, null, Long.MAX_VALUE);
        HttpResponse      response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void createLinkWithNonExistentTargetFolder() throws IOException {
        CreateLinkRequest createLinkRequest = new CreateLinkRequest(6, LinkType.FOLDER, 1, 1, Long.MAX_VALUE, null);
        HttpResponse      response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        assertCinnamonError(response, ErrorCode.FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkWithInvalidRequest() {
        assertClientError(() -> client.updateLinks(List.of(new Link())), INVALID_REQUEST);
    }

    @Test
    public void updateLinkWithoutBrowsePermission() throws IOException {
        List<LinkResponse> links = adminClient.getLinksById(List.of(17L), false);
        Link               link  = new Link(links.get(0));
        link.setOwnerId(1L);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkWithoutPermission() throws IOException {
        var link = client.getLinkById(1L, false);
        link.setOwnerId(1L);
        assertClientError(() -> client.updateLink(link), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void updateLinkAclWithoutSetAclPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("updateLinkAclWithoutSetAclPermission",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create an OSD, then a folder and inside a link to the osd.
        // Then try to update the link's ACL without permission:
        toh.createOsd("osdAsLinkTarget")
                .createFolder("updateLinkAclWithoutSetAclPermission", createFolderId)
                .createLinkToOsd(toh.osd)
                .link.setAclId(1L);
        assertClientError(() -> client.updateLink(toh.link), MISSING_SET_ACL_PERMISSION);
    }

    @Test
    public void updateLinkAclWithPermission() throws IOException {
        var link = client.getLinkById(19L, false);
        link.setAclId(1L);
        Link updateLink = client.updateLink(new Link(link));
        assertEquals(new Link(link), updateLink);
    }

    @Test
    public void updateLinkAclWithNonExistentAcl() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setAclId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), ACL_NOT_FOUND);
    }

    @Test
    public void updateLinkFolderWithNonExistentFolder() throws IOException {
        var linkResponse = client.getLinkById(20L, false);
        linkResponse.setFolderId(Long.MAX_VALUE);
        var link = new Link(linkResponse);
        assertClientError(() -> client.updateLink(link), FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkFolderWithoutFolderBrowsePermission() throws IOException {
        var link = client.getLinkById(20L, false);
        // try to link to "unseen-folder" #3
        link.setFolderId(3L);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkOwnerWithNonExistentOwner() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setOwnerId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), OWNER_NOT_FOUND);
    }

    @Test
    public void updateLinkOwner() throws IOException {
        var toh = prepareAclGroupWithPermissions("updateLinkOwner",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder("updateLinkOwnerTarget", createFolderId).folder;
        toh.createFolder("updateLinkOwner", createFolderId)
                .createLinkToFolder(linkTarget);
        var linkResponse = client.getLinkById(toh.link.getId(), true);
        linkResponse.setOwnerId(adminId);
        var  link        = new Link(linkResponse);
        Link updatedLink = client.updateLink(link);
        assertEquals(link, updatedLink);
    }

    @Test
    public void updateLinkParentIntoFolderWithoutBrowsePermission() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setParentId(7L);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkParentIntoFolderWithoutCreatePermission() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setParentId(8L);
        assertClientError(() -> client.updateLink(link), NO_CREATE_PERMISSION);
    }

    @Test
    public void updateLinkParentIntoFolderWithNonExistentParentFolder() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setParentId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkParentFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions("updateLinkParentFolder",
                List.of(BROWSE_OBJECT, BROWSE_FOLDER, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget  = toh.createFolder("updateLinkParentFolderTarget", createFolderId).folder;
        Long   newParentId = toh.createFolder("parent folder for update", linkTarget.getId()).folder.getId();
        toh.createFolder("updateLinkParentFolder", createFolderId)
                .createLinkToFolder(linkTarget);
        var linkResponse = client.getLinkById(toh.link.getId(), true);
        linkResponse.setParentId(newParentId);
        var link = new Link(linkResponse);
        assertEquals(link, client.updateLink(link));
    }

    @Test
    public void updateLinkToObject() throws IOException {
        var toh = prepareAclGroupWithPermissions("updateLinkToObject", List.of(
                BROWSE_FOLDER, CREATE_OBJECT, BROWSE_OBJECT, WRITE_OBJECT_SYS_METADATA
        ));

        long osdId        = toh.createOsd("updateLinkToObject").osd.getId();
        long linkFolderId = toh.createFolder("folder with a link", createFolderId).folder.getId();
        Link link         = client.createLinkToOsd(linkFolderId, toh.acl.getId(), userId, osdId);
        long secondOsdId  = toh.createOsd("second-link-osd").osd.getId();
        var  linkResponse = client.getLinkById(link.getId(), false);
        linkResponse.setObjectId(secondOsdId);
        assertEquals(new Link(linkResponse), client.updateLink(new Link(linkResponse)));
    }

    @Test
    public void updateLinkToObjectFromFolderIsForbidden() throws IOException {
        var linkResponse = client.getLinkById(22L, false);
        var link         = new Link(linkResponse);
        link.setType(LinkType.OBJECT);
        link.setObjectId(13L);
        link.setFolderId(null);
        assertClientError(() -> client.updateLink(link), CANNOT_CHANGE_LINK_TYPE);
    }

    @Test
    public void updateLinkToFolderFromObjectIsForbidden() throws IOException {
        var link = client.getLinkById(23L, false);
        link.setType(LinkType.FOLDER);
        link.setFolderId(6L);
        link.setObjectId(null);
        assertClientError(() -> client.updateLink(link), CANNOT_CHANGE_LINK_TYPE);
    }

    @Test
    public void updateLinkToObjectWithNonExistentObject() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setObjectId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), OBJECT_NOT_FOUND);
    }

    @Test
    public void updateLinkToObjectWithUnbrowsableObject() throws IOException {
        var link = client.getLinkById(21L, false);
        link.setObjectId(4L);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }


}
