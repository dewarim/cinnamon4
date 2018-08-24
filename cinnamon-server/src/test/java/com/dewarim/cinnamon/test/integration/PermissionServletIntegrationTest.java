package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import org.apache.http.HttpResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;


public class PermissionServletIntegrationTest extends CinnamonIntegrationTest {
    
    
    @Test
    public void listPermissions() throws IOException {
        HttpResponse response = sendAdminRequest(UrlMapping.PERMISSION__LIST_PERMISSIONS);
        assertResponseOkay(response);
        PermissionWrapper wrapper = mapper.readValue(response.getEntity().getContent(),PermissionWrapper.class);
        assertThat(wrapper.getPermissions().size(), equalTo(17));
    }
    
    @Test
    public void getUsersPermissions() throws IOException{
        // user doe @ default acl: should have browse and browse_folder as well as 
        // delete_object and delete_folder permission
        UserPermissionRequest permissionRequest = new UserPermissionRequest(2L,1L);
        HttpResponse response = sendAdminRequest(UrlMapping.PERMISSION__GET_USER_PERMISSIONS, permissionRequest);
        List<Permission> permissions = unwrapPermissions(response, 4);
        Optional<Permission> browse = permissions.stream().filter(s -> s.getName().equals("_browse")).findFirst();
        assertTrue(browse.isPresent());  
        Optional<Permission> browseFolder = permissions.stream().filter(s -> s.getName().equals("_browse_folder")).findFirst();
        assertTrue(browseFolder.isPresent());

        /* user doe @ reviewers acl: should have 
        - create folder,
        - write_object_sysmeta
        - browse permission
        - read_object_sysmeta
        - read_object_content
        - write_object_content
        - lock
         */
        UserPermissionRequest reviewerPermissionRequest = new UserPermissionRequest(2L,2L);
        HttpResponse reviewerResponse = sendAdminRequest(UrlMapping.PERMISSION__GET_USER_PERMISSIONS, reviewerPermissionRequest);
        unwrapPermissions(reviewerResponse,7);
    }    
    
    @Test
    public void getUsersPermissionsForMissingAcl() throws IOException{
        // user doe @ rename.me.acl: should have no permissions
        UserPermissionRequest permissionRequest = new UserPermissionRequest(2L,4L);
        HttpResponse response = sendAdminRequest(UrlMapping.PERMISSION__GET_USER_PERMISSIONS, permissionRequest);
        List<Permission> permissions = unwrapPermissions(response, null);
        assertNull(permissions);
    }

    private List<Permission> unwrapPermissions(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<Permission> permissions = mapper.readValue(response.getEntity().getContent(),PermissionWrapper.class).getPermissions();
        if (expectedSize != null) {
            assertNotNull(permissions);
            assertFalse(permissions.isEmpty());
            MatcherAssert.assertThat(permissions.size(), equalTo(expectedSize));
        }
        return permissions;
    }
    
}
