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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<view:timeout />
<%
  MainSessionController mainSessionCtrl = (MainSessionController) session
      .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

  OrganizationController organizationController =
      OrganizationControllerProvider.getOrganisationController();
  String language = mainSessionCtrl.getFavoriteLanguage();
  LocalizationBundle message =
      ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", language);

  String m_sContext = request.getContextPath();

  String[] m_asPrivateDomainsIds = mainSessionCtrl.getUserAvailRootSpaceIds();
  String title = message.getString("MyMap");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=title%></title>
  <view:looknfeel/>
  <script type="text/javascript">
    function notifyAdministrators(context, compoId, users, groups) {
      SP_openWindow('<%=m_sContext%>/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&theTargetsUsers=Administrators',
          'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
    }

    function openClipboard() {
      document.clipboardForm.submit();
    }

    function getTool(id, label, url, nb, target) {
      var res;
      if (url.substring(0, 11).toLowerCase() == "javascript:") {
        res = "<a href=\"#\" onclick=\"" + url + "\">" + label + "</a>";
      }
      else {
        res = "<a href=\"" + "<c:url value="/" />" + url + "\" target=\"" + target + "\">" + label + "</a>";
      }
      return res;
    }

    function getTools() {
      $.getJSON("<%=m_sContext%>/PersonalSpace?Action=GetTools&IEFix=" + new Date().getTime(),
          function(data) {
            try {
              // get tools
              var items = "";
              for (var i = 0; data != null && i < data.length; ++i) {
                if (i != 0) {
                  items += "&nbsp;&nbsp;|&nbsp;&nbsp;";
                }
                var tool = data[i];
                items += getTool(tool.id, tool.label, tool.url, tool.nb, '_self');
              }

              //display tools
              $("#contenu_outils").html(items);
            } catch (e) {
              //do nothing
              alert(e);
            }
          }, 'json');
    }

    function getComponent(id, label, url, name, description) {
      return "<a href=\"" + "<%=m_sContext%>" + url + "\" target=\"_self\">" + label + "</a>";
    }

    function getComponents() {
      $.getJSON("<%=m_sContext%>/PersonalSpace?Action=GetComponents&IEFix=" + new Date().getTime(),
          function(data) {
            try {
              // get components
              var items = "";
              for (var i = 0; data != null && i < data.length; ++i) {
                if (i != 0) {
                  items += "&nbsp;&nbsp;|&nbsp;&nbsp;";
                }
                var component = data[i];
                items += getComponent(component.id, component.label, component.url, component.name,
                    component.description);
              }

              //display components
              $("#contenu_components").html(items);
            } catch (e) {
              //do nothing
              alert(e);
            }
          }, 'json');
    }

    $(document).ready(function() {
      getTools();
      getComponents();
    });
  </script>
</head>
<body>
<view:browseBar extraInformations="<%=title%>"/>
<view:window>
  <view:frame>
      <view:authenticatedUser>
    <view:board>
      <table>
        <tr align="left">
          <td><img src="icons/accueil/esp_perso.gif" align="middle" alt=""/>&nbsp;&nbsp;<span
              class="txtnav"><%=message.getString("SpacePersonal")%></span></td>
        </tr>
        <tr>
          <td>
            <div id="contenu_outils"></div>
          </td>
        </tr>
        <tr>
          <td>
            <div id="contenu_components"></div>
          </td>
        </tr>
      </table>
    </view:board>
    <br/>
   </view:authenticatedUser>

    <view:board>
      <table>
        <tr>
          <td><img src="icons/accueil/esp_collabo.gif" align="middle" alt=""/> <span
              class="txtnav"><%=message.getString("SpaceCollaboration")%></span></td>
        </tr>
        <tr>
          <td valign="top">
            <%
              if (m_asPrivateDomainsIds != null) {
                for (int nK = 0; nK < m_asPrivateDomainsIds.length; nK++) {
                  SpaceInstLight spaceInst =
                      organizationController.getSpaceInstLightById(m_asPrivateDomainsIds[nK]);
                  if (spaceInst.isRoot()) {
                    pageContext.setAttribute("currentSpaceId", m_asPrivateDomainsIds[nK]);
            %>
            <view:map spaceId="${pageScope.currentSpaceId}"/>
            <%
                  }
                }
              }
            %>
          </td>
        </tr>
      </table>
    </view:board>
  </view:frame>
</view:window>
<form name="clipboardForm"
      action="<%=m_sContext+URLUtil.getURL(URLUtil.CMP_CLIPBOARD)%>Idle.jsp" method="post"
      target="IdleFrame">
  <input type="hidden" name="message" value="SHOWCLIPBOARD"/>
</form>
</body>
</html>
