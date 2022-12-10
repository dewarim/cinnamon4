package com.dewarim.cinnamon.model.index;

import org.apache.lucene.document.Document;
import org.dom4j.Element;

public interface Indexer {

    void indexObject(org.dom4j.Document xml, Element contentRoot, Document luceneDoc, String fieldName, String searchString, Boolean multipleResults);
    boolean isStored();
    void setStored(boolean stored);

}
