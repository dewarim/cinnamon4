package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CINNAMON_REQUEST_PART;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PerformanceTest extends CinnamonIntegrationTest {

    @Disabled("only manual testing - this may take a long time")
    @Test
    @DisplayName("Load lots of stuff into Cinnamon test server")
    public void performanceTest() throws IOException {

        File[] images = new File("/home/ingo/Downloads/img").listFiles();
        assertNotNull(images);
        TestObjectHolder toh       = new TestObjectHolder(client, userId);
        Format           jpeg      = TestObjectHolder.formats.stream().filter(format -> format.getExtension().equals("jpg")).findFirst().orElseThrow();
        List<File>       pngImages = new ArrayList<>(Arrays.stream(images).filter(img -> img.getName().endsWith("jpg")).toList());
        ArrayList<File>  files     = new ArrayList<>(pngImages);
        while (pngImages.size() < 100_000) {
            pngImages.addAll(files);
        }
        int count = 0;
        for (File image : pngImages) {
            if(count++ % 1000 == 0) {
                System.out.println(count);
            }
            CreateOsdRequest request = new CreateOsdRequest();
            request.setAclId(defaultCreationAcl.getId());
            request.setName("new osd");
            request.setOwnerId(userId);
            request.setParentId(createFolderId);
            request.setTypeId(1L);
            request.setFormatId(jpeg.getId());
            FileBody fileBody = new FileBody(image);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .addPart("file", fileBody)
                    .addTextBody(CINNAMON_REQUEST_PART, client.getCinnamonContentType().getObjectMapper().writeValueAsString(request),
                            APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
            try (StandardResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build())) {
                assertResponseOkay(response);
            }

        }
        System.out.println("Images added: " + pngImages.size());
        performanceTest2();
    }

    @Disabled
    @Test
    public void performanceTest2() throws IOException {
        ReindexRequest reindexRequest = new ReindexRequest();
        adminClient.reindex(reindexRequest);
        for(int count = 100; count > 1; count = adminClient.getIndexInfo(false,false).getJobCount()){
            System.out.println("******* "+count);
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private StandardResponse sendStandardMultipartRequest(UrlMapping urlMapping, HttpEntity multipartEntity) throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + urlMapping.getPath();
        return httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", getDoesTicket(false))
                .setEntity(multipartEntity).build(), StandardResponse::new);
    }

}
