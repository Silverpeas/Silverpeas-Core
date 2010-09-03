<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="java.util.Iterator"%>
<%@page import="com.silverpeas.util.i18n.I18NHelper"%>
<%
  response.setHeader("Cache-Control", "no-store");
  //HTTP 1.1
  response.setHeader("Pragma", "no-cache");
  //HTTP 1.0
  response.setDateHeader("Expires", -1);
%>
  <%@ page import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController" %>
  <%@ page import="com.stratelia.silverpeas.wysiwyg.*" %>
  <%@ page import="com.silverpeas.util.StringUtil"%>
  <%@ page import="com.silverpeas.wysiwyg.dynamicvalue.control.DynamicValueReplacement"%>
  <%@ page import="com.silverpeas.glossary.HighlightGlossaryTerms"%>
<%
  //initialisation des variables
  String objectId    	= request.getParameter("ObjectId");
  String spaceId     	= request.getParameter("SpaceId");
  String componentId 	= request.getParameter("ComponentId");
  String language 		= request.getParameter("Language");
  String axisId         = request.getParameter("axisId");
  String highlightFirst = request.getParameter("highlightFirst");
  

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
		  //dynamic value functionnality : check if active and try to replace the keys by their values
		  if(DynamicValueReplacement.isActivate()){
		    DynamicValueReplacement replacement = new DynamicValueReplacement();
		    content = replacement.replaceKeyByValue(content);
		  }
		  //highlight glossary term
		  if(StringUtil.isDefined(axisId)){ 	  
		  		content = HighlightGlossaryTerms.searchReplace(content,"highlight-silver",axisId,StringUtil.getBooleanValue(highlightFirst),language);
		  }
		  out.println(content);
	  }
	  else
	  {
		  out.println(WysiwygController.loadFileAndAttachment(spaceId, componentId, objectId));
	  }
  } catch (WysiwygException exc) {}
%>
