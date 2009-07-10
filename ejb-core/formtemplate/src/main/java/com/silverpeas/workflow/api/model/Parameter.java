package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of the &lt;parameter&gt; element of a Process Model.
 */
public interface Parameter
{
    /**
	 * Get the name of the Parameter
	 * @return parameter's name
     */
    public String getName();

    /**
     * Set the name of the Parameter
     * @param parameter's name
     */
    public void setName(String name);

    /**
	 * Get the value of the Parameter
	 * @return parameter's value
     */
    public String getValue();

    /**
     * Set the value of the Parameter
     * @param parameter's value
     */
    public void setValue(String value);
}
