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

<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>

<%
// Ze graffik factory
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String graphicPath        = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
String m_context          = graphicPath;

// Icones operationBar
String addNotif           = graphicPath + "/util/icons/create-action/send-notification.png";
String deleteAllNotif     = graphicPath + "/util/icons/deleteAllNotif.gif";
String paramNotif         = graphicPath + "/util/icons/confServer.gif";

// Icones diverses
String delete             = graphicPath + "/util/icons/delete.gif";
String modif              = graphicPath + "/util/icons/update.gif";
String up                 = graphicPath + "/util/icons/arrow/arrowUp.gif";
String down               = graphicPath + "/util/icons/arrow/arrowDown.gif";
String mandatoryField     = graphicPath + "/util/icons/squareRed.gif";
String test               = graphicPath + "/util/icons/ok.gif";

// Pixels
String noColorPix         = graphicPath + "/util/icons/colorPix/1px.gif";

// Divers
String separator = "<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0><TR><TD><img src="+noColorPix+"></TD></TR></TABLE>";
%>