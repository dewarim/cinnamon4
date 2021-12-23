package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RelationServletIntegrationTest extends CinnamonIntegrationTest {
    private final String firstRelationTypeName = "all-protector";


    @Test
    public void unhappyRequestWithoutParameters() throws IOException {
        SearchRelationRequest request  = new SearchRelationRequest();
        HttpResponse          response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getRelationsByName() throws IOException {
        SearchRelationRequest request = new SearchRelationRequest();
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        RelationWrapper wrapper  = parseResponse(response);
        Relation        relation = wrapper.getRelations().get(0);
        Long            typeId   = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);

        // also check: no metadata if not requested
        assertNull(relation.getMetadata());
    }

    @Test
    public void getRelationsByLeftIds() throws IOException {
        SearchRelationRequest request = new SearchRelationRequest();
        request.setLeftIds(Collections.singletonList(19L));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        RelationWrapper wrapper  = parseResponse(response);
        Relation        relation = wrapper.getRelations().get(0);
        Long            typeId   = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(2L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(19L, (long) leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(20L, (long) rightId);

    }

    @Test
    public void getRelationsByRightIds() throws IOException {
        SearchRelationRequest request = new SearchRelationRequest();
        request.setRightIds(Collections.singletonList(19L));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        RelationWrapper wrapper  = parseResponse(response);
        Relation        relation = wrapper.getRelations().get(0);
        Long            typeId   = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(20L, (long) leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(19L, (long) rightId);
    }

    @Test
    public void getRelationsWithAllParameters() throws IOException {
        SearchRelationRequest request = new SearchRelationRequest();
        request.setLeftIds(Collections.singletonList(20L));
        request.setRightIds(Collections.singletonList(19L));
        request.setIncludeMetadata(true);
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        RelationWrapper wrapper  = parseResponse(response);
        Relation        relation = wrapper.getRelations().get(0);
        Long            typeId   = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(20L, (long) leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(19L, (long) rightId);
        String meta = relation.getMetadata();
        assertEquals(meta, "<meta>important</meta>");
    }

    @Test
    public void createRelationWithInvalidRequest() throws IOException {
        CreateRelationRequest createRequest = new CreateRelationRequest(0L, 0L, null, null);
        HttpResponse          response      = sendStandardRequest(UrlMapping.RELATION__CREATE, createRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createRelationWithUnknownRelationType() throws IOException {
        CreateRelationRequest createRequest = new CreateRelationRequest(1L, 1L, Long.MAX_VALUE, "<meta/>");
        HttpResponse          response      = sendStandardRequest(UrlMapping.RELATION__CREATE, createRequest);
        assertCinnamonError(response, ErrorCode.RELATION_TYPE_NOT_FOUND);
    }

    @Test
    public void deleteRelationWithInvalidRequest() throws IOException {
        DeleteRelationRequest deleteRequest = new DeleteRelationRequest(0L, 0L, null);
        HttpResponse          response      = sendStandardRequest(UrlMapping.RELATION__DELETE, deleteRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteRelationWhichDoesNotExist() throws IOException {
        DeleteRelationRequest deleteRequest = new DeleteRelationRequest(Long.MAX_VALUE, Long.MAX_VALUE, firstRelationTypeName);
        HttpResponse          response      = sendStandardRequest(UrlMapping.RELATION__DELETE, deleteRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND_OR_GONE);
    }

    @Test
    public void happyPathCreateAndDeleteRelation() throws IOException {
        // create
        var createdRelation = client.createRelation(21L, 20L, 1L, "<meta>m</meta>");

        // verify relation exists:
        SearchRelationRequest request = new SearchRelationRequest();
        request.setLeftIds(Collections.singletonList(21L));
        request.setRightIds(Collections.singletonList(20L));
        request.setIncludeMetadata(true);
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__SEARCH, request);
        RelationWrapper wrapper  = parseResponse(response);
        Relation        relation = wrapper.getRelations().get(0);
        Long            typeId   = relation.getTypeId();
        assertNotNull(typeId);
        assertEquals(1L, (long) typeId);
        Long leftId = relation.getLeftId();
        assertNotNull(leftId);
        assertEquals(21L, (long) leftId);
        Long rightId = relation.getRightId();
        assertNotNull(rightId);
        assertEquals(20L, (long) rightId);
        String meta = relation.getMetadata();
        assertEquals(meta, "<meta>m</meta>");

        // delete
        DeleteRelationRequest deleteRequest  = new DeleteRelationRequest(21L, 20L, firstRelationTypeName);
        HttpResponse          deleteResponse = sendStandardRequest(UrlMapping.RELATION__DELETE, deleteRequest);
        assertResponseOkay(deleteResponse);
    }

    /**
     * use client instead
     */
    @Deprecated()
    private RelationWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        RelationWrapper wrapper = mapper.readValue(response.getEntity().getContent(), RelationWrapper.class);
        assertNotNull(wrapper);
        return wrapper;
    }

    @Disabled("TODO: find out if we really need this feature")
    @Test
    public void getRelationsInOrMode() throws IOException {
        var toh    = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        var left1  = toh.createOsd("left or 1").osd;
        var left2  = toh.createOsd("left or 2").osd;
        var right1 = toh.createOsd("right or 1").osd;
        var right2 = toh.createOsd("right or 2").osd;

        var relationType = adminClient.createRelationType(new RelationType("or-mode-test", false, false, false, false, false, false));
        var relation1    = client.createRelation(left1.getId(), right1.getId(), relationType.getId(), "<meta/>");
        var relation2    = client.createRelation(left2.getId(), right2.getId(), relationType.getId(), "<meta/>");

        var andRelations = client.getRelations(List.of(left1.getId(), right2.getId()));
//        assertEquals(0, andRelations.size());
    }

    @Test
    public void getRelations() throws IOException {
        var toh          = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        var left1        = toh.createOsd("left and 1").osd;
        var left2        = toh.createOsd("left and 2").osd;
        var right1       = toh.createOsd("right and 1").osd;
        var right2       = toh.createOsd("right and 2").osd;
        var left3        = toh.createOsd("left and 3").osd;
        var right3       = toh.createOsd("right and 3").osd;
        var relationType = adminClient.createRelationType(new RelationType("getRelations-test", false, false, false, false, false, false));
        var relation1    = client.createRelation(left1.getId(), right1.getId(), relationType.getId(), "<meta/>");
        var relation2    = client.createRelation(left2.getId(), right2.getId(), relationType.getId(), "<meta/>");
        var relation3    = client.createRelation(left3.getId(), right3.getId(), relationType.getId(), "<meta/>");

        var andRelations = client.getRelations(List.of(left1.getId(), right2.getId()));
        assertEquals(2, andRelations.size());
        assertTrue(andRelations.containsAll(List.of(relation1, relation2)));
        assertFalse(andRelations.contains(relation3));
    }
}
