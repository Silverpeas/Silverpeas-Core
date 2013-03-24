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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="headLog.jsp" %>
<%
  UserDetail userDetail = (UserDetail) request.getAttribute("userDetail");
  ResourceLocator authenticationBundle =
      new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=generalMultilang.getString("GML.popupTitle")%>
  </title>
  <link REL="SHORTCUT ICON" HREF="<%=request.getContextPath()%>/util/icons/favicon.ico">
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <script src="<%=m_context%>/util/javaScript/jquery/jquery-1.7.1.min.js" type="text/javascript"></script>
  <!--[if lt IE 8]>
  <style type="text/css">
    input {
      background-color: #FAFAFA;
      border: 1px solid #DAD9D9;
      width: 448px;
      text-align: left;
      margin-left: -10px;
      height: 26px;
      line-height: 24px;
      display: block;
      padding: 0;
    }
  </style>
  <![endif]-->
  <script type="text/javascript">
    var webContext = '<%=m_context%>';
    $(document).ready(function() {
      $('#questionForm').submit(function() {
        var answer = $(this).find('#answer').val();
        if (!answer || answer.length == 0) {
          alert("<%=authenticationBundle.getString("authentication.reminder.answer.empty") %>");
          return false;
        }
        this.action = '<c:url value="/CredentialsServlet/ValidateAnswer"/>';
        return true;
      });
    });
  </script>
</head>
<body>
<form id="questionForm" action="#" method="post">
  <div id="top"></div>
  <div class="page">
    <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %>
    </div>
    <div id="background">
      <div class="cadre">
        <div id="header">
          <img src="<%=logo%>" class="logo" alt=""/>

          <p class="information"><%=authenticationBundle
              .getString("authentication.reminder.label") %>
            <c:if test="${not empty requestScope.message}">
              <br/><span><c:out value="${requestScope.message}" escapeXml="false"/></span>
            </c:if>
          </p>

          <div class="clear"></div>
        </div>
        <p><label><span><%=userDetail.getLoginQuestion()%></span>
          <input type="password" name="answer" id="answer"/></label></p>
          <br/>
          <p>
          <input type="submit" style="width:0; height:0; border:0; padding:0"/>
          <a href="#" class="submit" onclick="$('#questionForm').submit()"><img src="<%=m_context%>/images/bt-login.png" alt=""/></a>
        </p>
      </div>
    </div>
    <input type="hidden" name="Login" value="<%=userDetail.getLogin()%>"/>
    <input type="hidden" name="DomainId" value="<%=userDetail.getDomainId()%>"/>
  </div>
</form>
</body>

</html>