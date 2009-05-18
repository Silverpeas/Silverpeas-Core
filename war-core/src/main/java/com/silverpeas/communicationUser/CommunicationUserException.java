package com.silverpeas.communicationUser;

import com.stratelia.webactiv.util.exception.*;

public class CommunicationUserException extends SilverpeasException
{
	/**--------------------------------------------------------------------------constructors
	 * constructors
	 */
	public CommunicationUserException (String callingClass, int errorLevel, String message)
	{
	   super(callingClass, errorLevel, message);
	}

	public CommunicationUserException (String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

    public CommunicationUserException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public CommunicationUserException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**--------------------------------------------------------------------------getModule
	 * getModule
	 */
	public String getModule() {
	   return "communicationUser";
	}

}
