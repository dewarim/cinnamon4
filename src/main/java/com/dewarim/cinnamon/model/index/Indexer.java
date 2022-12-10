package com.dewarim.cinnamon.model.index;

import org.apache.lucene.document.Document;

public interface Indexer {

    void indexObject(org.dom4j.Document xml, Document luceneDoc, String fieldName, String searchString, Boolean multipleResults);
    boolean isStored();
    void setStored(boolean stored);

}
