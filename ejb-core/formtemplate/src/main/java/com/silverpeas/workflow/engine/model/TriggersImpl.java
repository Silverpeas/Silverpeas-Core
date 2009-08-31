package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.api.model.Triggers;

/**
 * Class implementing the representation of the &lt;triggers&gt; element of a Process Model.
**/
public class TriggersImpl implements Serializable, Triggers 
{
    private Vector triggerList;           // a list of triggers ( Trigger objects )
    
    /**
     * Constructor
     */
    public TriggersImpl() 
    {
        super();
        triggerList = new Vector();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.engine.model.Columns#getItemRefList()
     */
    public List getTriggerList()
    {
        return triggerList;
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Columns#addColumn(com.silverpeas.workflow.api.model.Column)
     */
    public void addTrigger(Trigger trigger) 
    {
    	triggerList.addElement( trigger );
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Columns#createColumn()
     */
    public Trigger createTrigger() 
    {
        return new TriggerImpl();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Columns#iterateColumn()
     */
    public Iterator iterateTrigger() 
    {
        return triggerList.iterator();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Columns#removeAllColumns()
     */
    public void removeAllTriggers() {
    	triggerList.clear();
    }
}
