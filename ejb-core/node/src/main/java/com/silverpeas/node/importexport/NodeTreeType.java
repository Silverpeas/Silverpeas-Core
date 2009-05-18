/*
 * Created on 24 janv. 2005
 */
package com.silverpeas.node.importexport;

import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * Classe utilisée pour le (un)marshalling Castor
 * @author sdevolder
 */
public class NodeTreeType {

	private String componentId;
	private NodeDetail nodeDetail;
	
	/**
	 * @return
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * @return
	 */
	public NodeDetail getNodeDetail() {
		return nodeDetail;
	}

	/**
	 * @param string
	 */
	public void setComponentId(String string) {
		componentId = string;
	}

	/**
	 * @param detail
	 */
	public void setNodeDetail(NodeDetail detail) {
		nodeDetail = detail;
	}

}
