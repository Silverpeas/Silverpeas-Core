/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.treeManager.model;

import com.silverpeas.util.i18n.Translation;



public class TreeNodeI18N extends Translation implements java.io.Serializable
{
    private String     name;
    private String     description;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public TreeNodeI18N()
    {
    }

    public TreeNodeI18N(int nodeId, String lang,  String name,  String description)
    {
    	if (lang != null)
			super.setLanguage(lang);
		setObjectId(Integer.toString(nodeId));
		this.name = name;
		this.description = description;
    }

    public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return this.description;
	}
 
	public void setDescription(String description){
		this.description = description;
	}
}
