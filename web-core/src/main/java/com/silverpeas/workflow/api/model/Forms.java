package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;forms&gt; element of a Process Model.
 */
public interface Forms {

    /**
     * Iterate through the Form objects
     * 
     * @return an iterator
     */
    public Iterator iterateForm();
    
    /**
     * Add an form to the collection
     * 
     * @param form to be added
     */
    public void addForm( Form form );

    /**
     * Create an Form
     * 
     * @return an object implementing Form
     */
    public Form createForm();

    /**
     * Get the form definition with given name. Works fine for forms other than
     * 'presentationForm', since they have unique names.
     *  
     * @param    name    action form
     * @return form definition
     */
    public Form getForm(String name );

    /**
     * Get the form definition with given name for the given role,
     * will return the form dedicated to that role or,
     * if the former has not been found, a generic form with this name 
     * 
     * @param    name    action form
     * @param    role    role name
     * @return wanted form definition
     */
    public Form getForm(String name, String role);
    
    /**
     * Remove the form identified by name and role
     * 
     * @param strName the form name
     * @param strRole the name of the role, may be <code>null</code>
     * @throws WorkflowException if the role cannot be found
     */
    public void removeForm( String strName, String strRole ) throws WorkflowException;
}
