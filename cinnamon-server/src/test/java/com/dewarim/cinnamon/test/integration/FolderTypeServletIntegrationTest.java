package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.CinnamonClientException;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.Unwrapper;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.folderType.CreateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.ListFolderTypeRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.EXPECTED_SIZE_ANY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FolderTypeServletIntegrationTest extends CinnamonIntegrationTest {

    private final Unwrapper<FolderType, FolderTypeWrapper> folderTypeUnwrapper = new Unwrapper<>(FolderTypeWrapper.class);

    @Test
    public void create() throws IOException {
        List<FolderType> types = adminClient.createFolderTypes(Collections.singletonList("new-folder-type"));
        assertTrue(types.stream().anyMatch(ft -> ft.getName().equals("new-folder-type")));
    }

    @Test
    public void createWithoutPermission() throws IOException {
        try{
            client.createFolderTypes(Collections.singletonList("new-folder-type"));
        }
        catch (CinnamonClientException e){
            assertEquals(e.getErrorCode(), ErrorCode.REQUIRES_SUPERUSER_STATUS);
        }
    }

    @Test
    public void list() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__LIST, new ListFolderTypeRequest());
        assertResponseOkay(response);
        List<FolderType> folderTypes = folderTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
        assertTrue(folderTypes.contains(new FolderType(1L, Constants.FOLDER_TYPE_DEFAULT)));
    }

    @Test
    public void delete() throws IOException {
        HttpResponse response = sendAdminRequest(UrlMapping.FOLDER_TYPE__CREATE, new CreateFolderTypeRequest(Collections.singletonList("new-folder-type")));

    }


}
