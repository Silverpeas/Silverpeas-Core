package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;allowedActions&gt; element of a Process Model.
 */
public interface AllowedActions 
{
    /**
     * Iterate through the AllowedAction objects
     * 
     * @return an iterator
     */
    public Iterator iterateAllowedAction();
    
    /**
     * Create an AllowedAction
     * 
     * @return an object implementing AllowedAction
     */
    public AllowedAction createAllowedAction();
    
    /**
     * Add an allowedAction to the collection
     * 
     * @param allowedAction to be added
     */
    public void addAllowedAction( AllowedAction allowedAction );
    
    /**
     * Get available actions 
     * @return allowed actions in an array
     */
    public Action[] getAllowedActions();
    
    /**
     * Get allowed action by action name
     *  
     * @param strActionName the name of the action to find
     * @return allowed action or <code>null</code> if action not found
     */
    public AllowedAction getAllowedAction( String strActionName );

    /**
     * Remove an allowedAction from the collection
     *   
     * @param strAllowedActionName the name of the allowedAction to be removed.
     */
    public void removeAllowedAction( String strAllowedActionName ) throws WorkflowException;
}
