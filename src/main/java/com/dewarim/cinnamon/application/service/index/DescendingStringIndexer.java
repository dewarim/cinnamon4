package com.dewarim.cinnamon.application.service.index;

import org.dom4j.Node;

/**
 *
 */
public class DescendingStringIndexer extends DefaultIndexer {
	public String convertNodeToString(Node node){
		StringBuilder builder = descendIntoNodes(node);
		return builder.toString().trim();
	}
}
