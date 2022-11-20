package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.application.service.index.ContentContainer;
import org.apache.lucene.document.Document;

public interface Indexer {

    void indexObject(ContentContainer xml, Document doc, String fieldname, String searchString, Boolean multipleResults);
    boolean isStored();
    void setStored(boolean stored);

}
