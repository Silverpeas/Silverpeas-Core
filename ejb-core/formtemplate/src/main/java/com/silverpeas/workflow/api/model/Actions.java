package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;actions&gt; element of a Process Model.
 */
public interface Actions {
    
    /**
     * Iterate through the Action objects
     * 
     * @return an iterator
     */
    public Iterator iterateAction();
    
    /**
     * Create an Action
     * 
     * @return an object implementing Action
     */
    public Action createAction();
    
    /**
     * Add an action to the collection
     * 
     * @param action to be added
     */
    public void addAction( Action action );
    
    /**
     * Get the actions defined for this process model
     * @return actions defined for this process model
     */
    public Action[] getActions();

    /**
     * Get the action definition with given name
     * @param    name    action name
     * @return wanted action definition
     */
    public Action getAction(String name) throws WorkflowException;

    /**
     * Remove an action from the collection
     *   
     * @param strActionName the name of the action to be removed.
     * @throws WorkflowException when the action cannot be found
     */
    public void removeAction( String strActionName ) throws WorkflowException;
}
