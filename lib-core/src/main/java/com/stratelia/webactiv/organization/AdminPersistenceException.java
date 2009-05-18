package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.beans.admin.AdminException;

public  class AdminPersistenceException extends AdminException
{
	/**--------------------------------------------------------------------------constructor
	 * constructor
	 */
    public AdminPersistenceException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public AdminPersistenceException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public AdminPersistenceException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public AdminPersistenceException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }
}
