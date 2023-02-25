package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.ALIAS_OWNER;
import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;
import static com.dewarim.cinnamon.test.TestObjectHolder.defaultCreationFolderId;
import static org.junit.jupiter.api.Assertions.*;


public class LinkServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void getLinkByIdForObject() throws IOException {
        String           summary    = "<summary>sum of a sum</summary>";
        var              toh        = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA));
        ObjectSystemData linkTarget = toh.createOsd().osd;
        Long linkId = toh.createFolder()
                .createLinkToOsd(linkTarget)
                .link.getId();
        client.setSummary(linkTarget.getId(), summary);
        Link link = new Link(client.getLinkById(linkId, false));

        assertNull(link.getFolderId());
        assertEquals(LinkType.OBJECT, link.getType());
        assertNotNull(link.getObjectId());
        ObjectSystemData osd = client.getOsdById(link.getObjectId(), true, false);
        assertEquals(summary, osd.getSummary());
    }

    @Test
    public void getLinkByIdForObjectWithoutSummary() throws IOException {
        var              toh        = prepareAclGroupWithPermissions(List.of(BROWSE));
        ObjectSystemData linkTarget = toh.createOsd().osd;
        Long linkId = toh.createFolder()
                .createLinkToOsd(linkTarget)
                .link.getId();
        Link             link = new Link(client.getLinkById(linkId, false));
        ObjectSystemData osd  = client.getOsdById(link.getObjectId(), false, false);
        assertEquals(DEFAULT_SUMMARY, osd.getSummary());
    }

    @Test
    public void getLinkByIdForFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder().folder;
        assertNotNull(client.getFolderById(linkTarget.getId(), false));

        Link link = toh.createFolder()
                .createLinkToFolder(linkTarget).link;
        client.setFolderSummary(linkTarget.getId(), "<summary>stuff</summary>");
        var linkResponse = client.getLinkById(link.getId(), true);
        assertNull(linkResponse.getObjectId());
        assertEquals(LinkType.FOLDER, linkResponse.getType());

        Folder folder = client.getFolderById(linkResponse.getFolderId(), true);
        assertNotNull(folder);
        assertEquals(linkTarget.getName(), folder.getName());
        assertEquals("<summary>stuff</summary>", folder.getSummary());
    }

    @Test
    public void getLinkByIdForFolderWithoutSummary() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder(createFolderId).folder;
        toh.createFolder(createFolderId)
                .createLinkToFolder(linkTarget);
        client.setFolderSummary(linkTarget.getId(), "<summary>stuff</summary>");
        LinkResponse linkResponse = client.getLinkById(toh.link.getId(), true);
        Link         link         = new Link(linkResponse);
        assertEquals(LinkType.FOLDER, link.getType());
        Folder folder = client.getFolderById(link.getFolderId(), false);
        assertEquals(DEFAULT_SUMMARY, folder.getSummary());
    }

    @Test
    public void getLinkWhereOnlyOwnerHasBrowsePermissionForOsd() throws IOException {
        var toh = new TestObjectHolder(adminClient, userId)
                .createAcl("owner-acl-for-link");
        // set current group to owner-group:
        toh.group = TestObjectHolder.groups.stream()
                .filter(group -> group.getName().equals(ALIAS_OWNER)).findFirst().orElseThrow();
        // join acl to owner group:
        toh.createAclGroup()
                .addUserToGroup(userId)
                // add browse permission to the given acl-owner-group combination:
                .addPermissions(List.of(BROWSE))
                // create link target
                .createOsd()
                // create folder which will contain the link (cannot have link + target in same folder)
                .createFolder();
        toh.createLinkToOsd(toh.osd);

        var linkResponse = client.getLinkById(toh.link.getId(), false);
        var link         = new Link(linkResponse);
        assertEquals(LinkType.OBJECT, link.getType());
    }

    @Test
    public void aclOnLinkForbidsAccess() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .createFolder();
        toh.createLinkToOsd(toh.osd);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLinkById(toh.link.getId(), false));
        assertEquals(NO_BROWSE_PERMISSION, ex.getErrorCode());
    }

    @Disabled("fetching a link to an object that is no longer accessible is not wrong per se,")
    @Test
    public void aclOnObjectForbidsAccess() throws IOException {
        // we need 2 acls: the link itself is browsable, but the object behind it is not
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .createFolder();
        var toh2 = prepareAclGroupWithPermissions(List.of(BROWSE));
        toh2.createLinkToOsd(toh.osd);
        assertClientError(() -> client.getLinkById(toh2.link.getId(), false), UNAUTHORIZED);
    }

    @Test
    public void aclOnFolderForbidsAccess() throws IOException {
        // we need 2 acls: the link itself is browsable, but the folder behind it is not
        var toh          = prepareAclGroupWithPermissions(List.of());
        var targetFolder = toh.createFolder().folder;
        toh.createFolder();
        var toh2 = prepareAclGroupWithPermissions(List.of(BROWSE));
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
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .createFolder();
        var linkId = toh.createLinkToOsd(toh.osd).link.getId();
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.deleteLinks(List.of(linkId)));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void deleteLinkToObjectWithoutPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE))
                .createOsd()
                .createFolder();
        toh.createLinkToOsd(toh.osd);
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteLinks(List.of(toh.link.getId())));
        assertEquals(NO_DELETE_LINK_PERMISSION, ex.getErrorCode());

    }

    @Test
    public void deleteLinkToObjectWithOwnerPermission() throws IOException {
        var toh = prepareAclGroupWithOwnerPermissions(
                List.of(BROWSE, CREATE_OBJECT, DELETE))
                .createOsd()
                .createFolder();
        Link link = client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), userId, toh.osd.getId());
        assertTrue(client.deleteLinks(List.of(link.getId())));
    }

    @Test
    public void deleteLinkToFolderWithOwnerPermission() throws IOException {
        var toh = prepareAclGroupWithOwnerPermissions(
                List.of(BROWSE, CREATE_FOLDER, CREATE_OBJECT, DELETE))
                .createFolder();
        var targetFolder = toh.folder;
        toh.createFolder();

        Link link = client.createLinkToFolder(toh.folder.getId(), toh.acl.getId(), userId, targetFolder.getId());
        assertTrue(client.deleteLinks(List.of(link.getId())));
    }

    @Test
    public void deleteLinkToFolderWithoutPermission() throws IOException {
        var toh          = prepareAclGroupWithPermissions(List.of(BROWSE));
        var targetFolder = toh.createFolder().folder;
        toh.createFolder(defaultCreationFolderId)
                .createLinkToFolder(client.getFolderById(targetFolder.getId(), false));
        assertClientError(() -> client.deleteLinks(List.of(toh.link.getId())), NO_DELETE_LINK_PERMISSION);
    }

    @Test
    public void deleteObjectLinkHappyPath() throws IOException {
        var              toh        = prepareAclGroupWithPermissions(List.of(BROWSE, DELETE));
        ObjectSystemData linkTarget = toh.createOsd().osd;
        Long linkId = toh.createFolder()
                .createLinkToOsd(linkTarget)
                .link.getId();
        assertTrue(client.deleteLinks(List.of(linkId)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(linkId), true), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteFolderLinkHappyPath() throws IOException {
        var    toh        = prepareAclGroupWithPermissions(List.of(BROWSE, DELETE));
        Folder linkTarget = toh.createFolder(createFolderId).folder;
        Long linkId = toh.createFolder()
                .createLinkToFolder(linkTarget)
                .link.getId();
        assertTrue(client.deleteLinks(List.of(linkId)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(linkId), true), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void createLinkToObjectHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        ObjectSystemData osd  = toh.osd;
        Link             link = client.createLinkToOsd(toh.folder.getId(), defaultCreationAcl.getId(), userId, osd.getId());

        assertEquals(osd.getId(), link.getObjectId());
        assertEquals(toh.folder.getId(), link.getParentId());
        assertEquals(LinkType.OBJECT, link.getType());
        assertEquals(defaultCreationAcl.getId(), link.getAclId());
        assertEquals(userId, link.getOwnerId());
        assertNull(link.getFolderId());
    }

    @Test
    public void createLinkToFolderHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        var  targetFolder    = toh.folder;
        var  containerFolder = toh.createFolder(defaultCreationFolderId).folder;
        Link link            = client.createLinkToFolder(containerFolder.getId(), defaultCreationAcl.getId(), userId, targetFolder.getId());

        assertEquals(targetFolder.getId(), link.getFolderId());
        assertEquals(containerFolder.getId(), link.getParentId());
        assertEquals(LinkType.FOLDER, link.getType());
        assertEquals(defaultCreationAcl.getId(), link.getAclId());
        assertEquals(userId, link.getOwnerId());
        assertNull(link.getObjectId());
    }

    // TODO: refactor to _not_ use hardcoded ids
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
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createOsd();
        assertClientError(() -> client.createLinkToOsd(Long.MAX_VALUE, toh.acl.getId(), userId, toh.osd.getId()), PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void createLinkWithUnknownAcl() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        assertClientError(() -> client.createLinkToOsd(toh.folder.getId(), Long.MAX_VALUE, userId, toh.osd.getId()), ACL_NOT_FOUND);
    }

    @Test
    public void createLinkWithUnknownOwner() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        assertClientError(() -> client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), Long.MAX_VALUE, toh.osd.getId()), OWNER_NOT_FOUND);
    }

    @Test
    public void createLinkWithinParentFolderWithoutBrowsePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd();
        assertClientError(() -> client.createLinkToOsd(createFolderId, toh.acl.getId(), userId, toh.osd.getId()), UNAUTHORIZED);
    }

    @Test
    public void createLinkWithinParentFolderWithoutCreatePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE))
                .createOsd()
                .createFolder();
        assertClientError(() -> client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), userId, toh.osd.getId()), UNAUTHORIZED);
    }

    @Test
    public void createLinkToFolderWithoutBrowseFolderPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createFolder(createFolderId);
        var ex = assertThrows(CinnamonClientException.class, () -> client.createLinkToFolder(createFolderId, toh.acl.getId(), adminId, toh.folder.getId()));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void createLinkToFolderWithBrowsePermissionByOwner() throws IOException {
        var toh = prepareAclGroupWithOwnerPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        var  targetFolder    = toh.folder;
        var  containerFolder = toh.createFolder().folder;
        Link link            = client.createLinkToFolder(containerFolder.getId(), toh.acl.getId(), userId, targetFolder.getId());
        assertEquals(LinkType.FOLDER, link.getType());
        assertEquals(toh.folder.getId(), link.getParentId());
        assertEquals(targetFolder.getId(), link.getFolderId());
        assertNull(link.getObjectId());
    }

    @Test
    public void createLinkToObjectWithoutBrowsePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .createFolder();
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), userId, toh.osd.getId()));
        assertEquals(UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    public void createLinkToObjectWithBrowsePermissionByOwner() throws IOException {
        var toh = prepareAclGroupWithOwnerPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createOsd()
                .createFolder();
        Link link = client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), userId, toh.osd.getId());
        assertEquals(LinkType.OBJECT, link.getType());
        assertEquals(toh.folder.getId(), link.getParentId());
        assertEquals(toh.osd.getId(), link.getObjectId());
        assertNull(link.getFolderId());
    }

    @Test
    public void createLinkWithNonExistentTargetObject() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createFolder();
        assertClientError(() -> client.createLinkToOsd(toh.folder.getId(), toh.acl.getId(), userId, Long.MAX_VALUE),
                OBJECT_NOT_FOUND);
    }

    @Test
    public void createLinkWithNonExistentTargetFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, CREATE_OBJECT))
                .createFolder();
        assertClientError(() -> client.createLinkToFolder(toh.folder.getId(), toh.acl.getId(), userId, Long.MAX_VALUE),
                FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkWithInvalidRequest() {
        assertClientError(() -> client.updateLinks(List.of(new Link())), INVALID_REQUEST);
    }

    @Test
    public void updateLinkWithoutBrowsePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .createFolder();
        var link = toh.createLinkToOsd(toh.osd).link;
        link.setOwnerId(1L);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkWithoutPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER));
        Link link = toh.createOsd()
                .createFolder()
                .createLinkToOsd(toh.osd)
                .link;
        link.setOwnerId(1L);
        assertClientError(() -> client.updateLink(link), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void updateLinkAclWithoutSetAclPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create an OSD, then a folder and inside a link to the osd.
        // Then try to update the link's ACL without permission:
        toh.createOsd()
                .createFolder()
                .createLinkToOsd(toh.osd)
                .link.setAclId(1L);
        assertClientError(() -> client.updateLink(toh.link), MISSING_SET_ACL_PERMISSION);
    }

    @Test
    public void updateLinkAclWithPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA, SET_ACL))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        link.setAclId(1L);
        Link updateLink = client.updateLink(link);
        assertEquals(link, updateLink);
    }

    @Test
    public void updateLinkAclWithNonExistentAcl() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA, SET_ACL))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        link.setAclId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), ACL_NOT_FOUND);
    }

    @Test
    public void updateLinkFolderWithNonExistentFolder() throws IOException {
        var toh          = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA));
        var targetFolder = toh.createFolder().folder;
        var link = toh.createFolder()
                .createLinkToFolder(targetFolder).link;
        link.setFolderId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkFolderWithoutFolderBrowsePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA));
        var targetFolder = toh
                .createFolder()
                .folder;
        var link = toh
                .createFolder()
                .createLinkToFolder(targetFolder).link;
        var invisibleFolderId = prepareAclGroupWithPermissions(List.of())
                .createFolder()
                .folder.getId();
        link.setFolderId(invisibleFolderId);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkOwnerWithNonExistentOwner() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        link.setOwnerId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), OWNER_NOT_FOUND);
    }

    @Test
    public void updateLinkOwner() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget = toh.createFolder().folder;
        toh.createFolder()
                .createLinkToFolder(linkTarget);
        var linkResponse = client.getLinkById(toh.link.getId(), true);
        linkResponse.setOwnerId(adminId);
        var  link        = new Link(linkResponse);
        Link updatedLink = client.updateLink(link);
        assertEquals(link, updatedLink);
    }

    @Test
    public void updateLinkParentIntoFolderWithoutBrowsePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        var invisibleFolderId = prepareAclGroupWithPermissions(List.of())
                .createFolder()
                .folder.getId();
        link.setParentId(invisibleFolderId);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }

    @Test
    public void updateLinkParentIntoFolderWithoutCreatePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        var uNoCreateFolderId = prepareAclGroupWithPermissions(List.of(BROWSE))
                .createFolder(createFolderId)
                .folder.getId();
        link.setParentId(uNoCreateFolderId);
        assertClientError(() -> client.updateLink(link), NO_CREATE_PERMISSION);
    }

    @Test
    public void updateLinkParentIntoFolderWithNonExistentParentFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        link.setParentId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), FOLDER_NOT_FOUND);
    }

    @Test
    public void updateLinkParentFolder() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, CREATE_OBJECT, CREATE_FOLDER, WRITE_OBJECT_SYS_METADATA));
        // Create target folder, then a folder and inside a link to the target folder.
        Folder linkTarget  = toh.createFolder().folder;
        Long   newParentId = toh.createFolder("parent folder for update", linkTarget.getId()).folder.getId();
        toh.createFolder()
                .createLinkToFolder(linkTarget);
        var linkResponse = client.getLinkById(toh.link.getId(), true);
        linkResponse.setParentId(newParentId);
        var link = new Link(linkResponse);
        assertEquals(link, client.updateLink(link));
    }

    @Test
    public void updateLinkToObject() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(
                BROWSE, CREATE_OBJECT, WRITE_OBJECT_SYS_METADATA
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
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        var targetFolder = toh.folder;
        var link         = toh.createFolder().createLinkToFolder(toh.folder).link;
        link.setType(LinkType.OBJECT);
        link.setFolderId(null);
        link.setObjectId(toh.osd.getId());
        // change of link type from folder to object is forbidden:
        assertClientError(() -> client.updateLink(link), CANNOT_CHANGE_LINK_TYPE);
    }

    @Test
    public void updateLinkToFolderFromObjectIsForbidden() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        var targetFolder = toh.createFolder(createFolderId).folder;
        var link         = toh.createLinkToOsd(toh.osd).link;

        link.setType(LinkType.FOLDER);
        link.setFolderId(targetFolder.getId());
        link.setObjectId(null);
        assertClientError(() -> client.updateLink(link), CANNOT_CHANGE_LINK_TYPE);
    }

    @Test
    public void updateLinkToObjectWithNonExistentObject() throws IOException {
        var toh = prepareAclGroupWithPermissions(
                List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        var link = toh.createLinkToOsd(toh.osd).link;
        link.setObjectId(Long.MAX_VALUE);
        assertClientError(() -> client.updateLink(link), OBJECT_NOT_FOUND);
    }

    @Test
    public void updateLinkToObjectWithUnbrowsableObject() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_SYS_METADATA))
                .createOsd()
                .createFolder();
        Link link = toh.createLinkToOsd(toh.osd).link;
        var invisibleOsdId = prepareAclGroupWithPermissions(List.of())
                .createOsd()
                .osd.getId();
        link.setObjectId(invisibleOsdId);
        assertClientError(() -> client.updateLink(link), NO_BROWSE_PERMISSION);
    }


}
