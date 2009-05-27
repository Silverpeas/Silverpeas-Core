package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.AbstractDescriptor;
import com.silverpeas.workflow.api.model.Column;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;column&gt; element of a Process Model.
**/
public class ColumnImpl extends AbstractReferrableObject implements Column, AbstractDescriptor, Serializable 
{

    //~ Instance fields ////////////////////////////////////////////////////////

    private AbstractDescriptor parent;
    private boolean            hasId = false;
    private int                id;
    private Item               item;

    /************* Implemented methods *****************************************/

    //~ Methods ////////////////////////////////////////////////////////////////

    /*
     * @see com.silverpeas.workflow.api.model.Column#getItem()
     */
    public Item getItem()
    {
        return item;
    }

    /*
     * @see com.silverpeas.workflow.api.model.Column#setItem(Item)
     */
    public void setItem(Item item)
    {
        this.item = item;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setId(int)
     */
    public void setId(int id) 
    {
        this.id = id;
        hasId = true;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getId()
     */
    public int getId() 
    {
        return id;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setParent(com.silverpeas.workflow.api.model.AbstractDescriptor)
     */
    public void setParent(AbstractDescriptor parent) 
    {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getParent()
     */
    public AbstractDescriptor getParent() 
    {
        return parent;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#hasId()
     */
    public boolean hasId() 
    {
        return hasId;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.engine.AbstractReferrableObject#getKey()
     */
    public String getKey() 
    {
        if ( item == null )
            return "";
        else
            return item.getName();
    }
}