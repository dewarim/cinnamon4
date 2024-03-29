package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectTypeServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void listObjectTypes() throws IOException {
        List<ObjectType> objectTypes = client.listObjectTypes();
        
        assertNotNull(objectTypes);
        assertFalse(objectTypes.isEmpty());

        Optional<ObjectType> xmlOpt = objectTypes.stream().filter(objectType -> objectType.getName().equals("_default_objtype")).findFirst();
        assertTrue(xmlOpt.isPresent());
        assertThat(xmlOpt.get().getId(), equalTo(1L));
    }

    @Test
    public void create() throws IOException{
        var holder = new TestObjectHolder(adminClient);
        holder.createObjectType("create-object-type-test");
        List<ObjectType> objectTypes = client.listObjectTypes();
        assertEquals(1,objectTypes.stream().filter(objectType -> objectType.getName().equals("create-object-type-test")).count());
    }

    // TODO: implement other CRUD tests
    
}
