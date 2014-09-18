<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>

<%@ page import="org.silverpeas.wysiwyg.control.WysiwygController"%>
<%@ page import="org.silverpeas.wysiwyg.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="javax.servlet.http.*"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>	

<%
String spaceId = "";
String componentId = "";
String spaceName = "";
String componentName = "";
String objectId = "";
String language = "";
String contentLanguage = "";
String codeWysiwyg = "";
String returnUrl = null;
String browseInformation = null;
String fileName = "";
String path = "";
String[][] collectionPages = null;
String specificURL = "";		//For Websites only

String wysiwygTextValue = "";
String context = URLManager.getApplicationURL();
String userId = "";
String exit = "";
String indexIt = "";

String actionWysiwyg = request.getParameter("actionWysiwyg");
SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "actionWysiwyg="+actionWysiwyg);

if (actionWysiwyg == null) {
  actionWysiwyg = "Load";
}

if ("SaveHtmlAndExit".equals(actionWysiwyg) || "Refresh".equals(actionWysiwyg) || "SaveHtml".equals(actionWysiwyg)) {
  codeWysiwyg = request.getParameter("editor1");
  spaceId = (String) session.getAttribute("WYSIWYG_SpaceId");
  spaceName = (String) session.getAttribute("WYSIWYG_SpaceName");
  componentId = (String) session.getAttribute("WYSIWYG_ComponentId");
  componentName = (String) session.getAttribute("WYSIWYG_ComponentName");
  objectId = (String) session.getAttribute("WYSIWYG_ObjectId");
  browseInformation = (String) session.getAttribute("WYSIWYG_BrowseInfo");
  language = (String) session.getAttribute("WYSIWYG_Language");
  contentLanguage = (String) session.getAttribute("WYSIWYG_ContentLanguage");
  returnUrl = (String) session.getAttribute("WYSIWYG_ReturnUrl");
  userId = (String) session.getAttribute("WYSIWYG_UserId");
  fileName = (String) session.getAttribute("WYSIWYG_FileName");
  path = (String) session.getAttribute("WYSIWYG_Path");
  specificURL = (String) session.getAttribute("WYSIWYG_SpecificURL");
  indexIt = (String) session.getAttribute("WYSIWYG_IndexIt");
  exit = request.getParameter("Exit");

  if ("SaveHtmlAndExit".equals(actionWysiwyg) || "SaveHtml".equals(actionWysiwyg)) {
    //For parsing absolute url (Bug FCKEditor)
    String server = request.getRequestURL().substring(0,
        request.getRequestURL().toString().lastIndexOf(context));
    int serverPort = request.getServerPort();
    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      codeWysiwyg = codeWysiwyg.replaceAll("../../../../../", "/");
      codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
      codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
    } else {
      codeWysiwyg = codeWysiwyg.replaceAll("../../../../", context + "/");
      codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
      codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
    }

    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      WysiwygController.updateWebsite(path, fileName, codeWysiwyg);
    } else {
      boolean bIndexIt = (!StringUtil.isDefined(indexIt) || !"false".equalsIgnoreCase(indexIt));
      if (StringUtil.isDefined(contentLanguage)) {
        WysiwygController.save(codeWysiwyg, componentId, objectId, userId, contentLanguage,
            bIndexIt);
      } else {
        WysiwygController.updateFileAndAttachment(codeWysiwyg, componentId, objectId, userId,
            contentLanguage, bIndexIt);
      }
    }
  }
  if ("Refresh".equals(actionWysiwyg) || "SaveHtml".equals(actionWysiwyg)) {
    wysiwygTextValue = codeWysiwyg;
    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      collectionPages = WysiwygController.getWebsitePages(path, componentId);
      SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
          "nb collectionPages = " + collectionPages.length);
    } else {
      SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "objectId=" + objectId);
    }
  }
  if("SaveHtmlAndExit".equals(actionWysiwyg)) {
    session.removeAttribute("WYSIWYG_ContentLanguage");
  }
} else if ("Load".equals(actionWysiwyg)) {

  spaceId = request.getParameter("SpaceId");
  if (spaceId == null) {
    spaceId = (String) request.getAttribute("SpaceId");
  }
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "spaceId = " + spaceId);

  spaceName = request.getParameter("SpaceName");
  if (spaceName == null) {
    spaceName = (String) request.getAttribute("SpaceName");
  }
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "spaceName = " + spaceName);

  componentId = request.getParameter("ComponentId");
  if (componentId == null) {
    componentId = (String) request.getAttribute("ComponentId");
  }
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "componentId = " + componentId);

  componentName = request.getParameter("ComponentName");
  if (componentName == null) {
    componentName = (String) request.getAttribute("ComponentName");
  }
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "componentName = " + componentName);

  objectId = request.getParameter("ObjectId");
  if (objectId == null) {
    objectId = (String) request.getAttribute("ObjectId");
  }
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "ObjectId = " + objectId);

  returnUrl = request.getParameter("ReturnUrl");
  if (returnUrl == null) {
    returnUrl = (String) request.getAttribute("ReturnUrl");
  }
  session.setAttribute("WYSIWYG_ReturnUrl", returnUrl);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "return Url= " + returnUrl);

  browseInformation = request.getParameter("BrowseInfo");
  if (browseInformation == null) {
    browseInformation = (String) request.getAttribute("BrowseInfo");
  }

  userId = ((MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).getUserId();
  session.setAttribute("WYSIWYG_UserId", userId);

  fileName = request.getParameter("FileName");
  if (fileName == null) {
    fileName = (String) request.getAttribute("FileName");
  }
  session.setAttribute("WYSIWYG_FileName", fileName);

  path = request.getParameter("Path");
  if (path == null) {
    path = (String) request.getAttribute("Path");
  }
  if (componentId.startsWith("webSites")) {
    path = WysiwygController.getWebsiteRepository() + path;
  }
  session.setAttribute("WYSIWYG_Path", path);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "fileName= " + fileName + " Path=" + path);

  language = request.getParameter("Language");
  if (language == null) {
    language = (String) request.getAttribute("Language");
  }
  if (language == null) {
    language = "en";
  }

  contentLanguage = request.getParameter("ContentLanguage");
  if (contentLanguage == null) {
    contentLanguage = (String) request.getAttribute("ContentLanguage");
  }
  session.setAttribute("WYSIWYG_ContentLanguage", contentLanguage);

  indexIt = request.getParameter("IndexIt");
  if (indexIt == null) {
    indexIt = (String) request.getAttribute("IndexIt");
  }
  session.setAttribute("WYSIWYG_IndexIt", indexIt);

  if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
    collectionPages = WysiwygController.getWebsitePages(path, componentId);
    SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
        "nb collectionPages = " + collectionPages.length);
    specificURL = "/website/" + componentId + "/" + objectId + "/";
  } else {
    specificURL = context;
  }
  session.setAttribute("WYSIWYG_SpecificURL", specificURL);

  try {
    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      wysiwygTextValue = WysiwygController.loadFileWebsite(path, fileName);
    } else {
      wysiwygTextValue = WysiwygController.load(componentId, objectId, contentLanguage);
    }
    if (wysiwygTextValue == null) {
      wysiwygTextValue = "";
    }
  } catch (WysiwygException exc) {
    // no file
  }
}

	ResourceLocator message = new ResourceLocator("org.silverpeas.wysiwyg.multilang.wysiwygBundle",
        language);
    ResourceLocator settings =
        new ResourceLocator("org.silverpeas.wysiwyg.settings.wysiwygSettings", language);
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Window window = gef.getWindow();
  	BrowseBar 	browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceName);
  	browseBar.setComponentName(componentName, returnUrl);
  	browseBar.setExtraInformation(browseInformation);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Silverpeas Wysiwyg Editor</title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
</head>
<body>
<%
    out.println(window.printBefore());
%>

<script type="text/javascript">
	
	window.onload = function() {
		<view:wysiwyg replace="editor1" language="<%=language %>" 
			spaceId="<%=spaceId%>" spaceName="<%=spaceName%>" componentId="<%=componentId%>" componentName="<%=componentName%>" 
			browseInfo="<%=browseInformation%>" objectId="<%=objectId%>" />
			
		if ($.trim($(".wysiwyg-fileStorage").text()).length==0) {
			$(".wysiwyg-fileStorage").css("display", "none") ;
		}
	}

	function saveAndExit() {
		document.recupHtml.actionWysiwyg.value = "SaveHtmlAndExit";
		document.recupHtml.Exit.value = "1";
		document.recupHtml.submit();
	}

	function choixLien() {
		index = document.recupHtml.liens.selectedIndex;
		var str = document.recupHtml.liens.options[index].value;
		if (index != 0 && str != null) {
			CKEDITOR.instances.editor1.insertHtml('<a href="'+str+'">'+str.substring(str.lastIndexOf("/")+1)+"</a>");
		}
	}

</script>

<% if (actionWysiwyg.startsWith("SaveHtml") && "1".equals(exit)) { %>
	<script language="javascript">
	  location.href = '<%=EncodeHelper.javaStringToJsString(returnUrl)%>';
	</script>
<% } else if (actionWysiwyg.equals("Load") || actionWysiwyg.equals("Refresh") || actionWysiwyg.equals("SaveHtml")) { %>
	<form method="post" name="recupHtml" action="<%=context%>/wysiwyg/jsp/htmlEditor.jsp">
    
      <% if (I18NHelper.isI18N && StringUtil.isDefined(contentLanguage)) { %>
    <div class="container-wysiwyg wysiwyg-language"><%=I18NHelper.getLanguageLabel(contentLanguage, language)%></div>
      <% } %>
      
	<div class="container-wysiwyg wysiwyg-fileStorage">
	
		<viewTags:displayToolBarWysiwyg
		        editorName="editor1"
		        componentId="<%=componentId%>" 
		        objectId="<%=objectId%>" 
		        path="<%=path%>"/>
		        

				<%
				 // Only for WebSites
				 if (collectionPages != null) { %>
					<select name="liens" onchange="choixLien(); this.selectedIndex=0">
				        <option selected="selected"><%=message.getString("Links")%></option>
				        <% for(int i=0; i < collectionPages.length; i++ ) { %>
							<option value="<%=specificURL+collectionPages[i][0] %>"><%=collectionPages[i][1] %></option>
						<% } %>
					</select>
				<% } %>
	</div>
			
	<div class="container-wysiwyg wysiwyg-area">
		<textarea id="editor1" name="editor1" cols="10" rows="10"><c:out value="<%=wysiwygTextValue%>" escapeXml="true"/></textarea>
	</div>

		<input name="actionWysiwyg" type="hidden" value="SaveHtml"/>
		<input name="origin" type="hidden" value="<%=componentId%>"/>
		<input name="Exit" type="hidden" value="0"/>
<%
		ButtonPane buttonPane = gef.getButtonPane();
		Button button = gef.getFormButton(message.getString("SaveAndExit"), "javascript:onclick=saveAndExit();", false);
		Button buttonExit = gef.getFormButton(message.getString("Cancel"), "javascript:location.href='"+returnUrl+"';", false);
		buttonPane.addButton(button);
		buttonPane.addButton(buttonExit);
    	out.println("<br/>"+buttonPane.print());
%>
	</form>
<%
}
	out.println(window.printAfter());
%>
	</body>
</html>
