package com.silverpeas.workflow.api.user;

/**
 * 
 * @version $Revision: 1.1.1.1 $ $Date: 2002/08/06 14:47:54 $
**/
public interface User
{
    /**
	 * Get the user id
	 * @return user id
	 */
    public String getUserId();

	/**
	 * Get the user full name (firstname lastname)
	 * @return user full name
	 */
    public String getFullName();
	
	/**
	 * Returns the named info
	 */
    public String getInfo(String infoName);	
}
