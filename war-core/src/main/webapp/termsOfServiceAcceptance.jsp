<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="templateLocationBase" value="core:termsOfService"/>
<c:set var="templateContent" value="termsOfService${requestScope.templateDomainIdContent}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="headLog.jsp" %>

<fmt:setLocale value="${requestScope.language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication" var="authenticationBundle"/>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="GML.refuse"/></title>
    <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
    <view:includePlugin name="jquery"/>
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
      $(document).ready(function() {
        $('#termsOfServiceAcceptance').on("submit", function() {
          this.action = '<c:url value="/CredentialsServlet/TermsOfServiceResponse"/>';
          return true;
        });
      });
      function submit() {
        $('#termsOfServiceAcceptance').submit();
      }
      function userAccept() {
        $('#tosAccepted').val('true');
        submit();
      }
    </script>
  </head>

  <body>
    <form id="termsOfServiceAcceptance" action="#" method="post">
      <div id="top"></div>
      <div class="page">
        <div id="backgroundBig">
          <div class="cadre">
            <div id="header">
              <img src="<%=logo%>" class="logo" alt=""/>

              <div id="content-terms-of-service">
                <view:applyTemplate locationBase="${templateLocationBase}" name="${templateContent}"/>
              </div>
              <div class="clear"></div>
            </div>

            <p>
              <a href="#" class="submit refused" onclick="submit()"><span><span><fmt:message bundle="${authenticationBundle}" key="GML.refuse"/></span></span></a>
              <a href="#" class="submit validate" onclick="userAccept()"><span><span><fmt:message bundle="${authenticationBundle}" key="GML.accept"/></span></span></a>
            </p>
          </div>
        </div>
        <input type="hidden" name="tosToken" value="${requestScope.tosToken}"/>
        <input type="hidden" id="tosAccepted" name="tosAccepted" value="false"/>
      </div>
    </form>
  </body>
</html>