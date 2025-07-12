package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.aclGroup.AclGroupListRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclGroupServletIntegrationTest extends CinnamonIntegrationTest {

    static List<Acl>      acls      = new ArrayList<>();
    static List<Group>    groups    = new ArrayList<>();
    static List<AclGroup> aclGroups = new ArrayList<>();

    @Test
    public void testListAclGroupByAclId() throws IOException {
        AclGroupListRequest listRequest  = new AclGroupListRequest(1L, AclGroupListRequest.IdType.ACL);
        var                 httpResponse = sendStandardRequest(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest);
        List<AclGroup>      aclGroups    = unwrapAclGroups(httpResponse, 1);
        aclGroups.forEach(entry -> assertEquals(Long.valueOf(1), entry.getAclId()));
    }

    @Test
    public void testListAclGroupByGroupId() throws IOException {
        AclGroupListRequest listRequest  = new AclGroupListRequest(4L, AclGroupListRequest.IdType.GROUP);
        var                 httpResponse = sendStandardRequest(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest);
        List<AclGroup>      aclGroups    = unwrapAclGroups(httpResponse, 1);
        aclGroups.forEach(entry -> assertEquals(Long.valueOf(4), entry.getGroupId()));
    }

    @Test
    public void invalidAclGroupListRequest() throws IOException {
        AclGroupListRequest listRequest = new AclGroupListRequest();
        sendStandardRequestAndAssertError(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest, INVALID_REQUEST);
    }

    @Test
    public void listAclGroups() throws IOException {
        List<AclGroup> aclGroups = client.listAclGroups();
        assertTrue(aclGroups.size() > 0);
    }

    @Test
    @Order(1)
    public void createAclGroup() throws IOException {
        acls = adminClient.createAcls(List.of("a1", "a2"));
        Acl a1 = acls.get(0);
        Acl a2 = acls.get(1);
        groups = adminClient.createGroupsByName(List.of("g1", "g2"));
        var            g1      = groups.get(0);
        var            g2      = groups.get(1);
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(a1.getId(), g1.getId()));
        entries.add(new AclGroup(a2.getId(), g2.getId()));
        aclGroups = adminClient.createAclGroups(entries);

        assertEquals(entries.size(), aclGroups.size());
        AclGroup ae1 = aclGroups.get(0);
        AclGroup ae2 = aclGroups.get(1);

        assertEquals(ae1.getAclId(), a1.getId());
        assertEquals(ae2.getAclId(), a2.getId());

        assertEquals(ae1.getGroupId(), g1.getId());
        assertEquals(ae2.getGroupId(), g2.getId());
    }

    @Test
    @Order(150)
    public void createAclGroupWithoutPermission() {
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(acls.getFirst().getId(), groups.getFirst().getId()));
        assertClientError(() -> client.createAclGroups(entries), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void createAclGroupWithInvalidRequest() {
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(0L, 0L));
        assertClientError(() -> adminClient.createAclGroups(entries), INVALID_REQUEST);
    }

    @Test
    @Order(200)
    public void updateAclGroup() throws IOException {
        Acl   a1           = acls.getFirst();
        Group g2           = groups.get(1);
        var   idOfFirstAcl = aclGroups.getFirst().getId();
        // replace g1 in aclGroup (a1,g1) with g2
        UpdateAclGroupRequest updateRequest  = new UpdateAclGroupRequest(idOfFirstAcl, a1.getId(), g2.getId());
        var                   updatedEntries = adminClient.updateAclGroups(updateRequest);
        var                   aclGroup       = updatedEntries.getFirst();
        assertEquals(idOfFirstAcl, aclGroup.getId());
        assertEquals(g2.getId(), aclGroup.getGroupId());
        assertEquals(a1.getId(), aclGroup.getAclId());
    }

    @Test
    @Order(225)
    public void updateAclGroupWithoutPermisson() {
        UpdateAclGroupRequest updateRequest = new UpdateAclGroupRequest(1L, 1L, 1L);

        assertClientError(() -> client.updateAclGroups(updateRequest), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    @Order(250)
    public void deleteAclGroupWithoutPermission() {
        assertClientError(() -> client.deleteAclGroups(
                aclGroups.stream().map(AclGroup::getId).collect(Collectors.toList())), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    @Order(300)
    public void deleteAclGroup() throws IOException {
        boolean deleteResult = adminClient.deleteAclGroups(aclGroups.stream().map(AclGroup::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        List<AclGroup> remainingEntries = client.listAclGroups();
        assertTrue(aclGroups.stream().noneMatch(remainingEntries::contains));
    }

    @Test
    @Order(990)
    public void createAclGroupWithPermissionIds() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createAcl()
                .createGroup()
                .createAclGroupWithPermissionIds(List.of(1L, 2L));
        assertEquals(2, toh.aclGroup.getPermissionIds().size());
        List<AclGroup> refreshedAclGroups = adminClient.listAclGroups().stream().filter(aclGroup -> aclGroup.getId().equals(toh.aclGroup.getId())).toList();

        assertEquals(1, refreshedAclGroups.size());
        assertEquals(2, refreshedAclGroups.getFirst().getPermissionIds().size());
    }

    @Test
    @Order(999)
    public void deleteAclGroupWithPermissions() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createAcl()
                .createGroup()
                .createAclGroupWithPermissionIds(List.of(1L, 2L))
                .deleteAclGroup();
    }

    private List<AclGroup> unwrapAclGroups(StandardResponse httpResponse, int expectedSize) throws IOException {
        assertResponseOkay(httpResponse);
        AclGroupWrapper wrapper   = mapper.readValue(httpResponse.getEntity().getContent(), AclGroupWrapper.class);
        List<AclGroup>  aclGroups = wrapper.getAclGroups();
        assertEquals(expectedSize, aclGroups.size());
        return aclGroups;
    }

    @Test
    public void updateAclGroupWithUser() throws IOException {
        TestObjectHolder toh        = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK));
        AclGroup         updateMe   = new AclGroup(toh.aclGroup.getId(), toh.acl.getId(), toh.group.getId());
        Permission       deletePerm = client.listPermissions().stream().filter(p -> p.getName().equals(DELETE.getName())).findFirst().orElseThrow();
        updateMe.setPermissionIds(List.of(deletePerm.getId()));
        List<AclGroup> updateMel = List.of(updateMe);
        adminClient.updateAclGroups(new UpdateAclGroupRequest(updateMel));
        List<Permission> userPermissions = client.getUserPermissions(userId, toh.acl.getId());
        assertEquals(1, userPermissions.size());
        assertEquals(deletePerm, userPermissions.getFirst());
    }

    @Test
    public void deleteAclGroupWithUser() throws IOException {
        TestObjectHolder toh      = prepareAclGroupWithPermissions(List.of(BROWSE, LOCK));
        AclGroup         deleteMe = new AclGroup(toh.aclGroup.getId(), toh.acl.getId(), toh.group.getId());
        adminClient.deleteAclGroups(List.of(deleteMe.getId()));
    }
}
