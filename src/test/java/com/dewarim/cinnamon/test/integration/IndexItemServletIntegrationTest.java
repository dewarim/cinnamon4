package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class IndexItemServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(IndexItemServletIntegrationTest.class);
    @Test
    public void listIndexItems() throws IOException {
        List<IndexItem> indexItems = client.listIndexItems();

        assertNotNull(indexItems);
        assertFalse(indexItems.isEmpty());

        Optional<IndexItem> itemOpt = indexItems.stream().filter(indexItem -> indexItem.getName().equals("index.acl"))
                .findFirst();
        assertTrue(itemOpt.isPresent());
        IndexItem item = itemOpt.get();
        assertThat(item.getId(), equalTo(1L));
        assertThat(item.getFieldName(), equalTo("acl"));
        assertFalse(item.isForContent());
        assertFalse(item.isForMetadata());
        assertTrue(item.isForSysMetadata());
        assertFalse(item.isMultipleResults());
        assertThat(item.getSearchString(), equalTo("/sysMeta/object/aclId"));
        assertThat(item.getSearchCondition(), equalTo("true()"));
        assertTrue(item.isStoreField());

        log.info("Found IndexItem: "+mapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(item));
    }

    @Test
    public void createIndexItemInvalidRequest() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createIndexItem(new IndexItem()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void createIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, true, true, true, "test.field", "/test", "true()", true, IndexType.DEFAULT_INDEXER));
        assertTrue(client.listIndexItems().stream().anyMatch(i -> i.getName().equals("test.field")));
    }

    @Test
    public void deleteIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, true, true, true, "test.field.delete", "/test", "true()", true, IndexType.DEFAULT_INDEXER));
        adminClient.deleteIndexItem(item.getId());
        assertTrue(client.listIndexItems().stream().noneMatch(i -> i.getName().equals("test.field.delete")));
    }

    @Test
    public void updateIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, true, true, true, "test.field.update", "/test",  "true()", true, IndexType.DEFAULT_INDEXER));
        item.setName("update.index.name");
        adminClient.updateIndexItem(item);
        List<IndexItem> items = client.listIndexItems();
        assertTrue(items.stream().noneMatch(i -> i.getName().equals("test.field.update")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("update.index.name")));

    }

    @Test
    public void createIndexItemNonSuperuser() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createIndexItem(new IndexItem()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteIndexItemNonSuperuser() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.deleteIndexItem(1L));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateIndexItemNonSuperuser() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.updateIndexItem(new IndexItem()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

}
