package com.stratelia.webactiv.util.publication.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class PublicationRuntimeException extends SilverpeasRuntimeException
{

	public PublicationRuntimeException(String callingClass, int errorLevel, String message)
	{
		super(callingClass, errorLevel, message);
	}

	public PublicationRuntimeException(String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

	public PublicationRuntimeException(String callingClass, int errorLevel, String message, Exception nested)
	{
		super(callingClass, errorLevel, message, nested);
	}

	public PublicationRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
	{
		super(callingClass, errorLevel, message, extraParams, nested);
	}

	public String getModule()
	{
		return "publication";
	}

}