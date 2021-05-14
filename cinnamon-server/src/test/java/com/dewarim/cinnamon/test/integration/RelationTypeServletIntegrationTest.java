package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class RelationTypeServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listRelationTypes() throws IOException {
        HttpResponse       response      = sendStandardRequest(UrlMapping.RELATION_TYPE__LIST_RELATION_TYPES, new ListRelationTypeRequest());
        List<RelationType> relationTypes = parseResponse(response);

        assertNotNull(relationTypes);
        assertFalse(relationTypes.isEmpty());
        assertEquals(2, relationTypes.size());

        Optional<RelationType> typeOpt = relationTypes.stream().filter(relationType -> relationType.getName().equals("all-protector"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        RelationType type = typeOpt.get();
        assertThat(type.getId(), equalTo(1L));
        assertThat(type.isCloneOnLeftCopy(), equalTo(true));
        assertThat(type.isCloneOnRightCopy(), equalTo(true));
        assertThat(type.isCloneOnLeftVersion(), equalTo(true));
        assertThat(type.isCloneOnRightVersion(), equalTo(true));
        assertThat(type.isLeftObjectProtected(), equalTo(true));
        assertThat(type.isRightObjectProtected(), equalTo(true));
    }

    private List<RelationType> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        RelationTypeWrapper relationTypeWrapper = mapper.readValue(response.getEntity().getContent(), RelationTypeWrapper.class);
        assertNotNull(relationTypeWrapper);
        return relationTypeWrapper.getRelationTypes();
    }


}
