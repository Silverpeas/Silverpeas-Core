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
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache");        //HTTP 1.0
  response.setDateHeader("Expires", -1);          //prevents caching at the proxy server
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<view:setConstant var="mainSessionControllerAtt" constant="org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT"/>
<c:set var="mainSessionController" value="${sessionScope[mainSessionControllerAtt]}"/>
<html>
<head>
  <title>Utilitaires Silverpeas</title>
  <view:looknfeel/>
  <script language="javascript">
    function launchTool() {
      location.href = document.getElementById('tool').value;
    }
  </script>
</head>
<body>
<c:if test="${mainSessionController.currentUserDetail.accessAdmin}">

  <view:window>
    <div style="text-align: center;"><h2>Utilitaires Silverpeas</h2></div>
    <view:frame>
      <view:board>
        <div style="text-align: center;">
        <b><label for="tool">Sélectionner un utilitaire :</label></b>
        <br>
        <br/>

        <form name="tools" method="get" action="">
          <select name="tool" id="tool" size="5">
            <option selected value="domainSP2LDAP/domainSP2LDAP.jsp">Migration des utilisateurs du domaine Silverpeas vers un domaine LDAP</option>
          </select>
        </form>
      </view:board>
      <div style="text-align: center;">
        <c:url var="homePage" value="/admin/jsp/MainFrameSilverpeasV5.jsp"/>
        <fmt:message key="GML.validate" var="validateLabel"/>
        <view:buttonPane>
          <view:button label="${validateLabel}" action="javascript:onClick=launchTool();"/>
          <view:button label="Accueil Silverpeas" action="${homePage}"/>
        </view:buttonPane>
      </div>
    </view:frame>
  </view:window>
  </div>
</c:if>
</body>
</html>
