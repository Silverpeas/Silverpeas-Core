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

<%@page import="java.io.UnsupportedEncodingException"%>
<%@ page isELIgnored="false"%>
<%
  response.setHeader("Cache-Control","no-store"); //HTTP 1.1
  response.setHeader("Pragma","no-cache"); //HTTP 1.0
  response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.util.*"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.stratelia.webactiv.util.fileFolder.FileFolderManager"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetail"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentPK"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.util.SilverpeasSettings"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@	page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>

<%@ page import="org.apache.commons.fileupload.*"%>

<%!
private String getParameterValue(List items, String parameterName, String encoding) throws UnsupportedEncodingException
{
	Iterator iter = items.iterator();
	while (iter.hasNext()) {
		FileItem item = (FileItem) iter.next();
		if (item.isFormField() && parameterName.equals(item.getFieldName())) {
			return item.getString(encoding);
		}
	}
	return null;
}

private FileItem getUploadedFile(List items, String parameterName)
{
	Iterator iter = items.iterator();
	while (iter.hasNext()) {
		FileItem item = (FileItem) iter.next();
		if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
			return item;
		}
	}
	return null;
}

private boolean runOnUnix()
{
	return !com.silverpeas.util.FileUtil.isWindows();
}

private boolean isFileSharingEnable(MainSessionController msc, String componentId)
{
	String param = msc.getOrganizationController().getComponentParameterValue(componentId, "useFileSharing");
	return "yes".equalsIgnoreCase(param);
}
%>

<%
	MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
	String					userId				= m_MainSessionCtrl.getUserId();
	String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
	ResourceLocator 		messages 			= new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", language);

	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	
	String m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	
	ResourcesWrapper attResources = new ResourcesWrapper(messages, null, null, language);
	
	// Pixels
	String noColorPix       = m_Context + "/util/icons/colorPix/1px.gif";
	String ArrayPnoColorPix = m_Context + "/util/icons/colorPix/15px.gif";
	ResourceLocator attSettings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
%>