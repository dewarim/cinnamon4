package com.dewarim.cinnamon.application.service.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;

/**
 * <p>The IntegerXPathIndexer is based upon the DefaultIndexer and expects an XPath parameter as searchString.
 * It stores the results in the Lucene document under the given name as LongPoints  representations.</p>
 * If the string found cannot be converted into a valid Long, no result is saved / indexed.
 */
public class IntegerIndexer extends DefaultIndexer {
    @Override
    protected void addToDoc(Document luceneDoc, String fieldName, String nodeValue, FieldType typeOfField) {
        try {
            long longValue = Long.parseLong(nodeValue);
            luceneDoc.add(new LongPoint(fieldName, longValue));
        } catch (NumberFormatException ignore) {
            // ignored
        }
    }

}
