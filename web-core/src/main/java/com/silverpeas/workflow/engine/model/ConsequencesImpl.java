package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Consequence;
import com.silverpeas.workflow.api.model.Consequences;

/**
 * Class implementing the representation of the &lt;consequences&gt; element of a Process Model. 
 */
public class ConsequencesImpl implements Consequences, Serializable 
{
    private Vector consequenceList;

    /**
     * Constructor
     */
    public ConsequencesImpl() 
    {
        consequenceList = new Vector();
    }

    /**
     * Get the actions
     * @return the actions as a Hashtable
     */
    public Vector getConsequenceList()
    {
        return consequenceList;
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Consequences#addConsequence(com.silverpeas.workflow.api.model.Consequence)
     */
    public void addConsequence(Consequence consequence) 
    {
        consequenceList.add(consequence);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Consequences#createConsequence()
     */
    public Consequence createConsequence()
    {
        return new ConsequenceImpl();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Consequences#iterateConsequence()
     */
    public Iterator iterateConsequence()
    {
        return consequenceList.iterator();
    }
}