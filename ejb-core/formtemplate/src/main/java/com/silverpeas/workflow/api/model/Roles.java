package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;roles&gt; element of a Process Model.
 */
public interface Roles 
{    
    /**
     * Iterate through the Role objects
     * 
     * @return an iterator
     */
    public Iterator iterateRole();
    
    /**
     * Create a Role
     * 
     * @return an object implementing Role
     */
    public Role createRole();
    
    /**
     * Add an role to the collection
     * 
     * @param role to be added
     */
    public void addRole( Role role );
    
    /**
     * Get the roles definition
     * @return roles definition
     */
    public Role[] getRoles();

    /**
     * Get the role definition with given name
     * @param    name    role name
     * @return wanted role definition
     */
    public Role getRole(String name);

    /**
     * Remove an role from the collection
     *   
     * @param strRoleName the name of the role to be removed.
     * @throws WorkflowException if the role cannot be found.
     */
    public void removeRole( String strRoleName ) throws WorkflowException;
}
