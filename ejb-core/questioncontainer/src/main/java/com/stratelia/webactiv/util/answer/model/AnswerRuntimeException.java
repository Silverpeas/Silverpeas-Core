package com.stratelia.webactiv.util.answer.model;

import com.stratelia.webactiv.util.exception.*;

public class AnswerRuntimeException extends SilverpeasRuntimeException 
{

	public AnswerRuntimeException(String callingClass, int errorLevel, String message)
	{
		super(callingClass, errorLevel, message);
	}

	public AnswerRuntimeException(String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

	public AnswerRuntimeException(String callingClass, int errorLevel, String message, Exception nested)
	{
		super(callingClass, errorLevel, message, nested);
	}

	public AnswerRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
	{
		super(callingClass, errorLevel, message, extraParams, nested);
	}

	public String getModule()
	{
		return "answer";
	}

}