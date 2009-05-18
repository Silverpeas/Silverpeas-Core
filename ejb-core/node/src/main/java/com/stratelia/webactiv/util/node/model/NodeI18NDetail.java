package com.stratelia.webactiv.util.node.model;

import com.silverpeas.util.i18n.Translation;

public class NodeI18NDetail extends Translation  implements java.io.Serializable  {

	private String nodeName	= null;
	private String nodeDescription = null;
	
	public NodeI18NDetail()
	{
	}
	
   public NodeI18NDetail(String lang, String nodeName, String nodeDescription)
	{
		if (lang != null)
			super.setLanguage(lang);
		this.nodeName = nodeName;
		this.nodeDescription = nodeDescription;
	}

   public NodeI18NDetail(int id, String lang, String nodeName, String nodeDescription)
	{
	   super.setId(id);
		if (lang != null)
			super.setLanguage(lang);
		this.nodeName = nodeName;
		this.nodeDescription = nodeDescription;
	}

	public String getDescription() {
		return nodeDescription;
	}

	public void setDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}

	public String getName() {
		return nodeName;
	}

	public void setName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getNodeId() {
		return new Integer(super.getObjectId()).intValue();
	}

	public void setNodeId(String id) {
		super.setObjectId(id);
	}

	/**
	* Return the object table name
	* @return the table name of the object
	* @since 1.0
	*/
	public String getTableName() {
	  return "SB_Node_NodeI18N";
	}


}