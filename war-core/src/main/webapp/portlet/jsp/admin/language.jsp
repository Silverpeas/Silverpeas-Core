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

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<%!
  ResourceLocator messageBundle ;

public String getMessage(String messageName) {
  String message = null ;

  message = messageBundle.getString(messageName);

  if (message == null) {
    return message = messageName ;
  }
  return message ;
}
%>
<%
  MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
  messageBundle = new ResourceLocator("com.stratelia.silverpeas.portlet.multilang.portletBundle", m_MainSessionCtrl.getFavoriteLanguage());
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
%>
