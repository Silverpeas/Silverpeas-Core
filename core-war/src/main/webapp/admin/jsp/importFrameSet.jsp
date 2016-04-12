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
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController"%>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.util.SettingBundle"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="java.util.List"%>

<%
  MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
      MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  OrganizationController organizationCtrl = null;
  GraphicElementFactory gef = null;
  String language = null;
  LocalizationBundle message = null;
  SettingBundle homePageSettings = null;
  String m_sContext = null;
  if (m_MainSessionCtrl == null) {
%>
<script>
  top.location = "../../Login.jsp";
</script>
<%  } else {
    organizationCtrl = OrganizationControllerProvider.getOrganisationController();
    gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    language = m_MainSessionCtrl.getFavoriteLanguage();
    message = ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", language);
    homePageSettings = ResourceLocator.getSettingBundle("org.silverpeas.homePage.homePageSettings");

    m_sContext = URLUtil.getApplicationURL();
  }
%>
