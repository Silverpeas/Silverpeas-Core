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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">


<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination"%>
<%@page import="com.silverpeas.directory.model.Member"%>
<%@page import="java.util.List"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="java.io.File"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" var="LML" />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />
<c:set var="browseContext" value="${requestScope.browseContext}" />
 <c:url value="/RprofilPublic/ProfilPublic" var="profilPublic" />


<%
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    List members = (List) request.getAttribute("Members");
    Pagination pagination = (Pagination) request.getAttribute("pagination");

%>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<title></title>
    <view:looknfeel />
    <style type="text/css">
      * {
        margin: 0;
        padding: 0;
      }
    </style>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript">
      function OpenPopup(usersId,name ){
        usersId=usersId+'&Name='+name
        options="location=no, menubar=no,toolbar=no,scrollbars=yes,resizable,alwaysRaised"
        SP_openWindow('<%=m_context + "/Rdirectory/jsp/NotificationView"%>?Recipient='+usersId , 'strWindowName', '500', '200',options );

      }
      function OpenPopupInvitaion(usersId,name){
        usersId=usersId+'&Name='+name
        options="directories=no, menubar=no,toolbar=no,scrollbars=yes,resizable=no,alwaysRaised"
        SP_openWindow('<%=m_context + "/Rinvitation/jsp/invite"%>?Recipient='+usersId, 'strWindowName', '350', '200','directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no ,alwaysRaised');
      }
    </script>

  </head>
  <body id="directory">
    <view:window>
      <view:frame>
        <div id="indexAndSearch">
          <div id="index">
            <%
                // afficher la bande d'index alphabetique
                String para = (String) request.getAttribute("IndexLetter");

                for (char i = 'A'; i <= 'Z'; ++i) {

                  if (para != null && para.equals(String.valueOf(i))) {
                    out.println(
                        "<a class=\"active\" href=\"" + i + "\">" + i + "</a>");
                  } else {
                    out.println(
                        "<a class=\"index\" href=\"" + i + "\">" + i + "</a>");
                  }
                }
                if (para != null && para.equals("tous")) {
                  out.println("<a class=\"active\" href=\"tous\">Tous</a>");
                } else {
                  out.println("<a class=\"index\" href=\"tous\">Tous</a>");
                }
            %>
          </div>
          <div id="search">
            <form name="search" action="searchByKey" method="post">
              <input type="text" name="key" size="40" maxlength="60"
                     style="height: 20px"  />
              <img
                src="<%=m_context%>/directory/jsp/icons/advsearch.jpg"
                width="10" height="10" alt="advsearch" />
            </form>
          </div>
        </div>
        <div id="users">
          <ol class="message_list">
            <%
                for (int i = 0; i < members.size(); i++) {
                  Member member = (Member) members.get(i);
            %>
            <li>
              <view:board>
                <div id="infoAndPhoto">
                  <div id="profilPhoto">
                    <a href="createPhoto"><img
                        src="<%=m_context + member.getProfilPhoto()%>"
                        alt="viewUser" class="avatar"/> </a>
                  </div>
                  <div id="info">
                    <ul>
                      <li> <a class="userName" href="<%=m_context%>/Rprofil/jsp/Main?userId=<%=member.getId()%>"
                              ><%=member.getLastName() + " " + member.getFirstName()%></a>
                      </li>
                      <li>
                        <a class="userMail" href="#"  onclick="OpenPopup(<%=member.getId()%>,'<%=member.getLastName() + " "
                                               + member.getFirstName()%>')"><%=member.geteMail()%>
                        </a>
                      </li>
                      <li>
                        <fmt:message key="<%=member.getAccessLevel()%>"
                                     bundle="${LML}" var="carAccessLevel" />
                        <fmt:message key="${carAccessLevel}" bundle="${GML}" />
                      </li>
                      <li>
                        <% if (member.isConnected()) {
                        %>
                        <img src="<%=m_context%>/directory/jsp/icons/connected.jpg" width="10" height="10"
                             alt="connected"/> <fmt:message key="directory.connected" bundle="${LML}" /><%=" " + member.getDuration()%>


                        <%
                         } else {
                        %>
                        <img src="<%=m_context%>/directory/jsp/icons/deconnected.jpg" width="10" height="10" alt="deconnected"/> <fmt:message key="directory.deconnected" bundle="${LML}" />


                        <%          }
                        %>
                      </li>
                    </ul>
                  </div>
                </div>
                <div id="action">
                  <%                          if (!request.getAttribute(
                                        "MyId").
                                        equals(member.getId())) {
                  %>
                  <% if (!member.isRelationOrInvitation(request.
                                                  getAttribute("MyId").toString())) {
                  %>

                  <a href="#" class="link" onclick="OpenPopupInvitaion(<%=member.getId()%>,'<%=member.getLastName() + " " + member.getFirstName()%>');">
                    Envoyer une invitation</a><br />
                  <br />
                  <%
                                              }
                  %>
                  <a href="#" class="link" onclick="OpenPopup(<%=member.getId()%>,'<%=member.getLastName() + " " + member.getFirstName()%>')">
                    Envoyer un message</a>
                    <%
                                      }
                    %>
                </div>
              </view:board>
            </li>
            <%
                }

            %>


          </ol>
          <div id="pagination">
            <%=pagination.printIndex()%>
          </div>
        </div>
      </view:frame>

    </view:window>

  </body>
</html>