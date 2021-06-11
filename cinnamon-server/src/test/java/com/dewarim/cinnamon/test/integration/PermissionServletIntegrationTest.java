package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Permission;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PermissionServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listPermissions() throws IOException {
        List<Permission> permissions = client.listPermissions();
        assertEquals(17, permissions.size());
    }

    @Test
    public void getUsersPermissions() throws IOException {
        // user doe @ default acl: should have browse and browse_folder as well as 
        // delete_object and delete_folder permission
        List<Permission>     permissions = client.getUserPermissions(2L, 1L);
        Optional<Permission> browse      = permissions.stream().filter(s -> s.getName().equals("_browse")).findFirst();
        assertTrue(browse.isPresent());
        Optional<Permission> browseFolder = permissions.stream().filter(s -> s.getName().equals("_browse_folder")).findFirst();
        assertTrue(browseFolder.isPresent());

        /* user doe @ reviewers acl: should have 
        - browse
        - create folder,
        - write_object_sysmeta
        - browse permission
        - read_object_sysmeta
        - read_object_content
        - write_object_content
        - lock
        - read_object_custom_meta
        - edit_folder
        - set_acl
        - move
        - version
        - delete_object
         */
        List<Permission> reviewerPermissions = client.getUserPermissions(2L, 2L);
        assertEquals(13, reviewerPermissions.size());
    }

    @Test
    public void getUsersPermissionsForMissingAcl() throws IOException {
        // user doe @ rename.me.acl: should have no permissions
        List<Permission> permissions = client.getUserPermissions(2L, 4L);
        assertTrue(permissions.isEmpty());
    }

    @Test
    public void addAndRemovePermissions() throws IOException {
        Acl              acl         = adminClient.createAcl(List.of("add-and-remove-permission-acl")).get(0);
        Group            group       = adminClient.createGroups(List.of("add-and-remove-permission-group")).get(0);
        AclGroup         aclGroup    = adminClient.createAclGroups(List.of(new AclGroup(acl.getId(), group.getId()))).get(0);
        Long             aclGroupId  = aclGroup.getId();
        List<Permission> permissions = client.listPermissions();
        assertTrue(aclGroup.getPermissionIds().isEmpty(), "new AclGroup should have no permissions");

        // add permission
        adminClient.addAndRemovePermissions(aclGroup.getId(), List.of(permissions.get(0).getId()), List.of());
        List<AclGroup>     aclGroups          = client.listAclGroups();
        Optional<AclGroup> updatedAclGroupOpt = aclGroups.stream().filter(aGroup -> aGroup.getId().equals(aclGroupId)).findFirst();
        assertTrue(updatedAclGroupOpt.isPresent());
        AclGroup updatedGroup = updatedAclGroupOpt.get();
        assertEquals(1, updatedGroup.getPermissionIds().size());
        assertEquals(1, updatedGroup.getPermissionIds().get(0));

        // remove permission:
        adminClient.addAndRemovePermissions(aclGroup.getId(), List.of(), List.of(permissions.get(0).getId()));
        updatedAclGroupOpt = client.listAclGroups().stream().filter(aGroup -> aGroup.getId().equals(aclGroupId)).findFirst();
        assertTrue(updatedAclGroupOpt.isPresent());
        assertEquals(0, updatedAclGroupOpt.get().getPermissionIds().size());
    }

    @Test
    public void addAndRemovePermissionsNonAdmin() {
        assertClientError(() -> client.addAndRemovePermissions(1L, List.of(1L), List.of(2L)), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }
}
