package com.stratelia.silverpeas.pdcPeas.control;


import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class PdcPeasRuntimeException extends SilverpeasRuntimeException
{

	/**--------------------------------------------------------------------------constructors
	 * constructors
	 */
	public PdcPeasRuntimeException (String callingClass, int errorLevel, String message)
	{
	   super(callingClass, errorLevel, message);
	}

	public PdcPeasRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

    public PdcPeasRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public PdcPeasRuntimeException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**--------------------------------------------------------------------------getModule
	 * getModule
	 */
	public String getModule() {
	   return "pdcPeas";
	}


}

