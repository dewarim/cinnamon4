package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

public class RelationServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void unhappyRequestWithoutParameters() throws IOException {
        SearchRelationRequest request = new SearchRelationRequest();
        sendStandardRequestAndAssertError(UrlMapping.RELATION__SEARCH, request, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getRelationsByTypeId() throws IOException {
        Relation relation = client.searchRelations(List.of(), List.of(), List.of(1L), false, false).getFirst();
        assertNotNull(relation.getTypeId());
        assertEquals(1L, (long) relation.getTypeId());

        // also check: no metadata if not requested
        assertNull(relation.getMetadata());
    }

    @Test
    public void getRelationsByLeftIds() throws IOException {
        var toh = prepareAclWithDefaultRelationPermissions("getRelationsByLeftIds");
        var leftOsd = toh.createOsd("left").osd;
        var rightOsd = toh.createOsd("right").osd;

        client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<none/>");

        List<Relation> relations = client.searchRelations(List.of(leftOsd.getId()), null, null, false, false);
        assertEquals(1, relations.size());

        Relation relation = relations.getFirst();
        Long typeId = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(leftOsd.getId(), leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(rightOsd.getId(), rightId);
    }

    @Test
    public void getRelationsByRightIds() throws IOException {
        var toh = prepareAclWithDefaultRelationPermissions("getRelationsByRightIds");
        var leftOsd = toh.createOsd("left").osd;
        var rightOsd = toh.createOsd("right").osd;

        client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<none/>");

        List<Relation> relations = client.searchRelations(null, List.of(rightOsd.getId()), null, false, false);
        assertEquals(1, relations.size());

        Relation relation = relations.getFirst();
        Long typeId = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(leftOsd.getId(), leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(rightOsd.getId(), rightId);
    }

    private TestObjectHolder prepareAclWithDefaultRelationPermissions(String name) throws IOException {
        return prepareAclGroupWithPermissions(name, List.of(RELATION_CHILD_ADD, RELATION_PARENT_ADD));
    }

    @Test
    public void getRelationsWithAllParameters() throws IOException {
        var toh = prepareAclWithDefaultRelationPermissions("getRelationsWithAllParameters");
        var leftOsd = toh.createOsd("left").osd;
        var rightOsd = toh.createOsd("right").osd;

        client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<none/>");

        List<Relation> relations = client.searchRelations(List.of(leftOsd.getId()), List.of(rightOsd.getId()), List.of(1L), true, false);
        assertEquals(1, relations.size());

        Relation relation = relations.getFirst();
        Long typeId = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(leftOsd.getId(), leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(rightOsd.getId(), rightId);
        String meta = relation.getMetadata();
        assertEquals("<none/>", meta);
    }

    @Test
    public void createRelationWithInvalidRequest() throws IOException {
        CreateRelationRequest createRequest = new CreateRelationRequest(0L, 0L, null, null);
        sendStandardRequestAndAssertError(UrlMapping.RELATION__CREATE, createRequest, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createRelationWithUnknownRelationType() throws IOException {
        CreateRelationRequest createRequest = new CreateRelationRequest(1L, 1L, Long.MAX_VALUE, "<meta/>");
        sendStandardRequestAndAssertError(UrlMapping.RELATION__CREATE, createRequest, ErrorCode.RELATION_TYPE_NOT_FOUND);
    }

    @Test
    public void deleteRelationWithInvalidRequest() throws IOException {
        DeleteRelationRequest deleteRequest = new DeleteRelationRequest((Long) null);
        sendStandardRequestAndAssertError(UrlMapping.RELATION__DELETE, deleteRequest, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteRelationWhichDoesNotExist() {
        assertClientError(() -> client.deleteRelation(Long.MAX_VALUE), OBJECT_NOT_FOUND);
    }

    @Test
    public void happyPathCreateAndDeleteRelation() throws IOException {
        var toh = prepareAclGroupWithPermissions("happyPathCreateAndDeleteRelation",
                List.of(BROWSE, RELATION_CHILD_ADD, RELATION_PARENT_ADD,
                        RELATION_CHILD_REMOVE, RELATION_PARENT_REMOVE));
        var leftOsd = toh.createOsd("left-osd").osd;
        var rightOsd = toh.createOsd("right-osd").osd;
        // create
        var createdRelation = client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<meta>m</meta>");
        assertNotNull(createdRelation);
        assertEquals(createdRelation.getLeftId(), leftOsd.getId());
        assertEquals(createdRelation.getRightId(), rightOsd.getId());
        assertEquals(1L, (long) createdRelation.getTypeId());

        // verify relation exists:

        List<Relation> relations = client.searchRelations(List.of(leftOsd.getId()), List.of(rightOsd.getId()), List.of(1L), true, false);
        Relation relation = relations.getFirst();
        Long typeId = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(leftOsd.getId(), (long) leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(rightOsd.getId(), (long) rightId);
        String meta = relation.getMetadata();
        assertEquals("<meta>m</meta>", meta);

        // delete
        client.deleteRelation(relation.getId());
    }

    @Test
    public void createRelationFailsWithoutChildAddPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("createRelationFailsWithoutChildAddPermission", List.of(BROWSE));
        var leftOsd = toh.createOsd("left-osd").osd;
        var rightOsd = toh.createOsd("right-osd").osd;

        // create
        assertClientError(() -> client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<meta>m</meta>"),
                NO_RELATION_CHILD_ADD_PERMISSION);
    }

    @Test
    public void deleteRelationFailsWithoutChildRemovePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("deleteRelationFailsWithoutChildRemovePermission",
                List.of(BROWSE, RELATION_CHILD_ADD, RELATION_PARENT_ADD));
        var leftOsd = toh.createOsd("left-osd").osd;
        var rightOsd = toh.createOsd("right-osd").osd;

        // create
        Relation relation = client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<meta>m</meta>");

        // delete:
        assertClientError(() -> client.deleteRelation(relation.getId()),
                NO_RELATION_CHILD_REMOVE_PERMISSION);
    }

    @Test
    public void deleteRelationFailsWithoutParentRemovePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("deleteRelationFailsWithoutParentRemovePermission",
                List.of(BROWSE, RELATION_CHILD_ADD, RELATION_PARENT_ADD,
                        RELATION_CHILD_REMOVE));
        var leftOsd = toh.createOsd("left-osd").osd;
        var rightOsd = toh.createOsd("right-osd").osd;

        // create
        Relation relation = client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<meta>m</meta>");

        // delete:
        assertClientError(() -> client.deleteRelation(relation.getId()), NO_RELATION_PARENT_REMOVE_PERMISSION);
    }

    @Test
    public void createRelationFailsWithoutParentAddPermission() throws IOException {
        var toh = prepareAclGroupWithPermissions("createRelationFailsWithoutParentAddPermission", List.of(BROWSE, RELATION_CHILD_ADD));
        var leftOsd = toh.createOsd("left-osd").osd;
        var rightOsd = toh.createOsd("right-osd").osd;

        // create
        assertClientError(() -> client.createRelation(leftOsd.getId(), rightOsd.getId(), 1L, "<meta>m</meta>"),
                NO_RELATION_PARENT_ADD_PERMISSION);
    }

    @Test
    public void getRelationsInOrMode() throws IOException {
        var toh = prepareAclWithDefaultRelationPermissions("getRelationsInOrMode");
        var left1 = toh.createOsd("left or 1").osd;
        var left2 = toh.createOsd("left or 2").osd;
        var right1 = toh.createOsd("right or 1").osd;
        var right2 = toh.createOsd("right or 2").osd;

        var relationType1 = adminClient.createRelationType(new RelationType("or-mode-test-1", false, false, false, false, false, false));
        var relationType2 = adminClient.createRelationType(new RelationType("or-mode-test-2", false, false, false, false, false, false));
        var relation1 = client.createRelation(left1.getId(), right1.getId(), relationType1.getId(), "<meta/>");
        var relation2 = client.createRelation(left2.getId(), right2.getId(), relationType2.getId(), "<meta/>");

        // should find relation1 based on leftId + relation2 based on rightId
        List<Relation> searchResult = client.searchRelations(List.of(left1.getId()), List.of(right2.getId()), null, true, true);
        assertEquals(2, searchResult.size());
        assertTrue(searchResult.contains(relation1));
        assertTrue(searchResult.contains(relation2));

        // should find relation1 based on leftId + relation2 based on typeId
        List<Relation> searchResult2 = client.searchRelations(List.of(left1.getId()), null, List.of(relationType2.getId()), true, true);
        assertEquals(2, searchResult2.size());
        assertTrue(searchResult2.contains(relation1));
        assertTrue(searchResult2.contains(relation2));
    }

    @Test
    public void getRelations() throws IOException {
        var toh = prepareAclGroupWithPermissions("getRelations",
                List.of(RELATION_CHILD_ADD, RELATION_PARENT_ADD, BROWSE));

        var left1 = toh.createOsd("left and 1").osd;
        var left2 = toh.createOsd("left and 2").osd;
        var right1 = toh.createOsd("right and 1").osd;
        var right2 = toh.createOsd("right and 2").osd;
        var left3 = toh.createOsd("left and 3").osd;
        var right3 = toh.createOsd("right and 3").osd;

        var relationType = adminClient.createRelationType(new RelationType("getRelations-test", false, false, false, false, false, false));
        var relation1 = client.createRelation(left1.getId(), right1.getId(), relationType.getId(), "<meta/>");
        var relation2 = client.createRelation(left2.getId(), right2.getId(), relationType.getId(), "<meta/>");
        var relation3 = client.createRelation(left3.getId(), right3.getId(), relationType.getId(), "<meta/>");

        var andRelations = client.getRelations(List.of(left1.getId(), right2.getId()));
        assertEquals(2, andRelations.size());
        assertTrue(andRelations.containsAll(List.of(relation1, relation2)));
        assertFalse(andRelations.contains(relation3));
    }

}
