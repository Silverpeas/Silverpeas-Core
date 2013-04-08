<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page import="com.silverpeas.socialnetwork.model.SocialNetworkID" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.silverpeas.authentication.AuthenticationService" %>
<%@ page import="com.stratelia.webactiv.beans.admin.Domain" %>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.List" %>

<%
  response.setHeader("Expires", "Tue, 21 Dec 1993 23:59:59 GMT");
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-control", "no-cache");
  response.setHeader("Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT");
  response.setStatus(HttpServletResponse.SC_CREATED);
%>

<%
  String m_context = request.getContextPath();

// Get the authentication settings
  ResourceLocator authenticationSettings =
      new ResourceLocator("org.silverpeas.authentication.settings.authenticationSettings", "");
  ResourceLocator homePageBundle =
      new ResourceLocator("org.silverpeas.homePage.multilang.homePageBundle",
          request.getLocale().getLanguage());

// Get the logo to print
  ResourceLocator general =
      new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
   ResourceLocator generalMultilang =
      new ResourceLocator("org.silverpeas.multilang.generalMultilang",
          request.getLocale().getLanguage());

  String logo = general.getString("logo", m_context + "/images/logo.jpg");
  String styleSheet = general.getString("defaultLoginStyleSheet", m_context + "/style.css");

// Is "forgotten password" feature active ?
  String pwdResetBehavior = general.getString("forgottenPwdActive", "reinit");
  boolean forgottenPwdActive = !"false".equalsIgnoreCase(pwdResetBehavior);
  boolean changePwdFromLoginPageActive =
      authenticationSettings.getBoolean("changePwdFromLoginPageActive", false);
  boolean rememberPwdActive = authenticationSettings.getBoolean("cookieEnabled", false);
  boolean newRegistrationActive =
      authenticationSettings.getBoolean("newRegistrationEnabled", false);

// active social networks
  boolean facebookEnabled = SocialNetworkID.FACEBOOK.isEnabled();
  boolean linkedInEnabled = SocialNetworkID.LINKEDIN.isEnabled();
// Get a AuthenticationService object
AuthenticationService lpAuth = new AuthenticationService();
// list of domains
List<Domain> listDomains = lpAuth.getAllDomains();
pageContext.setAttribute("listDomains", listDomains);
boolean multipleDomains = listDomains != null && listDomains.size() > 1;
pageContext.setAttribute("multipleDomains", multipleDomains);
String submitClass = "submitWithOneDomain";
if (multipleDomains) {
  submitClass = "submit";
}
%>