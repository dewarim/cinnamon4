package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.ObjectType;
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
        assertThat(objectTypes.size(), equalTo(1));
        
        Optional<ObjectType> xmlOpt = objectTypes.stream().filter(objectType -> objectType.getName().equals("_default_objtype")).findFirst();
        assertTrue(xmlOpt.isPresent());
        assertThat(xmlOpt.get().getId(), equalTo(1L));
    }

    // TODO: implement other CRUD tests
    
}
