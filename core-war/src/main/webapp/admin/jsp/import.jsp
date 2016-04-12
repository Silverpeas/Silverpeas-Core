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

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>

<%
// Ze graffik factory
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
      GraphicElementFactory.GE_FACTORY_SESSION_ATT);

  MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
      MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

  String language = m_MainSessionCtrl.getFavoriteLanguage();
  LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);

  String m_context = ResourceLocator.getGeneralSettingBundle().
      getString("ApplicationURL");


// Icones operationBar
  String addNotif = m_context + "/util/icons/addEvent.gif";
  String paramNotif = m_context + "/util/icons/confServer.gif";

// Icones diverses
  String delete = m_context + "/util/icons/delete.gif";
  String modif = m_context + "/util/icons/update.gif";
  String up = m_context + "/util/icons/arrow/arrowUp.gif";
  String down = m_context + "/util/icons/arrow/arrowDown.gif";
  String mandatoryField = m_context + "/util/icons/squareRed.gif";

// Pixels
  String noColorPix = m_context + "/util/icons/colorPix/1px.gif";

// Divers
  String separator = "<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0><TR><TD><img src=" + noColorPix
      + "></TD></TR></TABLE>";
%>