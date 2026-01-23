package com.dewarim.cinnamon.test.debug;

import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;

import java.io.IOException;
import java.util.List;

/**
 * User reported OSDs staying in the Lucene index even after deletion.
 * <br>
 * This class creates and deletes lots of objects on an already running server (so we can observe the
 * behavior of the current distribution independently, not a server where transactions etc run in the same JVM)
 * <br>
 * Parts created by AI (IntelliJ Junie)
 */
public class CreateAndDeleteObjects {

    private final CinnamonClient client;

    public CreateAndDeleteObjects(CinnamonClient client) {
        this.client = client;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CinnamonClient cinnamonClient = new CinnamonClient(8080, "localhost", "http", "admin", "admin");
        cinnamonClient.connect();
        CreateAndDeleteObjects test = new CreateAndDeleteObjects(cinnamonClient);
        test.createAndDelete();
    }

    private void createAndDelete() throws IOException, InterruptedException {
        List<Folder> folders = client.getFoldersByPath("/creation", false);
        if (folders.isEmpty()) {
            throw new RuntimeException("Could not find /creation folder. Please create it first.");
        }

        UserAccount testUser = client.getUser("admin");
Long userId = testUser.getId();

        Folder testFolder = folders.getFirst();

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Creating two OSDs...");
            CreateOsdRequest createRequest1 = new CreateOsdRequest("test-osd-1", testFolder.getId(), userId, testFolder.getAclId(), 1L, null, 1L, null, "<summary>test summary 1</summary>");
            ObjectSystemData osd1 = client.createOsd(createRequest1);

            CreateOsdRequest createRequest2 = new CreateOsdRequest("test-osd-2", testFolder.getId(), userId, testFolder.getAclId(), 1L, null, 1L, null, "<summary>test summary 2</summary>");
            ObjectSystemData osd2 = client.createOsd(createRequest2);

            UpdateOsdRequest updateRequest = new UpdateOsdRequest(osd1.getId(), null, osd1.getName()+osd2.getId(), null, testFolder.getAclId(), null, null, false, false);
            client.updateOsd(updateRequest);

            System.out.println("Wait 1 second...");
            Thread.sleep(1000);

            System.out.println("Deleting one OSD: " + osd1.getId());
            client.deleteOsd(osd1.getId());

            System.out.println("Searching for OSDs...");
            String query = "<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='osd_name'>test-osd-*</WildcardQuery></Clause></BooleanQuery>";
            SearchIdsResponse searchResponse = client.search(query, SearchType.OSD);

            System.out.println("Found OSD IDs: " + searchResponse.getOsdIds().stream().sorted().toList());
            if (searchResponse.getOsdIds().contains(osd1.getId())) {
                System.out.println("*** Deleted OSD " + osd1.getId() + " still found in search index!");
            }
        }
    }

}
