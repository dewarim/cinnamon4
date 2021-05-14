package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import org.apache.http.HttpResponse;
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
        HttpResponse response = sendStandardRequest(UrlMapping.OBJECT_TYPE__LIST_OBJECT_TYPES, new ListObjectTypeRequest());
        List<ObjectType> objectTypes = parseResponse(response);
        
        assertNotNull(objectTypes);
        assertFalse(objectTypes.isEmpty());
        assertThat(objectTypes.size(), equalTo(1));
        
        Optional<ObjectType> xmlOpt = objectTypes.stream().filter(objectType -> objectType.getName().equals("_default_objtype")).findFirst();
        assertTrue(xmlOpt.isPresent());
        assertThat(xmlOpt.get().getId(), equalTo(1L));
    }
    
    private List<ObjectType> parseResponse(HttpResponse response) throws IOException{
        assertResponseOkay(response);
        ObjectTypeWrapper objectTypeWrapper = mapper.readValue(response.getEntity().getContent(), ObjectTypeWrapper.class);
        assertNotNull(objectTypeWrapper);
        return objectTypeWrapper.getObjectTypes();
    }
    
    
}
