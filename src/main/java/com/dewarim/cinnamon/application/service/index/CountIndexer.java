package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.model.index.Indexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The CountIndexer expects an XPath parameter as searchString that returns n nodes.
 * It will stored the number of nodes found by this search in the Lucene document.</p>
 * <p>Example: name="index.count", searchString="//name" will find all name-elements.
 * and stored the number.</p>
 */
public class CountIndexer implements Indexer {


    transient Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void indexObject(org.dom4j.Document xml, Element contentRoot, Document luceneDoc, String fieldName, String searchString, Boolean multipleResults) {
        List<Node> hits = new ArrayList<>();

        if (multipleResults) {
            hits = xml.selectNodes(searchString);
        } else {
            Node node = xml.selectSingleNode(searchString);
            if (node != null) {
                hits.add(node);
            }
        }
        log.debug("fieldName: " + fieldName + " count:" + hits.size());
        luceneDoc.add(new LongPoint(fieldName, hits.size()));

    }

    @Override
    public boolean isStored() {
        return false;
    }

    @Override
    public void setStored(boolean stored) {

    }

}
