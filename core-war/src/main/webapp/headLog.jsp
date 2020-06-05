<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.admin.domain.model.Domain"%>
<%@page import="org.silverpeas.core.admin.user.model.User" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.silverpeas.core.security.authentication.AuthenticationService" %>
<%@ page import="org.silverpeas.core.security.authentication.AuthenticationServiceProvider" %>
<%@ page import="org.silverpeas.core.socialnetwork.model.SocialNetworkID" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.authentication.credentials.RegistrationSettings" %>
<%@ page import="java.util.List" %>

<%
  response.setDateHeader("Expires", -1);
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-control", "no-cache");
%>

<%
  String m_context = request.getContextPath();
  RegistrationSettings registrationSettings = RegistrationSettings.getSettings();

// Get the authentication settings
  SettingBundle authenticationSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.authentication.settings.authenticationSettings");
  LocalizationBundle homePageBundle =
      ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle",
          request.getLocale().getLanguage());

// Get the logo to print
  String userLanguage = (String) request.getAttribute("userLanguage");
  if (StringUtil.isNotDefined(userLanguage) && User.getCurrentRequester() != null) {
    userLanguage = User.getCurrentRequester().getUserPreferences().getLanguage();
  }
  if (StringUtil.isNotDefined(userLanguage)) {
    userLanguage = request.getLocale().getLanguage();
  }
  SettingBundle general =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
   LocalizationBundle generalMultilang =
      ResourceLocator.getGeneralLocalizationBundle(userLanguage);

  String logo = general.getString("logo", m_context + "/images/logo.jpg");
  String styleSheet = general.getString("defaultLoginStyleSheet", m_context + "/style.css");
  String favicon = general.getString("loginPage.favicon", m_context + "/util/icons/favicon.ico");

// Is "forgotten password" feature active ?
  String pwdResetBehavior = general.getString("forgottenPwdActive", "reinit");
  boolean forgottenPwdActive = !"false".equalsIgnoreCase(pwdResetBehavior);
  boolean changePwdFromLoginPageActive =
      authenticationSettings.getBoolean("changePwdFromLoginPageActive", false);
  boolean newRegistrationActive = registrationSettings.isUserSelfRegistrationEnabled();
  boolean virtualKeyboardActive = ResourceLocator.getGeneralSettingBundle().getBoolean("web.tool.virtualKeyboard", false);

// active social networks
  boolean facebookEnabled = SocialNetworkID.FACEBOOK.isEnabled();
  boolean linkedInEnabled = SocialNetworkID.LINKEDIN.isEnabled();
  boolean registrationPartActive = newRegistrationActive || facebookEnabled || linkedInEnabled;
// Get a AuthenticationService object
AuthenticationService lpAuth = AuthenticationServiceProvider.getService();
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