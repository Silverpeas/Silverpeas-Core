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
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.silverpeas.directory.control.DirectorySessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.silverpeas.directory.model.Member"%>
<c:set var="browseContext" value="${requestScope.browseContext}" />
<%--<c:set var="level" value="byGroup" />
 <c:url value="/Rdirectory/Main" var="GroupUrl" >
                <c:param name="level" value="byGroup"></c:param>
            </c:url>--%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%  
	ResourceLocator settings = (ResourceLocator) request.getAttribute("Settings");
    
    String language = request.getLocale().getLanguage();
    ResourceLocator multilang = new ResourceLocator(
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle", language);
    ResourceLocator multilangG = new ResourceLocator(
        "com.stratelia.webactiv.multilang.generalMultilang", language);
    UserFull userFull = (UserFull) request.getAttribute("userFull");
    Member member = (Member) request.getAttribute("Member");

    String gml = "GML.";
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
%>

<html>
  <head>
    <view:looknfeel />
    <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>
    <script language="JavaScript">
      function OpenPopup(usersId){
        SP_openWindow('<%=m_context%>/Rdirectory/jsp/NotificationView?Recipient='+usersId , 'strWindowName', '500', '250', 'true');
      }
      function OpenPopupInvitaion(usersId,name){
        options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no, alwaysRaised";
        SP_openWindow('<%=m_context%>/Rinvitation/jsp/invite?Recipient='+usersId, 'strWindowName', '350', '200',options);
      }
    </script>
  </head>
  <body id="publicProfile">
    <view:browseBar extraInformations="Profil public"></view:browseBar>
    <view:window>
        <view:frame>
          <view:board>
            <table border="0" cellspacing="0" cellpadding="0" width="100%">
              <tr>
                <td width="50">
                	<% if (!member.haveAvatar()) { %>
                    	<img src="<%=m_context + member.getProfilPhoto()%>" alt="viewUser" class="defaultAvatar"/>
                    <% } else { %>
                    	<img src="<%=m_context + member.getProfilPhoto()%>" alt="viewUser" class="avatar"/>
                    <% } %>
                </td>
                <td>
                  <table border="0" cellspacing="0" cellpadding="5" width="100%">

                    <%      // afficher le  "Nom" si n'est pas null et n'est pas interdit de l'afficher
                        if (StringUtil.isDefined(userFull.getLastName()) && settings.
                            getBoolean("GML.lastname", true)) {
                    %>
                    <tr>
                      <td class="txtlibform" valign="baseline">
                        <%=userFull.getLastName()%>
                        <%
                            }
                            // afficher le  "Prenom" si  n'est pas null et n'est pas interdit de l'afficher
                            if (StringUtil.isDefined(userFull.getFirstName()) && settings.
                                getBoolean("GML.firstname", true)) {
                        %>
                        <%=userFull.getFirstName()%>
                      </td>
                    </tr>

                    <%                                       }

                    %>
                    <%
                        // afficher leur  "eMail" si n'est pas null et n'est pas interdit de l'afficher
                        if (StringUtil.isDefined(userFull.geteMail()) && settings.getBoolean("eMail", true)) {
                    %>
                    <tr>
                      <td class="txtlibform" valign="baseline" >
                        <a style="color: blue; text-decoration: underline" href="mailto:<%=userFull.geteMail()%> "><%=userFull.geteMail()%></a>
                      </td>
                    </tr>
                    <%
                        }
                    %>
                    <%
                        // afficher leur  "Droit" si n'est pas null et n'est pas interdit de l'afficher
                        if (StringUtil.isDefined(userFull.getAccessLevel()) && settings.
                            getBoolean("position", true)) {
                    %>
                    <tr>
                      <td class="txtlibform" valign="baseline" >
                        <%=multilangG.getString(multilang.getString(userFull.getAccessLevel()))%>
                      </td>
                    </tr>
                    <%                                       }

                    %>
                  </table>
                </td>
                <td align="right">
                  <a href="#" class="link" onclick="OpenPopupInvitaion(<%=userFull.getId()%>);">
                    Envoyer une invitation</a><br />
                  <br />
                  <a href="#" style="color: blue" onclick="OpenPopup(<%=userFull.getId()%>)">Envoyer un message</a>
                </td>
              </tr>
            </table>
          </view:board>
          <view:frame title="Informations professionnelles & Coordonnées">
            <view:board>
              <table border="0" cellspacing="0" cellpadding="5" width="100%">
                <%
                    // afficher leur  "Droit" si n'est pas null et n'est pas interdit de l'afficher
                    if (StringUtil.isDefined(userFull.getAccessLevel()) && settings.
                        getBoolean("position", true)) {
                %>
                <tr>
                  <td class="txtlibform" valign="baseline" width="30%"><%=multilangG.getString("GML.position")%></td>
                  <td valign="baseline">
                    <%=multilangG.getString(multilang.getString(userFull.getAccessLevel()))%>
                  </td>
                </tr>
                <%                                       }

                %>


                <%
                    //  r�cup�rer toutes les propri�t�s de ce User
                    String[] properties = userFull.getPropertiesNames();

                    String property = null;
                    for (int p = 0; p < properties.length; p++) {

                      property = properties[p];
                      // afficher toutes   les propri�t�s de User  si ne sont pas null et ne sont pas interdit de les afficher

                      if (StringUtil.isDefined(userFull.getValue(property)) && settings.
                          getBoolean(property, true)) {




                %>
                <tr>
                  <td class="txtlibform" valign="baseline" width="30%"><%= userFull.getSpecificLabel(language, property)%></td>
                  <td >
                    <%=userFull.getValue(property)%>
                  </td>
                </tr>
                <%
                      }
                    }
                %>

                <%
                    // afficher leur  "eMail" si n'est pas null et n'est pas interdit de l'afficher
                    if (StringUtil.isDefined(userFull.geteMail()) && settings.
                        getBoolean("eMail", true)) {
                %>
                <tr>
                  <td class="txtlibform" valign="baseline" width="30%"><%=multilangG.getString("GML.eMail")%></td>
                  <td >
                    <%=userFull.geteMail()%>
                  </td>
                </tr>
                <%
                    }
                %>
              </table>
            </view:board>
          </view:frame>
        </view:frame>
    </view:window>
  </body>
</html>