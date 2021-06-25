package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class LinkServletIntegrationTest extends CinnamonIntegrationTest {

    private LinkResponseWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        return mapper.readValue(response.getEntity().getContent(), LinkResponseWrapper.class);
    }

    @Test
    public void getLinkByIdForObject() throws IOException {
        // request the first link, which points to the first test object with default acl:
        GetLinksRequest     linkRequest = new GetLinksRequest(1L, true);
        HttpResponse        response    = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, linkRequest);
        LinkResponseWrapper linkWrapper = parseResponse(response);
        assertThat(linkWrapper.getLinks().size(), equalTo(1));
        Link link = linkWrapper.getLinks().get(0);
        assertThat(link.getFolderId(), nullValue());
        assertThat(link.getType(), equalTo(LinkType.OBJECT));
        assertNotNull(link.getObjectId());
        ObjectSystemData osd = client.getOsdById(link.getObjectId(), true);
        assertThat(osd.getSummary(), equalTo("<summary>sum of sum</summary>"));
    }

    @Test
    public void getLinkByIdForObjectWithoutSummary() throws IOException {
        // request the first link, which points to the first test object with default acl:
        var              linkResponse = client.getLinkById(1L, false);
        Link             link         = new Link(linkResponse);
        ObjectSystemData osd          = client.getOsdById(link.getObjectId(), false);
        assertThat(osd.getSummary(), equalTo("<summary/>"));
    }

    @Test
    public void getLinkByIdForFolder() throws IOException {
        // request link #2, which points to the "home" folder with default acl:
        LinkResponse linkResponse = client.getLinkById(2L, true);

        assertThat(linkResponse.getObjectId(), nullValue());
        assertThat(linkResponse.getType(), equalTo(LinkType.FOLDER));
        Folder folder = client.getFolderById(linkResponse.getFolderId(), true);
        assertNotNull(folder);
        assertThat(folder.getName(), equalTo("home"));
        assertThat(folder.getSummary(), equalTo("<summary>stuff</summary>"));
    }

    @Test
    public void getLinkByIdForFolderWithoutSummary() throws IOException {
        // request link #2, which points to the "home" folder with default acl:
        GetLinksRequest     linkRequest = new GetLinksRequest(2L, false);
        HttpResponse        response    = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, linkRequest);
        LinkResponseWrapper linkWrapper = parseResponse(response);
        Link                link        = linkWrapper.getLinks().get(0);
        assertThat(link.getType(), equalTo(LinkType.FOLDER));
        Folder folder = client.getFolderById(link.getFolderId(), false);
        assertThat(folder.getSummary(), nullValue());
    }

    @Test
    public void getLinkWhereOnlyOwnerHasBrowsePermission() throws IOException {
        // request link #14, which points to the "home" folder with default acl:
        var linkResponse = client.getLinkById(14L,false);
        var link = new Link(linkResponse);
        assertThat(link.getType(), equalTo(LinkType.OBJECT));
        ObjectSystemData osd = client.getOsdById(link.getObjectId(), true);
        assertThat(osd.getSummary(), equalTo("<summary/>"));
    }

    @Test
    public void aclOnLinkForbidsAccess() throws IOException {
        // request link #2, which points to the "home" folder with default acl:
        GetLinksRequest linkRequest = new GetLinksRequest(3L, false);
        HttpResponse    response    = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, linkRequest);
        assertCinnamonError(response, NO_BROWSE_PERMISSION);
    }

    @Test
    public void aclOnObjectForbidsAccess() throws IOException {
        // request link #4, which points to object #7 with acl "unseen" #7:
        GetLinksRequest linkRequest = new GetLinksRequest(4L, false);
        HttpResponse    response    = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, linkRequest);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void aclOnFolderForbidsAccess() throws IOException {
        // request link #5, which points to folder #3 with acl "unseen" #7:
        GetLinksRequest linkRequest = new GetLinksRequest(5L, false);
        HttpResponse    response    = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, linkRequest);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void deleteLinkWithInvalidId() {
        assertClientError(() -> client.deleteLinks(List.of(-1L)), INVALID_REQUEST);
    }

    @Test
    public void deleteLinkWithNonExistentLink() throws IOException {
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
    public void deleteLinkToFolderWithoutPermission() throws IOException {
        assertClientError(() -> client.deleteLinks(List.of(13L)), ErrorCode.UNAUTHORIZED);
    }

    @Test
    public void deleteObjectLinkHappyPath() throws IOException {
        assertTrue(client.deleteLinks(List.of(9L)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(9L), true), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteFolderLinkHappyPath() throws IOException {
        assertTrue(client.deleteLinks(List.of(10L)));

        // verify delete:
        assertClientError(() -> client.getLinksById(List.of(10L), true), ErrorCode.OBJECT_NOT_FOUND);
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
        assertClientError(() -> client.createLink(6L, LinkType.OBJECT, 1L, 1L, null, 0L), INVALID_REQUEST);

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
    public void createLinkToFolderWithoutBrowsePermission() throws IOException {
        CreateLinkRequest crlId    = new CreateLinkRequest(6, LinkType.FOLDER, 1, 1, 7L, null);
        HttpResponse      response = sendStandardRequest(UrlMapping.LINK__CREATE, crlId);
        assertCinnamonError(response, ErrorCode.UNAUTHORIZED);
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
        var link = client.getLinksById(List.of(18L), false).get(0);
        link.setAclId(1L);
        assertClientError(() -> client.updateLink(link), MISSING_SET_ACL_PERMISSION);
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
        var linkResponse = client.getLinkById(21L, false);
        linkResponse.setOwnerId(2L);
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
        var linkResponse = client.getLinkById(21L, false);
        linkResponse.setParentId(6L);
        var link = new Link(linkResponse);
        assertEquals(link, client.updateLink(link));
    }

    @Test
    public void updateLinkToObject() throws IOException {
        var linkResponse = client.getLinkById(21L, false);
        linkResponse.setObjectId(15L);
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
