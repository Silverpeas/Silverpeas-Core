/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.silverpeasinitialize;

import java.util.*;
import java.io.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author eDurand
 * @version 1.0
 */

public class ProcessInitialize implements Runnable
{
	protected File m_InitializeSettingsFile;

	/**
	 * Constructor declaration
	 * 
	 * 
	 * @param p_InitializeSettingsFile
	 * 
	 * @see
	 */
	public ProcessInitialize(File p_InitializeSettingsFile)
	{
		m_InitializeSettingsFile = p_InitializeSettingsFile;
	}

	/**
	 * Method declaration
	 * 
	 * 
	 * @see
	 */
	public void run()
	{
		processInitializeSettingsFile();
	}

	/**
	 * Method declaration
	 *
	 *
	 * @see
	 */
	private void processInitializeSettingsFile()
	{
		try
		{
			Properties p = getPropertiesOfFile(m_InitializeSettingsFile.getAbsolutePath());

			if ((p.getProperty("Initialize") != null) && (p.getProperty("Initialize").equalsIgnoreCase("true") == true))
			{

				String		InitializeClass = p.getProperty("InitializeClass");
				Class		c = Class.forName(InitializeClass);
				IInitialize init = (IInitialize) c.newInstance();

				if (!(init instanceof IInitialize))
				{
					throw new Exception("Class " + InitializeClass + " isn't a IInitialize.");
				}

				if (init.Initialize() == false)
				{
					LogMsg(this, LOG_ERROR, "processInitializeSettingsFile", InitializeClass + ".Initialize() failed.");
				}

			}

			if ((p.getProperty("CallBack") != null) && (p.getProperty("CallBack").equalsIgnoreCase("true") == true))
			{

				String	 CallBackClass = p.getProperty("CallBackClass");
				Class	 c = Class.forName(CallBackClass);
				CallBack cb = (CallBack) c.newInstance();

				if (!(cb instanceof CallBack))
				{
					throw new Exception("Class " + CallBackClass + " isn't a CallBack.");
				}

				cb.subscribe();
			}
        }
		catch (Exception e)
		{
			LogMsg(this, LOG_ERROR, "processInitializeSettingsFile", e.getMessage());
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param fileName
	 *
	 * @return
	 *
	 * @see
	 */
	private Properties getPropertiesOfFile(String fileName)
	{
		Properties result = null;

		try
		{
			InputStream is = new FileInputStream(fileName);

			result = new Properties();
			result.load(is);
		}
		catch (Exception e)
		{
			LogMsg(this, LOG_ERROR, "getPropertiesOfFile( " + fileName + " )", e.getMessage());
		}
		return result;
	}

	private static int LOG_ERROR = 0;

	/**
	 * Method declaration
	 * 
	 * 
	 * @param obj
	 * @param debugLevel
	 * @param fct
	 * @param msg
	 * 
	 * @see
	 */
	private static void LogMsg(Object obj, int debugLevel, String fct, String msg)
	{
		String from = obj.getClass().getName() + "." + fct + "()";

		System.out.println(from + " : " + msg);
	}

}
