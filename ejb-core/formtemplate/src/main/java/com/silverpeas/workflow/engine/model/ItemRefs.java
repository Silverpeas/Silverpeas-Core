package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Vector;

import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * TODO remove, not used
**/
public class ItemRefs extends AbstractReferrableObject implements Serializable 
{
    private Vector itemRefList;
	private String roleName = "default";
	
	/**
	 * Constructor
	 */
    public ItemRefs() 
	{
        super();
        itemRefList = new Vector();
    }

	/** TODO remove
     * Get the itemRefs
     * @return the itemRefs as a Vector
     * /
    public Vector getItemRefList()
    {
        return itemRefList;
    }

	/**
     * Get the role for which the list of items must be returned
	 * @return role name
	 */
	public String getRoleName()
	{
		 return roleName;
	}

	/**
     * Set the role for which the list of items must be returned
	 * @param	roleName role name
	 */
	public void setRoleName(String roleName)
	{
		 this.roleName = roleName;
	}

    /**
	 * Get the unique key, used by equals method
	 * @return	unique key 
     */
    public String getKey()
    {
        return (this.roleName);
    }
}
