package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.RelationRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class RelationServletIntegrationTest extends CinnamonIntegrationTest {
    private final String firstRelationTypeName = "all-protector";


    @Test
    public void unhappyRequestWithoutParameters() throws IOException {
        RelationRequest request  = new RelationRequest();
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getRelationsByName() throws IOException {
        RelationRequest request = new RelationRequest();
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
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
        RelationRequest request = new RelationRequest();
        request.setLeftIds(Collections.singletonList(19L));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
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
        RelationRequest request = new RelationRequest();
        request.setRightIds(Collections.singletonList(19L));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
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
        RelationRequest request = new RelationRequest();
        request.setLeftIds(Collections.singletonList(20L));
        request.setRightIds(Collections.singletonList(19L));
        request.setIncludeMetadata(true);
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
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
        CreateRelationRequest createRequest = new CreateRelationRequest(1L, 1L, "unknown", null);
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
        CreateRelationRequest createRequest  = new CreateRelationRequest(21L, 20L, firstRelationTypeName, "<meta>m</meta>");
        HttpResponse          createResponse = sendStandardRequest(UrlMapping.RELATION__CREATE, createRequest);
        assertResponseOkay(createResponse);

        // verify relation exists:
        RelationRequest request = new RelationRequest();
        request.setLeftIds(Collections.singletonList(21L));
        request.setRightIds(Collections.singletonList(20L));
        request.setIncludeMetadata(true);
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__LIST, request);
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

    private RelationWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        RelationWrapper wrapper = mapper.readValue(response.getEntity().getContent(), RelationWrapper.class);
        assertNotNull(wrapper);
        return wrapper;
    }
}
