package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexServiceUnitTest {

    @DisplayName("Add all fields of a folder to a Lucene document")
    @Test
    public void addFolderFieldHappyTest() {
        Document doc    = new Document();
        Folder   folder = new Folder("testFolder", 1L, 2L, 3L, 4L, "<summary/>");
        folder.setId(5L);
        folder.setCreated(LocalDateTime.now());
        String folderPath = "/TMP";

        IndexService.addFolderFields(doc, folder, folderPath);

        String[] fields = {"id", "folderpath", "acl", "created",
                "name", "owner", "parent", "summary", "folder_type"};
        for (String field : fields) {
            assertEquals(field, doc.getField(field).name());
        }
        assertEquals(fields.length, doc.getFields().stream().map(IndexableField::name).distinct().toList().size());
        assertEquals(folderPath.toLowerCase(), doc.getField("folderpath").stringValue());
    }

    @DisplayName("Add all fields of an OSD to a Lucene document")
    @Test
    public void addOsdFieldHappyTest() {
        Document         doc = new Document();
        ObjectSystemData osd = new ObjectSystemData(1L, "testOsd", 1L, 2L, 3L, 4L, 5L);
        osd.setCreated(LocalDateTime.now());
        osd.setCreatorId(6L);
        osd.setModified(LocalDateTime.now());
        osd.setModifierId(7L);
        osd.setContentChanged(true);
        osd.setContentSize(100L);
        osd.setFormatId(1L);
        osd.setLatestBranch(true);
        osd.setLatestHead(true);
        osd.setLockerId(8L);
        osd.setMetadataChanged(true);
        osd.setPredecessorId(9L);
        osd.setRootId(10L);
        osd.setLifecycleStateId(11L);
        osd.setSummary("<summary>testOsd</summary>");
        String folderPath = "/TMP";

        IndexService.addOsdFields(doc, osd, folderPath);

        String[] fields = {"id", "folderpath", "acl",
                "created", "creator",
                "modified", "modifier",
                "name", "owner", "parent", "summary", "object_type",
                "cmn_version", "content_changed", "content_size", "format", "language",
                "latest_head", "latest_branch", "metadata_changed", "predecessor", "root",
                "lifecycle_state", "locker"};
        for (String field : fields) {
            assertEquals(field, doc.getField(field).name());
        }

        List<String> foundFields = doc.getFields().stream().map(IndexableField::name).distinct().sorted().toList();
        assertEquals(fields.length, foundFields.size(), "Found fields: " + foundFields);
        assertEquals(folderPath.toLowerCase(), doc.getField("folderpath").stringValue());
    }

}
