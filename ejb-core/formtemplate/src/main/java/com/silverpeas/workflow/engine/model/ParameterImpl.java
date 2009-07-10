package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.engine.AbstractReferrableObject;


/**
 * Class implementing the representation of the &lt;parameter&gt; element of a Process Model.
**/
public class ParameterImpl extends AbstractReferrableObject implements Parameter, Serializable 
{
    private String name;
    private String value;

	/**
	 * Constructor
	 */
	public ParameterImpl() 
	{
        super();
		reset();
    }

	/**
	 * reset attributes
	 */
	private void reset()
	{
		name = "";
		value = "";
	}

	/**
	 * Get the name of the Parameter
	 * @return parameter's name
     */
    public String getName()
    {
        return this.name;
    }

    /**
	 * Set the name of the Parameter
	 * @param parameter's name
     */
    public void setName(String name)
    {
        this.name = name;
    }

	/**
	 * Get the value of the Parameter
	 * @return parameter's value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
	 * Set the value of the Parameter
	 * @param parameter's value
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.engine.AbstractReferrableObject#getKey()
     */
    public String getKey()
    {
        if ( name == null )
            return "";
        else
            return name;
    }
}