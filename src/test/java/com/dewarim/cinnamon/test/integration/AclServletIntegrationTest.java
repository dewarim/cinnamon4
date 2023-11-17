package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.client.Unwrapper;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.acl.UpdateAclRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.api.Constants.ACL_DEFAULT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AclServletIntegrationTest extends CinnamonIntegrationTest {

    private final Unwrapper<Acl, AclWrapper> aclUnWrapper = new Unwrapper<>(AclWrapper.class);

    @Test
    public void listAclsTest() throws IOException {
        var aclListResponse = sendAdminRequest(UrlMapping.ACL__LIST, new ListAclRequest());
        List<Acl> acls = unwrapAcls(aclListResponse, null);
        assertFalse(acls.isEmpty());
        Optional<Acl> defaultAcl = acls.stream().filter(acl -> acl.getName().equals(ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
        Optional<Acl> reviewers = acls.stream().filter(acl -> acl.getName().equals("reviewers.acl")).findFirst();
        assertTrue(reviewers.isPresent());
    }

    @Test
    public void createAclTest() throws IOException {
        String aclName = "test_acl_" + Math.random();
        List<Acl> acls = adminClient.createAcls(List.of(aclName));
        Optional<Acl> testAcl = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(testAcl.isPresent());
    }

    @Test
    public void createAclShouldFailOnInvalidName() {
        assertClientError( () -> adminClient.createAcls(List.of("")),INVALID_REQUEST);
    }

    @Test
    public void updateAclTest() throws IOException {
        String aclName = "rename.me.acl.new";
        UpdateAclRequest updateRequest = new UpdateAclRequest(4L, aclName);
        var aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        List<Acl> acls = aclUnWrapper.unwrap(aclListResponse, 1);
        Optional<Acl> renamedAcl = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(renamedAcl.isPresent());
    }

    @Test
    public void renameToNullShouldFail() throws IOException {
        UpdateAclRequest updateRequest = new UpdateAclRequest(4L, null);
        StandardResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, INVALID_REQUEST);
    }

    @Test
    public void renameNonExistentAclShouldFail() throws IOException {
        UpdateAclRequest updateRequest = new UpdateAclRequest(Long.MAX_VALUE, "fooXXX");
        StandardResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void renameToExistingOtherNameShouldFail() throws IOException {
        UpdateAclRequest updateRequest = new UpdateAclRequest(4L, Constants.ACL_DEFAULT);
        StandardResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.DB_UPDATE_FAILED);
    }

    @Test
    public void deleteAclShouldFailOnAclInUse() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(1L);
        var response = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertCinnamonError(response, ErrorCode.DB_DELETE_FAILED);

        // TODO: verify delete fails when acl is currently used on object
        // TODO: verify delete fails when acl is currently used on link
        // TODO: verify delete fails when acl is currently used on aclGroup
    }

    @Test
    public void deleteAclShouldFailOnUnknownAcl() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(Long.MAX_VALUE);
        StandardResponse response = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteAclShouldIgnoreUnknownAclOnRequest() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(Long.MAX_VALUE);
        deleteAclRequest.setIgnoreNotFound(true);
        StandardResponse response = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertResponseOkay(response);
    }

    @Test
    public void deleteAcl() throws IOException {
        Long aclId = adminClient.createAcl(new TestObjectHolder(client, userId).createRandomName()).getId();
        assertTrue(adminClient.deleteAcl(Collections.singletonList(aclId)));
    }

    @Test
    public void getUserAcls() throws IOException {
        List<Acl> acls = client.getAclsOfUser(adminId);
        assertTrue(acls.stream().anyMatch(acl -> acl.getName().equals("reviewers.acl")));
        assertTrue(acls.stream().anyMatch(acl -> acl.getName().equals(ACL_DEFAULT)));
    }

    @Test
    public void getUserAclsShouldFailWithoutValidId() throws IOException {
        var response = sendAdminRequest(UrlMapping.ACL__GET_USER_ACLS, new IdRequest(-1L));
        assertCinnamonError(response, INVALID_REQUEST);
    }

    private List<Acl> unwrapAcls(StandardResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<Acl> acls = mapper.readValue(response.getEntity().getContent(), AclWrapper.class).getAcls();
        if (expectedSize != null) {
            assertNotNull(acls);
            assertFalse(acls.isEmpty());
            assertThat(acls.size(), equalTo(expectedSize));
        }
        return acls;
    }

}
