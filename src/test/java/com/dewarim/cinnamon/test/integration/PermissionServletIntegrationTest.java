package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.BROWSE;
import static com.dewarim.cinnamon.DefaultPermission.RELATION_CHILD_REMOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PermissionServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listPermissions() throws IOException {
        List<Permission> permissions = client.listPermissions();
        assertEquals(DefaultPermission.values().length, permissions.size());
    }

    @Test
    public void getUsersPermissions() throws IOException {
        List<Permission>     permissions = client.getUserPermissions(2L, defaultCreationAcl.getId() );
        Optional<Permission> browse      = permissions.stream().filter(s -> s.getName().equals(BROWSE.getName())).findFirst();
        assertTrue(browse.isPresent());
        List<Permission> reviewerPermissions = client.getUserPermissions(2L, defaultCreationAcl.getId() );
        assertEquals(DefaultPermission.values().length, reviewerPermissions.size());
    }

    @Test
    public void userShouldInheritPermissionsFromParentGroup() throws IOException {
        var permissions = List.of(DefaultPermission.BROWSE, RELATION_CHILD_REMOVE);
        var toh         = new TestObjectHolder(adminClient, adminId);
        var parentGroup = toh.createGroup()
                .createAcl()
                .createAclGroup()
                .addPermissions(permissions)
                .group;
        // no permissions from unconnected group:
        assertEquals(0, client.getUserPermissions(userId, toh.acl.getId()).size());

        toh.createGroup(parentGroup.getId()).addUserToGroup(userId);
        List<Permission> userPermissions = client.getUserPermissions(userId, toh.acl.getId());
        // check inherited permissions:
        assertEquals(
                permissions.stream().map(DefaultPermission::getName).collect(Collectors.toSet()),
                userPermissions.stream().map(Permission::getName).collect(Collectors.toSet())
        );
    }

    @Test
    public void getUsersPermissionsForMissingAcl() throws IOException {
        Long aclId = prepareAclGroupWithPermissions(List.of()).acl.getId();
        List<Permission> permissions = client.getUserPermissions(userId, aclId);
        assertTrue(permissions.isEmpty());
    }

    @Test
    public void addAndRemovePermissions() throws IOException {
        Acl              acl         = adminClient.createAcls(List.of("add-and-remove-permission-acl")).getFirst();
        Group            group       = adminClient.createGroupsByName(List.of("add-and-remove-permission-group")).getFirst();
        AclGroup         aclGroup    = adminClient.createAclGroups(List.of(new AclGroup(acl.getId(), group.getId()))).getFirst();
        Long             aclGroupId  = aclGroup.getId();
        List<Permission> permissions = client.listPermissions();
        assertTrue(aclGroup.getPermissionIds().isEmpty(), "new AclGroup should have no permissions");

        // add permission
        adminClient.addAndRemovePermissions(aclGroup.getId(), List.of(permissions.getFirst().getId()), List.of());
        List<AclGroup>     aclGroups          = client.listAclGroups();
        Optional<AclGroup> updatedAclGroupOpt = aclGroups.stream().filter(aGroup -> aGroup.getId().equals(aclGroupId)).findFirst();
        assertTrue(updatedAclGroupOpt.isPresent());
        AclGroup updatedGroup = updatedAclGroupOpt.get();
        assertEquals(1, updatedGroup.getPermissionIds().size());
        assertEquals(1, updatedGroup.getPermissionIds().getFirst());

        // remove permission:
        adminClient.addAndRemovePermissions(aclGroup.getId(), List.of(), List.of(permissions.getFirst().getId()));
        updatedAclGroupOpt = client.listAclGroups().stream().filter(aGroup -> aGroup.getId().equals(aclGroupId)).findFirst();
        assertTrue(updatedAclGroupOpt.isPresent());
        assertEquals(0, updatedAclGroupOpt.get().getPermissionIds().size());
    }

    @Test
    public void addAndRemovePermissionsNonAdmin() {
        assertClientError(() -> client.addAndRemovePermissions(1L, List.of(1L), List.of(2L)), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }
}
