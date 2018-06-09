package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class LifecycleServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLifecycles() throws IOException {
        HttpResponse    response   = sendStandardRequest(UrlMapping.LIFECYCLE__LIST_LIFECYCLES, new ListRequest());
        List<Lifecycle> lifecycles = parseResponse(response);

        assertNotNull(lifecycles);
        assertFalse(lifecycles.isEmpty());
        assertEquals(2, lifecycles.size());

        Optional<Lifecycle> typeOpt = lifecycles.stream().filter(lifecycle -> lifecycle.getName().equals("review.lc"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        Lifecycle type = typeOpt.get();
        assertThat(type.getId(), equalTo(1L));
    }

    private List<Lifecycle> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LifecycleWrapper lifecycleWrapper = mapper.readValue(response.getEntity().getContent(), LifecycleWrapper.class);
        assertNotNull(lifecycleWrapper);
        return lifecycleWrapper.getLifecycles();
    }


}
