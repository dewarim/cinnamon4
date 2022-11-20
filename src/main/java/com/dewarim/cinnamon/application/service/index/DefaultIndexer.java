package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.model.index.Indexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The DefaultIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and stored the <i>analyzed</i> results of node.getText()</p>
 */
// copied from Cinnamon 3, TODO: refactoring
public class DefaultIndexer implements Indexer {

    protected FieldType fieldType;
    boolean stored = false;

    public DefaultIndexer() {
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStored(false);
        fieldType.setTokenized(true);
    }

    transient Logger log = LogManager.getLogger(this.getClass());

    @Override
    public void indexObject(ContentContainer data, Document doc, String fieldname,
                            String searchString, Boolean multipleResults) {

        // 	log.debug("trying to index the following data:\n"+data.asString()+"\n//end of data.");
        org.dom4j.Document indexObject = data.asDocument();
        List<Node> hits = new ArrayList<>();

        if (multipleResults) {
            hits = indexObject.selectNodes(searchString);
        }
        else {
            Node node = indexObject.selectSingleNode(searchString);
            if (node != null) {
                hits.add(node);
            }
        }

        for (Node node : hits) {
            String nodeValue = convertNodeToString(node);
            if (nodeValue != null) {
                log.debug("fieldname: " + fieldname + " value: " + nodeValue + " stored:" + fieldType.stored());
                doc.add(new Field(fieldname, nodeValue, fieldType));
            }
            else {
                log.debug("nodeValue for '" + searchString + "' is null");
            }
        }
    }

    public String convertNodeToString(Node node) {
        return node.getText();
    }

    public StringBuilder descendIntoNodes(Node node) {
        List<Node> children = node.selectNodes("descendant::*");
        StringBuilder result = new StringBuilder();
        appendNonEmptyText(node, result);
        for (Node n : children) {
            appendNonEmptyText(n, result);
        }
        return result;
    }

    /**
     * If a node contains non-whitespace text, add it to the builder, followed by one whitespace.
     *
     * @param node    the node from which the text content is read
     * @param builder the StringBuilder to which the text is appended (that is, this parameter object will be modified)
     */
    public void appendNonEmptyText(Node node, StringBuilder builder) {
        String text = node.getText();
        if (text != null && text.trim().length() > 0) {
            builder.append(node.getText());
            builder.append(' ');
        }
    }

    public void setStored(boolean stored) {
        this.stored = stored;
        fieldType.setStored(stored);
    }

    public boolean isStored() {
        return this.stored;
    }
}
