package com.stratelia.webactiv.util.indexEngine.parser;

import java.util.MissingResourceException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/*
 * CVS Informations
 * 
 * $Id: ParserHelper.java,v 1.3 2006/07/10 16:22:58 neysseri Exp $
 * 
 * $Log: ParserHelper.java,v $
 * Revision 1.3  2006/07/10 16:22:58  neysseri
 * no message
 *
 * Revision 1.2  2004/06/22 15:10:35  neysseri
 * Les titres et descriptions ne sont plus restitués en lowerCase.
 *
 * Revision 1.1.1.1  2002/08/06 14:47:48  nchaix
 * no message
 *
 * Revision 1.2  2002/02/06 11:38:14  mhguig
 * stabilisation moteur de recherche
 *
 * Revision 1.1  2002/01/09 17:22:29  dwenzek
 * mise en place des silvertrace
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author $Author: neysseri $
 */
public class ParserHelper
{

    /**
     *
     * @return the path to the temp directory or null
     *
     */
    static public String getTempDirectory()
    {
        ResourceLocator resource = null;
        String          tempDirectory = null;

        try
        {
            resource = new ResourceLocator("com.stratelia.webactiv.general", "");
            if (resource != null)
            {
                tempDirectory = resource.getString("tempPath");
            }

        }
        catch (MissingResourceException e)
		{
			SilverTrace.warn("indexEngine", "ParserHelper",
			                 "indexEngine.MSG_MISSING_GENERAL_PROPERTIES", null, e);
		}
        return tempDirectory;
    }

	/**
	 * @get the time out parameter or 30000
	 */
	static public int getTimeOutParameter()
	{
		ResourceLocator resourceLocator = null;
		int timeOutParameter = 0;

		try
		{ 
			resourceLocator = new ResourceLocator("com.stratelia.webactiv.util.indexEngine.IndexEngine", "");

    
			if (resourceLocator != null)
			{
				timeOutParameter = Integer.parseInt(resourceLocator.getString("TimeOutParameter"));
			}
		}
		catch (MissingResourceException e) 
		{
			SilverTrace.warn("indexEngine", "ParserHelper",
			                 "indexEngine.MSG_MISSING_INDEXENGINE_PROPERTIES", null, e);
		}
		catch (NumberFormatException e) 
		{
			SilverTrace.warn("indexEngine", "ParserHelper",
			                 "indexEngine.MSG_PARSE_STRING_FAIL", resourceLocator.getString("TimeOutParameter"), e);
		}
		return timeOutParameter;
	}


}
