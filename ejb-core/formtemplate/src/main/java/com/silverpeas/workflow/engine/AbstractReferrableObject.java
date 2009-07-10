/**
 * @author ludovic Bertin
 * @version 1.0
*/

/**
 * The abstract class for all workflow objects refferable
 */

package com.silverpeas.workflow.engine;

public abstract class AbstractReferrableObject implements ReferrableObjectIntf
{

    /**
     * This method has to be implemented by the referrable object
     * it has to compute the unique key
     * @return The unique key.
	 * @see equals
	 * @see hashCode
     */
    public abstract String getKey();

	/**
	 * Tests equality with another referrable object
	 * @return true if both object's keys are equals
     */
    public boolean equals(Object theOther)
	{
    	if (theOther instanceof String)
    		return ( getKey().equals( theOther ) );
    	else
		return ( getKey().equals( ( (ReferrableObjectIntf) theOther ).getKey() ) );
	}

	/**
	 * Calculate the hashcode for this referrable object
	 * @return hashcode
     */
    public int hashCode()
	{
		return ( getKey().hashCode() );
	}
}