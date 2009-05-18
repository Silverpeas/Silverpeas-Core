package com.silverpeas.formTemplate.ejb;

import com.stratelia.webactiv.util.exception.*;

public class FormTemplateBmRuntimeException extends SilverpeasRuntimeException 
{

	public FormTemplateBmRuntimeException(String callingClass, int errorLevel, String message)
	{
		super(callingClass, errorLevel, message);
	}

	public FormTemplateBmRuntimeException(String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

	public FormTemplateBmRuntimeException(String callingClass, int errorLevel, String message, Exception nested)
	{
		super(callingClass, errorLevel, message, nested);
	}

	public FormTemplateBmRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
	{
		super(callingClass, errorLevel, message, extraParams, nested);
	}

	public String getModule()
	{
		return "form";
	}

}