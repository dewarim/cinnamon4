package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupServletIntegrationTest extends CinnamonIntegrationTest {

    private static List<Group> groups;

    @Test
    public void listGroups() throws IOException {
        List<Group> groups = client.listGroups();

        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertTrue(groups.size() >= 7);

        List<String> actualGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        String[]     groupNames       = {"_superusers", "_everyone", "_owner"};
        Arrays.stream(groupNames).forEach(name ->
                assertTrue(actualGroupNames.contains(name))
        );
    }

    @Test
    public void createGroupWithInvalidRequest() {
        CinnamonClientException e = assertThrows(CinnamonClientException.class, () -> adminClient.createGroupsByName(List.of("")));
        assertEquals(ErrorCode.INVALID_REQUEST, e.getErrorCode());
    }

    @Test
    public void createGroupWithSameName() throws IOException {
        String name = "a-group-with-a-name";
        adminClient.createGroupsByName(List.of(name));
        CinnamonClientException e = assertThrows(CinnamonClientException.class, () -> adminClient.createGroupsByName(List.of(name)));
        assertEquals(ErrorCode.DB_INSERT_FAILED, e.getErrorCode());
    }

    @Test
    @Order(100)
    public void createGroups() throws IOException {
        groups = adminClient.createGroupsByName(List.of("test1", "test2", "test3"));
        var allGroupNames = client.listGroups().stream().map(Group::getName).collect(Collectors.toList());
        var newGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        assertTrue(allGroupNames.containsAll(newGroupNames));
    }

    @Test
    public void createGroupsWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createGroupsByName(List.of("no-perm-group")));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteGroupsWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.deleteGroups(List.of(1L)));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    @Order(400)
    public void deleteGroups() throws IOException {
        var deleteResult = adminClient.deleteGroups(groups.stream().map(Group::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        var allGroupNames     = client.listGroups().stream().map(Group::getName).toList();
        var deletedGroupNames = groups.stream().map(Group::getName).toList();
        assertFalse(allGroupNames.containsAll(deletedGroupNames));
    }

    @Test
    public void upgradeGroup() throws IOException {
        List<Group> groups = adminClient.createGroupsByName(List.of("update-my-name"));
        groups.get(0).setName("is-updated-group");
        List<Group> updatedGroups = adminClient.updateGroups(groups);
        assertEquals(groups.get(0).getName(), updatedGroups.get(0).getName());
    }

    @Test
    public void updateGroupWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.updateGroups(Collections.emptyList()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateNonExistentGroup() {
        var group = new Group("foo");
        group.setId(Long.MAX_VALUE);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () ->
                adminClient.updateGroups(Collections.singletonList(group)));
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void updateGroupWithDuplicateName() throws IOException {
        var existing    = adminClient.createGroup(new Group("existing-group"));
        var updateGroup = adminClient.createGroup(new Group("update-my-name"));
        updateGroup.setName(existing.getName());
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () ->
                adminClient.updateGroups(Collections.singletonList(updateGroup)));
        assertEquals(ErrorCode.DB_UPDATE_FAILED, ex.getErrorCode());
    }

    @Test
    public void createGroupWithParent() throws IOException {
        var  parent      = adminClient.createGroup(new Group("parent-group", null));
        var  child       = adminClient.createGroup(new Group("a-child", parent.getId()));
        long parentCount = client.listGroups().stream().filter(group -> group.equals(new Group(child.getId(), child.getName(), child.getParentId()))).count();
        assertEquals(1L, parentCount);
    }

    @Test
    public void updateGroupWithParent() throws IOException {
        var parent     = adminClient.createGroup(new Group("another-parent-group", null));
        var stepParent = adminClient.createGroup(new Group("step-parent-group", null));
        var child      = adminClient.createGroup(new Group("another-child", parent.getId()));
        child.setParentId(stepParent.getId());
        Group adoptedChild = adminClient.updateGroups(List.of(child)).get(0);
        assertEquals(stepParent.getId(), adoptedChild.getParentId());
    }

    @Test
    public void updateGroupByAddingParent() throws IOException {
        var parent = adminClient.createGroup(new Group("parent-group2", null));
        var child  = adminClient.createGroup(new Group("a-child2", null));
        child.setParentId(parent.getId());
        Group adoptedChild = adminClient.updateGroups(List.of(child)).get(0);
        long  count        = client.listGroups().stream().filter(group -> group.equals(new Group(child.getId(), child.getName(), adoptedChild.getParentId()))).count();
        assertEquals(1L, count);
    }

    @Test
    public void deleteChildWithParentGroup() throws IOException {
        var parent = adminClient.createGroup(new Group("parent-group3", null));
        var child  = adminClient.createGroup(new Group("a-child3", parent.getId()));
        adminClient.deleteGroups(List.of(child.getId()));
        assertTrue(client.listGroups().stream().noneMatch(group -> group.equals(new Group(child.getId(), child.getName(), null))));
    }

    // currently, deleteGroupRequest has no "recurse" option.
    @Test
    public void deleteGroupWithChildrenShouldFail() throws IOException {
        var parent = adminClient.createGroup(new Group("moribund-parent-group", null));
        var child  = adminClient.createGroup(new Group("to-be-orphaned", parent.getId()));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () ->
                adminClient.deleteGroups(List.of(parent.getId())));
        assertEquals(ErrorCode.DB_DELETE_FAILED, ex.getErrorCode());
    }

    @Test
    public void addUserToGroupAndRemove() throws IOException {
        var userGroup = adminClient.createGroup(new Group("user-group", null));
        var user      = adminClient.createUser(new UserAccount("user-for-a-group", "passwehde", "-", "-", 1L, LoginType.CINNAMON.name(), true, true, true));
        adminClient.addUserToGroups(user.getId(), List.of(userGroup.getId()));
        UserAccount userWithGroup = client.getUser(user.getId());
        assertEquals(userGroup.getId(), userWithGroup.getGroupIds().get(0));

        adminClient.removeUserFromGroups(user.getId(), List.of(userGroup.getId()));
        var userWithoutGroups = client.getUser(user.getId());
        assertEquals(0, Objects.requireNonNullElse(userWithoutGroups.getGroupIds(), new ArrayList<>()).size());
    }

    @Disabled("Currently, adding a user to a non-existent group will fail silently (same as removal from missing group).")
    @Test
    public void addUserToNonExistentGroup() throws IOException {
        var user = adminClient.createUser(new UserAccount("user-for-a-missing-group",
                "passwehde", "-", "-", 1L, LoginType.CINNAMON.name(), true, true, true));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () ->
                adminClient.addUserToGroups(user.getId(), List.of(Long.MAX_VALUE)));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

}
