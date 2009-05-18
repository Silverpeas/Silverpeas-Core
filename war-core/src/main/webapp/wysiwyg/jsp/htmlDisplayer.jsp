
<%@page import="java.util.Iterator"%>
<%@page import="com.silverpeas.util.i18n.I18NHelper"%><%
  response.setHeader("Cache-Control", "no-store");
  //HTTP 1.1
  response.setHeader("Pragma", "no-cache");
  //HTTP 1.0
  response.setDateHeader("Expires", -1);
%>
  <%@ page import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController" %>
  <%@ page import="com.stratelia.silverpeas.wysiwyg.*" %>
  <%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode" %>
  <%@ page import="com.silverpeas.util.StringUtil"%>
<%
  //initialisation des variables
  String objectId    	= request.getParameter("ObjectId");
  String spaceId     	= request.getParameter("SpaceId");
  String componentId 	= request.getParameter("ComponentId");
  String language 		= request.getParameter("Language");

  try {
	  if (StringUtil.isDefined(language))
	  {
		  String content = WysiwygController.load(componentId, objectId, language);
		  
		  //if content not found in specified language, check other ones
		  if (!StringUtil.isDefined(content))
		  {
			  Iterator languages = I18NHelper.getLanguages();
			  if (languages != null)
			  {
				  while (languages.hasNext() && !StringUtil.isDefined(content))
				  {
					  language 	= (String) languages.next();
					  content 	= WysiwygController.load(componentId, objectId, language);
				  }
			  }
		  }
		  out.println(content);
	  }
	  else
	  {
		  out.println(WysiwygController.loadFileAndAttachment(spaceId, componentId, objectId));
	  }
  } catch (WysiwygException exc) {}
%>
