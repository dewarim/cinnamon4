package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.RelationRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RelationServletIntegrationTest extends CinnamonIntegrationTest {
    private final String firstRelationTypeName = "all-protector";


    @Test
    public void unhappyRequestWithoutParameters() throws IOException {
        RelationRequest request  = new RelationRequest();
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__GET_RELATIONS, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getRelationsByName() throws IOException {
        RelationRequest request = new RelationRequest();
        request.setNames(Collections.singletonList(firstRelationTypeName));
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__GET_RELATIONS, request);
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
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__GET_RELATIONS, request);
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
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__GET_RELATIONS, request);
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
        HttpResponse    response = sendStandardRequest(UrlMapping.RELATION__GET_RELATIONS, request);
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

    private RelationWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        RelationWrapper wrapper = mapper.readValue(response.getEntity().getContent(), RelationWrapper.class);
        assertNotNull(wrapper);
        return wrapper;
    }
}
