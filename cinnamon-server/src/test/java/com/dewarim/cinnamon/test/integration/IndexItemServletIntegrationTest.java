package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class IndexItemServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listIndexItems() throws IOException {
        HttpResponse       response      = sendStandardRequest(UrlMapping.INDEX_ITEM__LIST_INDEX_ITEMS, new ListRequest());
        List<IndexItem> indexItems = parseResponse(response);

        assertNotNull(indexItems);
        assertFalse(indexItems.isEmpty());
        assertEquals(1, indexItems.size());
        
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
        
    }

    private List<IndexItem> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        IndexItemWrapper indexItemWrapper = mapper.readValue(response.getEntity().getContent(), IndexItemWrapper.class);
        assertNotNull(indexItemWrapper);
        return indexItemWrapper.getIndexItems();
    }


}
