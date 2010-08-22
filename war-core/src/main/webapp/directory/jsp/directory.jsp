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



<%
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    List members = (List) request.getAttribute("Members");
    Pagination pagination = (Pagination) request.getAttribute("pagination");

%>

<html>
  <head>
    <view:looknfeel />



    <style type="text/css">
      * {
        margin: 0;
        padding: 0;
      }

      /* message display page */
      .message_list {
        list-style: none;
        margin: 0;
        padding: 0;
        width: 100%;
      }

      .message_list li {
        padding: 0px;
        margin: 3px;
      }

      .message_table rd {
        padding: 0px;
        margin: 3px;
      }

      #recherche {
        background-image:
          url(<c:url value="/admin/jsp/icons/silverpeasV5/recherche.jpg"/>);
        background-repeat: no-repeat;
      }

      .mail {
        text-decoration: underline;
        color: blue;
      }

      .link {
        color: blue;
      }

      .NameLink {
        font-size: 14px;
        color: blue;
      }

      #directory #index {

        float:none;
        margin:0 auto;
        padding:4px;
        text-align:center;
        width:370px;
      }

      #directory #index a {
        font-size: 12px;
        text-decoration: none;
      }

      #directory #index a.active {
        font-weight: bolder;
        text-decoration: underline;
      }

      #directory #search {
        float:left;
        margin:5px 10px 10px auto;
        padding:5px;
        text-align:left;
        width:230px;
      }

      #directory #indexAndSearch{
        float: left;
        width: 98%;
      }
      #directory #users {
        float: left;
        width: 98%;
      }

      #directory .avatar {
        width: 60px;
        height: 70px;
        border: 0px;
        margin: 5px;

      }
      #directory #users #infoAndPhoto #profilPhoto {
        float:left;
        width:80px;
      }
      #directory #users #infoAndPhoto #info {
        float: left;
        margin-top:   5px;
      }
      #directory #users #infoAndPhoto {
        float:left;
        width:65%;
      }
      #directory #users #action {
        text-align: right;
        float:right;
        width:28%;
        padding-top: 2%;
      }
      #directory #users #pagination {
        text-align: center;
      }
      #info ul {
        margin: 0;
        padding: 0;
      }

      #info ul li {
        display:list-item;
        list-style-type:none;
      }
      #users a{
        color:blue;
        font-size:10px;
        font-weight:normal;
      }
      #info ul li .userName{
        color:blue;
        font-size:14px;
        font-weight:bold;
      }

    </style>
    <script type="text/javascript"
    src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript"
    src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script language="JavaScript">
      function OpenPopup(usersId,name ){

        usersId=usersId+'&Name='+name
        options="location=no, menubar=no,toolbar=no,scrollbars=yes, resizable        , alwaysRaised"
        SP_openWindow('<%=m_context + "/Rdirectory/jsp/NotificationView"%>?Recipient='+usersId , 'strWindowName', '500', '200',options );

      }
      function OpenPopupInvitaion(usersId,name){
        usersId=usersId+'&Name='+name
        options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no ,alwaysRaised"
        SP_openWindow('<%=m_context + "/Rinvitation/jsp/invite"%>?Recipient='+usersId, 'strWindowName', '350', '200','directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no ,alwaysRaised');
      }
    </script>

  </head>
  <body id="directory">
    <%--  <a href="${GroupUrl}" >Annuiare</a>--%>
    <c:url value="/RprofilPublic/ProfilPublic" var="profilPublic" />

    <%--<view:browseBar extraInformations="Annuaire Interne"></view:browseBar>--%>

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
                              class="link"><%=member.getLastName() + " " + member.getFirstName()%></a>
                      </li>
                      <li>
                        <a  class="userMail"class="link" href="#" class="link" onclick="OpenPopup(<%=member.getId()%>,'<%=member.getLastName() + " "
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