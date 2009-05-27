package com.silverpeas.workflow.api.model;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;input&gt; element of a Process Model.
 */
public interface Input extends Column
{
	/**
     * Get the read-only attribute of this input
     * @return true if input is read-only
     */
    public boolean isReadonly();

    /**
     * Set the readonly attribute 
     */
    public void setReadonly(boolean readonly);

    /**
	 * Get value of mandatory attribute
	 * @return		true if item must be filled
     */
    public boolean isMandatory();

    /**
     * Set value of mandatory attribute
     * @param   mandatory   true if item must be filled
     */
    public void setMandatory(boolean mandatory);

    /**
	 * Get name of displayer used to show the item
	 * @return displayer name
     */
    public String getDisplayerName();

    /**
     * Set name of displayer used to show the item
     * @param   displayerName   displayer name
     */
    public void setDisplayerName(String displayerName);

    /**
	 * Get default value
	 * @return default value
     */
    public String getValue();

    /**
     * Set default value
     * @param   value   default value
     */
    public void setValue(String value);

    /**
     * Get all the labels
     * 
     * @return an object containing the collection of the labels
     */
    public ContextualDesignations getLabels();

	/**
	 * Get label in specific language for the given role
	 * @param		lang		label's language
	 * @param		role		role for which the label is
	 * @return		wanted label as a String object. 
	 *				If label is not found, search label with given role and default language,
	 *				if not found again, return the default label in given language,
	 *				if not found again, return the default label in default language,
	 *				if not found again, return empty string.
	 */
	public String getLabel(String role, String language);
    
    /**
     * Iterate through the Labels
     * 
     * @return an iterator
     */
    public Iterator iterateLabel();
    
    /**
     * Add a label
     * Method needed primarily by Castor
     */
    public void addLabel( ContextualDesignation label );
    
    /** 
     * Create an object implementing ContextualDesignation
     * Method needed primarily by Castor
     */
    public ContextualDesignation createDesignation();
}