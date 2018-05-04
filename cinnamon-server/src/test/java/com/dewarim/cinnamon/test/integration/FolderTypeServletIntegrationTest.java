package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class FolderTypeServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listFolderTypes() throws IOException {
        HttpResponse       response      = sendStandardRequest(UrlMapping.FOLDER_TYPE__LIST_FOLDER_TYPES, new ListRequest());
        List<FolderType> folderTypes = parseResponse(response);

        assertNotNull(folderTypes);
        assertFalse(folderTypes.isEmpty());
        assertEquals(1, folderTypes.size());
        
        Optional<FolderType> typeOpt = folderTypes.stream().filter(folderType -> folderType.getName().equals(Constants.FOLDER_TYPE_DEFAULT))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        FolderType type = typeOpt.get();
        assertThat(type.getId(), equalTo(1L));
    }

    private List<FolderType> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        FolderTypeWrapper folderTypeWrapper = mapper.readValue(response.getEntity().getContent(), FolderTypeWrapper.class);
        assertNotNull(folderTypeWrapper);
        return folderTypeWrapper.getFolderTypes();
    }


}
