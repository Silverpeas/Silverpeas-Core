<%--
    Copyright (C) 2000 - 2022 Silverpeas
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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../../headLog.jsp" %>

<fmt:setLocale value="<%=userLanguage%>" />
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication" />
<view:sp-page>
<view:sp-head-part noLookAndFeel="true">
<link rel="icon" href="<%=favicon%>" />
<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />

</view:sp-head-part>
<view:sp-body-part>
<div id="top"></div>
<div class="page">
  <div class="titre"><fmt:message key="registration.title"/></div>
    <div id="background">
        <div class="cadre">
			
			<div class="registrationSuccessText success">
				<p><fmt:message key="registration.success"/></p>
			</div>

            <a href="<c:url value="/Login"/>" class="submit"><span><span><fmt:message key="registration.connect"/></span></span></a>
        </div>
    </div>
    <div id="copyright"><fmt:message key="GML.trademark" /></div>
</div>
</view:sp-body-part>
</view:sp-page>
