package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.*;
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

        List<String> actualGroupNames = groups.stream().map(Group::getName).toList();
        String[] groupNames = {"_superusers", "_everyone", "_owner"};
        Arrays.stream(groupNames).forEach(name ->
                assertTrue(actualGroupNames.contains(name))
        );
    }

    @Test
    public void createGroupWithInvalidRequest() {
        assertClientError( () -> adminClient.createGroupsByName(List.of("")),INVALID_REQUEST);
    }

    @Test
    public void createGroupWithSameName() throws IOException {
        String name = "a-group-with-a-name";
        adminClient.createGroupsByName(List.of(name));
        assertClientError( () -> adminClient.createGroupsByName(List.of(name)),DB_INSERT_FAILED);
    }

    @Test
    @Order(100)
    public void createGroups() throws IOException {
        groups = adminClient.createGroupsByName(List.of("test1", "test2", "test3"));
        var allGroupNames = client.listGroups().stream().map(Group::getName).toList();
        var newGroupNames = groups.stream().map(Group::getName).toList();
        assertTrue(allGroupNames.containsAll(newGroupNames));
    }

    @Test
    public void createGroupsWithoutPermission() {
        assertClientError( () -> client.createGroupsByName(List.of("no-perm-group")),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteGroupsWithoutPermission() {
        assertClientError( () -> client.deleteGroups(List.of(1L)),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    @Order(400)
    public void deleteGroups() throws IOException {
        var deleteResult = adminClient.deleteGroups(groups.stream().map(Group::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        var allGroupNames = client.listGroups().stream().map(Group::getName).toList();
        var deletedGroupNames = groups.stream().map(Group::getName).toList();
        assertFalse(allGroupNames.containsAll(deletedGroupNames));
    }

    @Test
    public void upgradeGroup() throws IOException {
        List<Group> groups = adminClient.createGroupsByName(List.of("update-my-name"));
        groups.getFirst().setName("is-updated-group");
        List<Group> updatedGroups = adminClient.updateGroups(groups);
        assertEquals(groups.getFirst().getName(), updatedGroups.getFirst().getName());
    }

    @Test
    public void updateGroupWithoutPermission() {
        assertClientError( () -> client.updateGroups(Collections.emptyList()),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void updateNonExistentGroup() {
        var group = new Group("foo");
        group.setId(Long.MAX_VALUE);
        assertClientError( () ->
                adminClient.updateGroups(Collections.singletonList(group)),OBJECT_NOT_FOUND);
    }

    @Test
    public void updateGroupWithDuplicateName() throws IOException {
        var existing = adminClient.createGroup(new Group("existing-group"));
        var updateGroup = adminClient.createGroup(new Group("update-my-name"));
        updateGroup.setName(existing.getName());
        assertClientError( () ->
                adminClient.updateGroups(Collections.singletonList(updateGroup)),DB_UPDATE_FAILED);
    }

    @Test
    public void createGroupWithParent() throws IOException {
        var parent = adminClient.createGroup(new Group("parent-group", null));
        var child = adminClient.createGroup(new Group("a-child", parent.getId()));
        long parentCount = client.listGroups().stream().filter(group -> group.equals(new Group(child.getId(), child.getName(), child.getParentId()))).count();
        assertEquals(1L, parentCount);
    }

    @Test
    public void updateGroupWithParent() throws IOException {
        var parent = adminClient.createGroup(new Group("another-parent-group", null));
        var stepParent = adminClient.createGroup(new Group("step-parent-group", null));
        var child = adminClient.createGroup(new Group("another-child", parent.getId()));
        child.setParentId(stepParent.getId());
        Group adoptedChild = adminClient.updateGroups(List.of(child)).getFirst();
        assertEquals(stepParent.getId(), adoptedChild.getParentId());
    }

    @Test
    public void updateGroupByAddingParent() throws IOException {
        var parent = adminClient.createGroup(new Group("parent-group2", null));
        var child = adminClient.createGroup(new Group("a-child2", null));
        child.setParentId(parent.getId());
        Group adoptedChild = adminClient.updateGroups(List.of(child)).getFirst();
        long count = client.listGroups().stream().filter(group -> group.equals(new Group(child.getId(), child.getName(), adoptedChild.getParentId()))).count();
        assertEquals(1L, count);
    }

    @Test
    public void deleteChildWithParentGroup() throws IOException {
        var parent = adminClient.createGroup(new Group("parent-group3", null));
        var child = adminClient.createGroup(new Group("a-child3", parent.getId()));
        adminClient.deleteGroups(List.of(child.getId()));
        assertTrue(client.listGroups().stream().noneMatch(group -> group.equals(new Group(child.getId(), child.getName(), null))));
    }

    @Test
    public void deleteGroupWithChildrenShouldFail() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createGroup();
        Long parentId = toh.group.getId();
        toh.createGroup(parentId).createAcl().createAclGroup().addUserToGroup(adminId);
        assertClientError( () ->
                adminClient.deleteGroups(List.of(parentId)),GROUP_HAS_CHILDREN);
    }

    @Test
    public void deleteGroupWithChildrenAndFlag() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createGroup();
        Long parentId = toh.group.getId();
        toh.createGroup(parentId).createAcl().createAclGroup().addUserToGroup(adminId);
        adminClient.deleteGroups(List.of((parentId)),true);
    }

    @Test
    public void addUserToGroupAndRemove() throws IOException {
        var userGroup = adminClient.createGroup(new Group("user-group", null));
        var user = adminClient.createUser(new UserAccount("user-for-a-group", "passwehde", "-", "-", 1L, LoginType.CINNAMON.name(), true, true, true));
        adminClient.addUserToGroups(user.getId(), List.of(userGroup.getId()));
        UserAccount userWithGroup = client.getUser(user.getId());
        assertEquals(userGroup.getId(), userWithGroup.getGroupIds().getFirst());

        adminClient.removeUserFromGroups(user.getId(), List.of(userGroup.getId()));
        var userWithoutGroups = client.getUser(user.getId());
        assertEquals(0, Objects.requireNonNullElse(userWithoutGroups.getGroupIds(), new ArrayList<>()).size());
    }

    @Disabled("Currently, adding a user to a non-existent group will fail silently (same as removal from missing group).")
    @Test
    public void addUserToNonExistentGroup() throws IOException {
        var user = adminClient.createUser(new UserAccount("user-for-a-missing-group",
                "passwehde", "-", "-", 1L, LoginType.CINNAMON.name(), true, true, true));
        assertClientError( () ->
                adminClient.addUserToGroups(user.getId(), List.of(Long.MAX_VALUE)),DB_INSERT_FAILED);
    }

}
