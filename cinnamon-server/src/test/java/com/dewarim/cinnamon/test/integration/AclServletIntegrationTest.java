package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.CinnamonClientException;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.Unwrapper;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.acl.UpdateAclRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.ACL_DEFAULT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AclServletIntegrationTest extends CinnamonIntegrationTest {

    private Logger                     log          = LogManager.getLogger(AclServletIntegrationTest.class);
    private Unwrapper<Acl, AclWrapper> aclUnWrapper = new Unwrapper<>(AclWrapper.class);

    @Test
    public void listAclsTest() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__LIST, new ListAclRequest());
        List<Acl>    acls            = unwrapAcls(aclListResponse, null);
        assertFalse(acls.isEmpty());
        Optional<Acl> defaultAcl = acls.stream().filter(acl -> acl.getName().equals(ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
        Optional<Acl> reviewers = acls.stream().filter(acl -> acl.getName().equals("reviewers.acl")).findFirst();
        assertTrue(reviewers.isPresent());
    }

    @Test
    public void createAclTest() throws IOException {
        String        aclName         = "test_acl_" + Math.random();
        HttpResponse  aclListResponse = sendAdminRequest(UrlMapping.ACL__CREATE, new CreateAclRequest(Collections.singletonList(aclName)));
        List<Acl>     acls            = unwrapAcls(aclListResponse, 1);
        Optional<Acl> testAcl         = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(testAcl.isPresent());
    }

    @Test
    public void createAclShouldFailOnInvalidName()  {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createAcl(List.of("")));
        assertEquals(ex.getErrorCode(),ErrorCode.INVALID_REQUEST );
    }

    @Test
    public void updateAclTest() throws IOException {
        String           aclName         = "rename.me.acl.new";
        UpdateAclRequest updateRequest   = new UpdateAclRequest(4L, aclName);
        HttpResponse     aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        List<Acl>        acls            = aclUnWrapper.unwrap(aclListResponse, 1);
        Optional<Acl>    renamedAcl      = acls.stream().filter(acl -> acl.getName().equals(aclName)).findFirst();
        assertTrue(renamedAcl.isPresent());
    }

    @Test
    public void renameToNullShouldFail() throws IOException {
        UpdateAclRequest updateRequest   = new UpdateAclRequest(4L, null);
        HttpResponse     aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void renameNonExistentAclShouldFail() throws IOException {
        UpdateAclRequest updateRequest   = new UpdateAclRequest(Long.MAX_VALUE, "fooXXX");
        HttpResponse     aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void renameToExistingOtherNameShouldFail() throws IOException {
        UpdateAclRequest updateRequest   = new UpdateAclRequest(4L, Constants.ACL_DEFAULT);
        HttpResponse     aclListResponse = sendAdminRequest(UrlMapping.ACL__UPDATE, updateRequest);
        assertCinnamonError(aclListResponse, ErrorCode.DB_UPDATE_FAILED);
    }

    @Test
    public void validRequestByAclName() throws IOException {
        AclInfoRequest aclInfoRequest  = new AclInfoRequest(null, Constants.ACL_DEFAULT);
        HttpResponse   aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, aclInfoRequest);
        List<Acl>      acls            = unwrapAcls(aclListResponse, 1);
        Optional<Acl>  defaultAcl      = acls.stream().filter(acl -> acl.getName().equals(Constants.ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
    }

    @Test
    public void validRequestByAclId() throws IOException {
        HttpResponse  aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(1L, null));
        List<Acl>     acls            = unwrapAcls(aclListResponse, 1);
        Optional<Acl> defaultAcl      = acls.stream().filter(acl -> acl.getName().equals(Constants.ACL_DEFAULT)).findFirst();
        assertTrue(defaultAcl.isPresent());
    }

    @Test
    public void invalidRequestForAcl() throws IOException {
        AclInfoRequest aclInfoRequest  = new AclInfoRequest(null, null);
        HttpResponse   aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, aclInfoRequest);
        assertCinnamonError(aclListResponse, ErrorCode.INFO_REQUEST_WITHOUT_NAME_OR_ID);
    }

    @Test
    public void requestForNonExistentAclShouldFail() throws IOException {
        HttpResponse aclListResponse = sendAdminRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(0L, null));
        assertCinnamonError(aclListResponse, ErrorCode.ACL_NOT_FOUND);
    }

    @Test
    public void deleteAclShouldFailOnAclInUse() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(1L);
        HttpResponse     response         = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertCinnamonError(response, ErrorCode.DB_DELETE_FAILED);

        // TODO: verify delete fails when acl is currently used on object
        // TODO: verify delete fails when acl is currently used on link
        // TODO: verify delete fails when acl is currently used on aclGroup
    }

    @Test
    public void deleteAclShouldFailOnUnknownAcl() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(Long.MAX_VALUE);
        HttpResponse     response         = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteAclShouldIgnoreUnknownAclOnRequest() throws IOException {
        // aclId 1 is default acl in test db, linked to root folder
        DeleteAclRequest deleteAclRequest = new DeleteAclRequest(Long.MAX_VALUE);
        deleteAclRequest.setIgnoreNotFound(true);
        HttpResponse response = sendAdminRequest(UrlMapping.ACL__DELETE, deleteAclRequest);
        assertResponseOkay(response);
    }

    @Test
    public void deleteAcl() throws IOException {
        // aclId 3 is "delete.me.acl" in test db
        assertTrue(adminClient.deleteAcl(Collections.singletonList(3L)));
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
        List<Acl> acls = mapper.readValue(response.getEntity().getContent(), AclWrapper.class).getAcls();
        if (expectedSize != null) {
            assertNotNull(acls);
            assertFalse(acls.isEmpty());
            assertThat(acls.size(), equalTo(expectedSize));
        }
        return acls;
    }

}
