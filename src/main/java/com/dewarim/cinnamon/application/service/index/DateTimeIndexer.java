package com.dewarim.cinnamon.application.service.index;

import org.apache.lucene.document.DateTools;
import org.dom4j.Node;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The DateTimeIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Dates must be formatted as YYYY-MM-DDThh:mm:ss.</p> 
 */
public class DateTimeIndexer extends DefaultIndexer{

	/**
	 * Convert a node containing a date formated as
	 * "2009-10-01T16:10:30" into an indexable string,
	 * which is the time in 
	 */
    public String convertNodeToString(Node node){
		String val = node.getStringValue();		
		log.debug("Trying to index: "+val);
		
		String result = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = sdf.parse(val);
			result = DateTools.dateToString(date, DateTools.Resolution.MILLISECOND);
		}catch (Exception e) {
			log.debug("failed to parse date:",e);
		}
		log.debug("Result of date conversion: "+result);
		return result;
	}

	
}
