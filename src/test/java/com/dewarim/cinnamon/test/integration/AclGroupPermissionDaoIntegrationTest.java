package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.AclGroupDao;
import com.dewarim.cinnamon.dao.AclGroupPermissionDao;
import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.BROWSE;
import static com.dewarim.cinnamon.DefaultPermission.DELETE;
import static com.dewarim.cinnamon.DefaultPermission.SET_ACL;
import static com.dewarim.cinnamon.DefaultPermission.WRITE_OBJECT_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AclGroupPermissionDaoIntegrationTest extends CinnamonIntegrationTest {

    /**
     * listAclIdsWithPermissionForGroups replaced a loop in AccessFilter.generateObjectAclSet that
     * ran three queries per acl, so the cost of a permission cache miss grew with the number of
     * acls in the installation. This checks the single query still answers exactly what that loop
     * answered, for every acl in the database and every permission - a rewrite in permission code
     * must not silently widen or narrow what a user may see.
     */
    @Test
    public void singleQueryAgreesWithThePerAclCheck() throws IOException {
        // create acls with a few different group/permission shapes, so the comparison below has
        // something to disagree about even when this class runs on its own:
        prepareAclGroupWithPermissions(List.of(BROWSE));
        prepareAclGroupWithPermissions(List.of(BROWSE, WRITE_OBJECT_CONTENT));
        prepareAclGroupWithPermissions(List.of(DELETE, SET_ACL));
        prepareAclGroupWithPermissions(List.of());
        prepareAclGroupWithOwnerPermissions(List.of(BROWSE, DELETE));

        ThreadLocalSqlSession.refreshSession();
        // Newest first and capped: the per-acl loop below is deliberately the slow, pre-rewrite
        // shape, so letting it run over every acl the whole suite has created would cost exactly
        // what the rewrite removed.
        List<Acl> acls = new AclDao().list().stream()
                .sorted(Comparator.comparingLong(Acl::getId).reversed())
                .limit(15)
                .toList();
        assertTrue(acls.size() > 5, "the acls created above should be visible here");

        int grantsFound = 0;
        for (long userIdToCheck : List.of(userId, adminId)) {
            UserAccount user     = new UserAccountDao().getUserAccountById(userIdToCheck).orElseThrow();
            Set<Long>   groupIds = new GroupDao().getGroupIdsWithAncestorsOfUserById(user.getId());
            for (DefaultPermission defaultPermission : DefaultPermission.values()) {
                Permission permission = new PermissionDao().getPermissionByName(defaultPermission.getName());
                Set<Long>  expected   = perAclLoop(acls, groupIds, permission);
                assertEquals(expected, singleQuery(acls, groupIds, permission),
                        "mismatch for user " + userIdToCheck + " and permission " + defaultPermission.getName());
                grantsFound += expected.size();
            }
        }
        // otherwise two empty sets would compare equal and prove nothing:
        assertTrue(grantsFound > 0, "the comparison above never saw a granted permission");
    }

    /**
     * Groups form a hierarchy, and permissions are inherited upwards: a member of a subgroup gets
     * what that subgroup's ancestors were granted. That expansion happens in
     * GroupDao.getGroupIdsWithAncestorsOfUserById, before listAclIdsWithPermissionForGroups ever
     * sees a group id - which is why the comparison above cannot catch a regression in it: it
     * hands the same expanded set to both sides. So this covers the path end to end instead, with
     * the user a member of the child group only and the permission granted to the parent.
     */
    @Test
    public void permissionIsInheritedFromAncestorGroup() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId);
        // an acl which grants BROWSE to the parent group, and nothing to anyone else:
        Long parentGroupId = toh.createGroup()
                .createAcl()
                .createAclGroup()
                .addPermissions(List.of(BROWSE))
                .group.getId();
        Acl acl = toh.acl;
        // A group below it, and a user who is a member of that child group only - a fresh user, so
        // the expanded set is exactly {child, parent} and the shared test user does not silently
        // gain a group for every other test in the suite.
        Long memberId = toh.createGroup(parentGroupId).createUser().user.getId();
        toh.addUserToGroup(memberId);

        ThreadLocalSqlSession.refreshSession();
        Set<Long> groupIds = new GroupDao().getGroupIdsWithAncestorsOfUserById(memberId);
        assertTrue(groupIds.contains(parentGroupId),
                "the groups of a member of the child group should include the parent group "
                        + parentGroupId + ", but were " + groupIds);

        AclGroupPermissionDao aclGroupPermissionDao = new AclGroupPermissionDao();
        Permission            browse                = new PermissionDao().getPermissionByName(BROWSE.getName());
        assertTrue(aclGroupPermissionDao.listAclIdsWithPermissionForGroups(groupIds, browse).contains(acl.getId()),
                "acl " + acl.getId() + " grants " + BROWSE.getName()
                        + " to the parent group, so a member of the child group has it too");
        // and the per-acl shape it replaced has to agree here as well:
        assertEquals(perAclLoop(List.of(acl), groupIds, browse), singleQuery(List.of(acl), groupIds, browse),
                "the per-acl loop and the single query disagree about an inherited permission");

        // The permission filter still has to mean something: this acl grants BROWSE and nothing
        // else, so inheritance must not turn into "the user has everything on this acl".
        Permission setAcl = new PermissionDao().getPermissionByName(SET_ACL.getName());
        assertFalse(aclGroupPermissionDao.listAclIdsWithPermissionForGroups(groupIds, setAcl).contains(acl.getId()),
                "acl " + acl.getId() + " grants no " + SET_ACL.getName() + " to any group of this user");
    }

    private Set<Long> singleQuery(List<Acl> acls, Set<Long> groupIds, Permission permission) {
        Set<Long> knownAclIds = acls.stream().map(Acl::getId).collect(Collectors.toSet());
        return new AclGroupPermissionDao().listAclIdsWithPermissionForGroups(groupIds, permission)
                .stream().filter(knownAclIds::contains).collect(Collectors.toSet());
    }

    /**
     * What AccessFilter.checkAclGroups did once per acl.
     */
    private Set<Long> perAclLoop(List<Acl> acls, Set<Long> groupIds, Permission permission) {
        AclGroupDao           aclGroupDao   = new AclGroupDao();
        AclGroupPermissionDao permissionDao = new AclGroupPermissionDao();
        Set<Long>             aclIds        = new HashSet<>();
        for (Acl acl : acls) {
            List<AclGroup> aclGroups = aclGroupDao.getAclGroupsByGroupIdsAndAcl(groupIds, acl.getId());
            aclGroupDao.getAclGroupForEveryoneGroup(acl.getId()).ifPresent(aclGroups::add);
            if (permissionDao.checkIfAnyAclGroupHasThisPermission(aclGroups, permission)) {
                aclIds.add(acl.getId());
            }
        }
        return aclIds;
    }
}
