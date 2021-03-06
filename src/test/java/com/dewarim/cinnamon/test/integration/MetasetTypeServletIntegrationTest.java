package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MetasetTypeServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listMetasetTypes() throws IOException {
        HttpResponse      response     = sendStandardRequest(UrlMapping.METASET_TYPE__LIST, new ListMetasetTypeRequest());
        List<MetasetType> metasetTypes = parseResponse(response);

        assertNotNull(metasetTypes);
        assertFalse(metasetTypes.isEmpty());
        assertEquals(2, metasetTypes.size());

        Optional<MetasetType> typeOpt = metasetTypes.stream().filter(metasetType -> metasetType.getName().equals("license"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        MetasetType type = typeOpt.get();
        assertThat(type.getId(), equalTo(2L));
    }

    private List<MetasetType> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        MetasetTypeWrapper metasetTypeWrapper = mapper.readValue(response.getEntity().getContent(), MetasetTypeWrapper.class);
        assertNotNull(metasetTypeWrapper);
        return metasetTypeWrapper.getMetasetTypes();
    }


}
