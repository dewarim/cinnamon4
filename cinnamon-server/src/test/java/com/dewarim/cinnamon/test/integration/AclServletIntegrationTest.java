package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.DeletionResponse;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.ACL_DEFAULT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class AclServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listAclsTest() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__GET_ACLS);
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
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__CREATE_ACL, new CreateAclRequest(aclName));
        List<Acl> acls = unwrapAcls(aclListResponse, 1);
        Optional<Acl> testAcl = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(testAcl.isPresent());
    }

    @Test
    public void createAclShouldFailOnInvalidName() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__CREATE_ACL, new CreateAclRequest(null));
        assertCinnamonError(aclListResponse, ErrorCode.NAME_PARAM_IS_INVALID);
    }

    @Test
    public void updateAclTest() throws IOException {
        String aclName = "rename.me.acl.new";
        AclUpdateRequest updateRequest = new AclUpdateRequest(2L, aclName);
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE_ACL, updateRequest);
        List<Acl> acls = unwrapAcls(aclListResponse, 1);
        Optional<Acl> renamedAcl = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(renamedAcl.isPresent());
    }

    @Test
    public void renameToNullShouldFail() throws IOException {
        AclUpdateRequest updateRequest = new AclUpdateRequest(4L, null);
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE_ACL, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.NAME_PARAM_IS_INVALID);
    }

    @Test
    public void renameToExistingOtherNameShouldFail() throws IOException {
        AclUpdateRequest updateRequest = new AclUpdateRequest(4L, Constants.ACL_DEFAULT);
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE_ACL, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.DB_UPDATE_FAILED);
    }

    @Test
    public void validRequestByAclName() throws IOException {
        AclInfoRequest aclInfoRequest = new AclInfoRequest(null, Constants.ACL_DEFAULT);
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, aclInfoRequest);
        List<Acl> acls = unwrapAcls(aclListResponse, 1);
        Optional<Acl> defaultAcl = acls.stream().filter(acl -> acl.getName().equals(Constants.ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
    }

    @Test
    public void validRequestByAclId() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(1L, null));
        List<Acl> acls = unwrapAcls(aclListResponse, 1);
        Optional<Acl> defaultAcl = acls.stream().filter(acl -> acl.getName().equals(Constants.ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
    }

    @Test
    public void invalidRequestForAcl() throws IOException {
        AclInfoRequest aclInfoRequest = new AclInfoRequest(null, null);
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, aclInfoRequest);
        assertCinnamonError(aclListResponse, ErrorCode.INFO_REQUEST_WITHOUT_NAME_OR_ID);
    }

    @Test
    public void requestForNonExistentAclShouldFail() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(0L, null));
        assertNull(unwrapAcls(aclListResponse, null));
    }

    @Test
    public void deleteAclShouldFailOnAclInUse() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        HttpResponse response = sendAdminRequest(UrlMapping.ACL__DELETE_ACL, new DeleteByIdRequest(1L));
        assertResponseOkay(response);
        DeletionResponse deletionResponse = mapper.readValue(response.getEntity().getContent(), DeletionResponse.class);
        assertThat(deletionResponse.isSuccess(), equalTo(false));

        // TODO: verify delete fails when acl is currently used on object
        // TODO: verify delete fails when acl is currently used on link
        // TODO: verify delete fails when acl is currently used on aclentry
    }

    @Test
    public void deleteAcl() throws IOException {
        // aclId 3 is "delete.me.acl" in test db
        HttpResponse response = sendAdminRequest(UrlMapping.ACL__DELETE_ACL, new DeleteByIdRequest(3L));
        assertResponseOkay(response);
        DeletionResponse deletionResponse = mapper.readValue(response.getEntity().getContent(), DeletionResponse.class);
        assertThat(deletionResponse.isSuccess(), equalTo(true));
    }

    @Test
    public void getUserAcls() throws IOException {
        // admin should be connected to reviewer.acl and default acl.
        HttpResponse response = sendAdminRequest(UrlMapping.ACL__GET_USER_ACLS, new IdRequest(1L));
        unwrapAcls(response, 2);
    }

    @Test
    public void getUserAclsShouldFailWithoutValidId() throws IOException {
        HttpResponse response = sendAdminRequest(UrlMapping.ACL__GET_USER_ACLS, new IdRequest(-1L));
        assertCinnamonError(response, ErrorCode.ID_PARAM_IS_INVALID);
    }

    private List<Acl> unwrapAcls(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
//      response.getEntity().writeTo(System.out)
        List<Acl> acls = mapper.readValue(response.getEntity().getContent(), AclWrapper.class).getAcls();
        if (expectedSize != null) {
            assertNotNull(acls);
            assertFalse(acls.isEmpty());
            assertThat(acls.size(), equalTo(expectedSize));
        }
        return acls;
    }

}
