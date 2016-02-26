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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPersonalization.jsp" %>

<%
  String action;
  String componentId = "";
  String notificationId = "";

  action = (String) request.getParameter("Action");

  if (action == null) {
    action = "NotificationView";
  }
  if (action.equals("addPref"))
  {
    componentId = request.getParameter("componentId");
    notificationId = request.getParameter("notificationId");
    if ((componentId != null) && (componentId.length() > 0) && (notificationId != null) && (notificationId.length() > 0))
    {
        personalizationScc.addPreference(componentId,null,notificationId);
    }
    action = "NotificationView";
  }
  if (action.equals("delete"))
  {
    String id = request.getParameter("id");
    if ((id != null) && (id.length() > 0))
    {
        personalizationScc.deletePreference(id);
    }
    action = "NotificationView";
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
</head>
<body onload="javascript:resizePopup(750,430);">
<%
    browseBar.setComponentName(resource.getString("PersonalizationTitleTab1"));
    browseBar.setPath(resource.getString("browseBar_Path3"));

	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperationOfCreation(addGuideline, resource.getString("operationPane_addguideline"), "paramNotif2.jsp");

    out.println(window.printBefore());
%>

<view:frame>
<view:areaOfOperationOfCreation/>

<!-- Add commun code that display the Rules list -->
<%@ include file="paramNotif_Commun.jsp" %>

<br/>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close()", false));
	out.print(buttonPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>