package com.silverpeas.form;

import java.io.PrintWriter;

import com.silverpeas.util.ZipManager;
import com.stratelia.webactiv.util.*;
import com.stratelia.silverpeas.peasCore.URLManager;

public class Util
{

	private static final ResourceLocator formIcons = new ResourceLocator("com.silverpeas.form.settings.formIcons", "");
	private static final ResourceLocator settings = new ResourceLocator("com.silverpeas.form.settings.form", "");
	private static final String path = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	private	static ResourceLocator generalMessage;	
	private static ResourceLocator message;    
	private static String language = null;

	public static String getPath()
	{
		return path;
	}

	public static String getSetting(String setting)
	{
		return settings.getString(setting);
	}

	public static String getIcon(String icon)
	{
		return path + formIcons.getString(icon);
	}

	public static String getString(String msg, String language)
	{
		String s = "";
		setLanguage(language);
		if (msg.startsWith("GML."))
			s = generalMessage.getString(msg);
		else
			s = message.getString(msg);		
		return s;
	}

	public static String getJavascriptIncludes()
	{
		String includes = "";
		includes += "<script type=\"text/javascript\" src=\"" + path + "/util/javaScript/dateUtils.js" +"\"></script>\n";
		includes += "<script type=\"text/javascript\" src=\"" + path + "/util/javaScript/checkForm.js" + "\"></script>\n";
		includes += "<script type=\"text/javascript\" src=\"" + path + "/util/javaScript/animation.js" + "\"></script>\n";
		includes += "<script language=\"JavaScript\">";
		includes += "function calendar(indexForm, indexField) {";
		includes += "	SP_openWindow('"+ path + URLManager.getURL(URLManager.CMP_AGENDA)+"calendar.jsp?indiceForm='+indexForm+'&indiceElem='+indexField,'Calendrier',180,200,'');";
		includes += "}";
		includes += "</script>";
		return includes;

	}
	
	public static void getJavascriptChecker(String fieldName, PagesContext pageContext, PrintWriter out)
	{
		String jsFunction = "check"+ZipManager.transformStringToAsciiString(fieldName.replace(' ', '_'));
		out.println(" try { ");
		out.println("if (typeof("+jsFunction+") == 'function')");
     	out.println(" 	"+jsFunction+"('"+pageContext.getLanguage()+"');");
     	out.println(" } catch (e) { ");
     	out.println(" 	//catch all exceptions");
     	out.println(" } ");
	}


	private static void setLanguage(String lg)
	{
		if ((language == null)||(!language.trim().toLowerCase().equals(lg.trim().toLowerCase())))
		{
			language = lg;
			generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);	
			message = new ResourceLocator("com.silverpeas.form.multilang.formBundle", language);    
		}
	}

  
}
