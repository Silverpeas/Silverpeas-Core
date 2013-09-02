<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, javax.ejb.EJBException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.util.Collection, java.util.Iterator, java.util.ArrayList, java.util.Date"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.util.*"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK, com.stratelia.webactiv.util.node.control.*, com.stratelia.webactiv.util.node.model.*, com.stratelia.webactiv.util.exception.* "%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.core.admin.OrganisationController" %>

<%
  MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
      MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  OrganisationController organizationCtrl = null;
  GraphicElementFactory gef = null;
  String language = null;
  ResourceLocator message = null;
  ResourceLocator homePageSettings = null;
  String m_sContext = null;
  if (m_MainSessionCtrl == null) {
%>
<script>
  top.location = "../../Login.jsp";
</script>
<%  } else {
    organizationCtrl = m_MainSessionCtrl.getOrganisationController();
    gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    language = m_MainSessionCtrl.getFavoriteLanguage();
    message = new ResourceLocator("org.silverpeas.homePage.multilang.homePageBundle", language);
    homePageSettings = new ResourceLocator("org.silverpeas.homePage.homePageSettings", "");

    m_sContext = URLManager.getApplicationURL();
  }
%>
