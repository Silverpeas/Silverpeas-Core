package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of the &lt;allowedAction&gt; element of a Process Model.
 */
public interface AllowedAction {
    
    /**
     * Get the allowed action
     * 
     * @return an Action object
     */
    public Action getAction();
    
    /**
     * Set the allowed action
     * 
     * @param action an action object
     */
    public void setAction( Action action );
}
