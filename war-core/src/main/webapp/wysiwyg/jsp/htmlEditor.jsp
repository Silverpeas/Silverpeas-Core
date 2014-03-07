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

<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.silverpeas.treeMenu.model.NodeType"%>

<%@ page import="org.silverpeas.wysiwyg.control.WysiwygController"%>
<%@ page import="org.silverpeas.wysiwyg.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.silverpeas.wysiwyg.dynamicvalue.control.DynamicValueReplacement" %>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.io.File"%>
<%@ page import="javax.servlet.http.*"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

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
String[][] collectionImages = null;
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
        request.getRequestURL().toString().lastIndexOf(URLManager.getApplicationURL()));
    int serverPort = request.getServerPort();
    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      codeWysiwyg = codeWysiwyg.replaceAll("../../../../../", "/");
      codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
      codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
    } else {
      codeWysiwyg = codeWysiwyg.replaceAll("../../../../", URLManager.getApplicationURL() + "/");
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
      collectionImages = WysiwygController.getWebsiteImages(path, componentId);
      collectionPages = WysiwygController.getWebsitePages(path, componentId);
      SilverTrace.info("wysiwyg", "wysiwyg.htmlEditor.jsp", "root.MSG_GEN_PARAM_VALUE",
          "nb collectionPages = " + collectionPages.length + " nb collectionImages=" +
              collectionImages.length);
    } else {
      SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "objectId=" + objectId);
      collectionImages = WysiwygController.getImages(objectId, componentId);
      specificURL = URLManager.getApplicationURL();
    }
  }
  if("SaveHtmlAndExit".equals(actionWysiwyg)) {
    session.removeAttribute("WYSIWYG_ContentLanguage");
  }
} else if ("Load".equals(actionWysiwyg)) {

  spaceId = request.getParameter("SpaceId");
  session.setAttribute("WYSIWYG_SpaceId", spaceId);

  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "spaceId = " + spaceId);

  spaceName = request.getParameter("SpaceName");
  session.setAttribute("WYSIWYG_SpaceName", spaceName);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "spaceName = " + spaceName);

  componentId = request.getParameter("ComponentId");
  session.setAttribute("WYSIWYG_ComponentId", componentId);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "componentId = " + componentId);

  componentName = request.getParameter("ComponentName");
  session.setAttribute("WYSIWYG_ComponentName", componentName);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "componentName = " + componentName);

  objectId = request.getParameter("ObjectId");
  session.setAttribute("WYSIWYG_ObjectId", objectId);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "ObjectId = " + objectId);

  returnUrl = request.getParameter("ReturnUrl");
  session.setAttribute("WYSIWYG_ReturnUrl", returnUrl);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "return Url= " + returnUrl);

  browseInformation = request.getParameter("BrowseInfo");
  session.setAttribute("WYSIWYG_BrowseInfo", browseInformation);
    userId = ((MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).getUserId();
  session.setAttribute("WYSIWYG_UserId", userId);

  fileName = request.getParameter("FileName");
  session.setAttribute("WYSIWYG_FileName", fileName);
  path = request.getParameter("Path");
  if (componentId.startsWith("webSites")) {
    path = WysiwygController.getWebsiteRepository() + path;
  }
  session.setAttribute("WYSIWYG_Path", path);
  SilverTrace.debug("wysiwyg", "Wysiwyg.htmlEditorJSP", "createSite", "fileName= " + fileName + " Path=" + path);

  language = request.getParameter("Language");
  if (language == null) {
    language = "en";
  }
  session.setAttribute("WYSIWYG_Language", language);
  contentLanguage = request.getParameter("ContentLanguage");
  session.setAttribute("WYSIWYG_ContentLanguage", contentLanguage);

  indexIt = request.getParameter("IndexIt");
  session.setAttribute("WYSIWYG_IndexIt", indexIt);

  if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
    collectionImages = WysiwygController.getWebsiteImages(path, componentId);
    collectionPages = WysiwygController.getWebsitePages(path, componentId);
    SilverTrace.info("wysiwyg", "wysiwyg.htmlEditor.jsp", "root.MSG_GEN_PARAM_VALUE",
        "nb collectionPages = " + collectionPages.length + " nb collectionImages=" +
            collectionImages.length);
    specificURL = "/website/" + componentId + "/" + objectId + "/";
  } else {
    collectionImages = WysiwygController.getImages(objectId, componentId);
    specificURL = URLManager.getApplicationURL();
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

<script type="text/javascript" src='<c:url value="/util/javaScript/animation.js" />'></script>
<view:includePlugin name="wysiwyg"/>
</head>
<body>
<%
    out.println(window.printBefore());
%>

<script type="text/javascript">
	var galleryWindow = window;
	window.onload = function() {
		<view:wysiwyg replace="editor1" language="<%=language %>"/>
	}

	function getCKEditor() {
		return CKEDITOR.instances.editor1;
	}

	function saveAndExit() {
		document.recupHtml.actionWysiwyg.value = "SaveHtmlAndExit";
		document.recupHtml.Exit.value = "1";
		document.recupHtml.submit();
	}

	function choixImage() {
		index = document.recupHtml.images.selectedIndex;
		var str = document.recupHtml.images.options[index].value;
		var title = document.recupHtml.images.options[index].text;

		if (index != 0 && str != null) {
			var ext = title.substring(title.length - 4);
    	    if (ext.toLowerCase() == ".swf") {//du flash
    	    	getCKEditor().insertHtml('<embed type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" src="'+str+'"></embed>');
			} else {
				getCKEditor().insertHtml('<img border="0" src="'+str+'" alt=""/>');
			}
		}
	}

	function choixGallery() {
		index = document.recupHtml.galleries.selectedIndex;
		var componentId = document.recupHtml.galleries.options[index].value;
		if (index != 0) {
			url = "<%=context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&amp;Language=<%=language%>";
			windowName = "galleryWindow";
			larg = "820";
			haut = "600";
			windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
			if (!galleryWindow.closed && galleryWindow.name=="galleryWindow") {
				galleryWindow.close();
			}
			galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
		}
	}

	function choixImageInGallery(url) {
		getCKEditor().insertHtml('<img border="0" src="'+url+'" alt=""/>');
	}

	function choixLien() {
		index = document.recupHtml.liens.selectedIndex;
		var str = document.recupHtml.liens.options[index].value;
		if (index != 0 && str != null) {
			getCKEditor().insertHtml('<a href="'+str+'">'+str.substring(str.lastIndexOf("/")+1)+"</a>");
		}
	}

	function chooseDynamicValuesdefault(){
		index = document.recupHtml.dynamicValues.selectedIndex;
		var str = document.recupHtml.dynamicValues.options[index].value;
		if (index != 0 && str != null){
			getCKEditor().insertHtml('(%'+str+'%)');
		}
	}

	var storageFileWindow=window;
	function openStorageFilemanager(){
		index = document.getElementById("storageFile").selectedIndex;
		var componentId = document.getElementById("storageFile").options[index].value;
		if (index != 0){
			url = "<%=context%>/kmelia/jsp/attachmentLinkManagement.jsp?key="+componentId+"&ntype=<%=NodeType.COMPONENT%>";
			windowName = "StorageFileWindow";
			width = "750";
			height = "580";
			windowParams = "scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised";
			if (!storageFileWindow.closed && storageFileWindow.name==windowName) {
				storageFileWindow.close();
			}
			storageFileWindow = SP_openWindow(url, windowName, width, height, windowParams);
		}
	}

	function insertAttachmentLink(url, img, label){
		getCKEditor().insertHtml('<a href="'+url+'" target="_blank"><img src="'+img+'" width="20" border="0" align="top" alt=""/> '+label+'</a> ');
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

		<% List fileStorage = WysiwygController.getStorageFile(userId);
 		   request.setAttribute("fileStorage",fileStorage);
		%>
		<c:if test="<%=!fileStorage.isEmpty()%>">
			<select id="storageFile" name="componentId" onchange="openStorageFilemanager();this.selectedIndex=0">
			<option value=""><%=message.getString("storageFile.select.title")%></option>
			<c:forEach items="${fileStorage}" var="option" >
				<option value="${option.id}"><%= ((ComponentInstLight)pageContext.findAttribute("option")).getLabel(language) %></option>
			</c:forEach>
			</select>
	  	</c:if>
				<select id="images" name="images" onchange="choixImage();this.selectedIndex=0">
					<option selected="selected"><%=message.getString("Image")%></option>
							<% for(int i=0; collectionImages != null && i < collectionImages.length; i++ ) { %>
									<option value="<%=specificURL+collectionImages[i][0] %>"><%=collectionImages[i][1] %></option>
							<% } %>
				</select>
				<%
					List<ComponentInstLight> galleries = WysiwygController.getGalleries();
					if (galleries != null) {
						%>
						<select id="galleries" name="galleries" onchange="choixGallery();this.selectedIndex=0;">
							<option selected="selected"><%=message.getString("Galleries")%></option>
							<% for (ComponentInstLight gallery : galleries) { %>
								<option value="<%=gallery.getId() %>"><%=gallery.getLabel() %></option>
							<% } %>
						</select>
						<%
					}
				%>
				<%-- code adding for dynamic value functionnality --%>
				<%
					if(DynamicValueReplacement.isActivate()){
					  out.println(DynamicValueReplacement.buildHTMLSelect(language,"default"));
					}
				%>
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
				<textarea id="editor1" name="editor1" cols="10" rows="10"><%=wysiwygTextValue%></textarea>
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