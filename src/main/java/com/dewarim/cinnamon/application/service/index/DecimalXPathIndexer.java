package com.dewarim.cinnamon.application.service.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The DecimalXPathIndexer is based upon the DefaultIndexer and expects an XPath parameter as searchString.
 * It stores the results in the Lucene document under the given name as "Double" length floating point values.</p>
 * If the string found cannot be converted into a valid Double, no result is saved.
 * <p>Note: the given number is indexed with a "," as decimal separator. If you index
 * a value "1,23", you must query it as "00000000001,23000000". This way, you can safely index
 * and search decimal values without having to worry about some people using "." or ",", whether that
 * corresponds correctly to <i>their and the server's</i> locale.</p>
 * <p>This class will not create proper index entries for decimal values with "." like 1.23.</p> 
 */
public class DecimalXPathIndexer extends DefaultIndexer{


	public DecimalXPathIndexer(){
		fieldType.setTokenized(false);
	}
	
	
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

		for(Node node : hits){
			Double myDouble;
			try{
				myDouble = Double.valueOf(node.getText().trim());
			}
			catch (NumberFormatException e) {
				log.debug("decimal parsing failed.");
				myDouble= null;
			}

			log.debug("fieldName: " + fieldName + " value:" + myDouble);
			luceneDoc.add(new DoublePoint(fieldName, myDouble));
		}


	}

}
