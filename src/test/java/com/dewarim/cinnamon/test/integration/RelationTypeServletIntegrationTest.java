package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.relations.RelationType;
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

        List<RelationType> relationTypes = client.getRelationTypes();

        assertNotNull(relationTypes);
        assertFalse(relationTypes.isEmpty());
        // exact number of relationTypes may change due to other tests creating extra relations
        assertTrue(relationTypes.size() >= 2);

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

    @Test
    public void createRelationTypes() throws IOException {
        var rt = new RelationType("left-rt-create",
                true, false,
                false, false, false, false);
        var newRt = adminClient.createRelationTypes(List.of(rt)).get(0);
        assertEquals(rt,newRt);
    }

    // TODO: add tests for updateRelationType
    // TODO: add tests for createWithInvalidName
    // TODO: add test with createWithExistingName

    @Test
    public void deleteRelationTypes() throws IOException{
        var rt = new RelationType("left-rt-delete",
                true, false,
                false, false, false, false);
        var rtToDelete = adminClient.createRelationTypes(List.of(rt)).get(0);
        adminClient.deleteRelationTypes(List.of(rtToDelete.getId()));
    }

    @Test
    public void createRequiresSuperuser(){
        RelationType relationType = new RelationType();
        assertClientError(() -> client.createRelationTypes(List.of(relationType)), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }
    
    @Test
    public void deleteRequiresSuperuser() {
        assertClientError(() -> client.deleteRelationTypes(List.of(Long.MAX_VALUE)), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteUnknownRelationType() {
        assertClientError(() -> adminClient.deleteRelationTypes(List.of(Long.MAX_VALUE)), ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteInvalidRelationType() {
        assertClientError(() -> adminClient.deleteRelationTypes(List.of(0L)), ErrorCode.INVALID_REQUEST);
    }

}
