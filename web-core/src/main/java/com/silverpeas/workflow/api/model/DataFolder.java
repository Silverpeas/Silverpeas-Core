package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.form.RecordTemplate;
import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;dataFolder&gt; element of a Process Model.
 */
public interface DataFolder
{
    /**
     * Get the items
     * @return the items as a Vector
     */
    public Item[] getItems();

    /**
     * Converts this object in a RecordTemplate object
     * @return the resulting RecordTemplate
     */
    public RecordTemplate toRecordTemplate(String role, String lang, boolean disabled) throws WorkflowException;

    /**
     * Get item contained in the DataFolder by role name
     * 
     * @param strRoleName to search with
     * @return an object implementing the Item interface 
     */
    public Item getItem(String strRoleName);
    
    /**
     * Iterate through the Item objects
     * 
     * @return an iterator
     */
    public Iterator iterateItem();
    
    /**
     * Create an Item
     * 
     * @return an object implementing Item
     */
    public Item createItem();
    
    /**
     * Add an item to the collection
     * 
     * @param item to be added
     */
    public void addItem( Item item );
    
    /**
     * Remove an item from the collection
     *   
     * @param strItemName the name of the item to be removed.
     * @throws WorkflowException when the item could not be found
     */
    public void removeItem( String strItemName ) throws WorkflowException;
}
