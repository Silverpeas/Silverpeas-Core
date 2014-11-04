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

<%@page import="org.silverpeas.web.token.SynchronizerTokenServiceFactory"%>
<%@page import="org.silverpeas.web.token.SynchronizerTokenService"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="profile"/>

<c:set var="userInfos" value="${requestScope.userObject}" />

<c:set var="lastName" value="${userInfos.lastName}" />
<c:set var="displayedLastName"><view:encodeHtml string="${lastName}" /></c:set>
<c:set var="firstName" value="${userInfos.firstName}" />
<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
<c:set var="firstName" value="${userInfos.firstName}" />
<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
<c:set var="email" value="${userInfos.eMail}" />
<c:set var="displayedEmail"><view:encodeHtml string="${email}" /></c:set>
<c:set var="login" value="${userInfos.login}" />
<c:set var="displayedLogin"><view:encodeHtml string="${login}" /></c:set>

<%@ include file="check.jsp" %>
<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  UserFull userObject = (UserFull) request.getAttribute("userObject");
  String groupsPath = (String) request.getAttribute("groupsPath");
  boolean isDomainRW = (Boolean) request.getAttribute("isDomainRW");
  boolean isDomainSync = (Boolean) request.getAttribute("isDomainSync");
  boolean isUserRW = (Boolean) request.getAttribute("isUserRW");
  boolean isX509Enabled = (Boolean) request.getAttribute("isX509Enabled");
  boolean isGroupManager = (Boolean) request.getAttribute("isOnlyGroupManager");
  boolean isUserManageableByGroupManager =
      (Boolean) request.getAttribute("userManageableByGroupManager");

  String thisUserId = userObject.getId();

  if (domObject != null) {
    browseBar.setComponentName(getDomainLabel(domObject, resource),
        "domainContent?Iddomain=" + domObject.getId());
  }

  if (groupsPath != null && groupsPath.length() > 0) {
    browseBar.setPath(groupsPath);
  }

  if (isDomainRW && isUserRW && (!isGroupManager || isUserManageableByGroupManager)) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserModify?Iduser=" + thisUserId);
    if (userObject.isBlockedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userUnblock"), resource.getString("JDP.userUnblock"),
              "userUnblock?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userBlock"), resource.getString("JDP.userBlock"),
              "userBlock?Iduser=" + thisUserId);
    }
    operationPane.addOperation(resource.getIcon("JDP.userDel"), resource.getString("GML.delete"),
        "javascript:ConfirmAndSend('" + resource.getString("JDP.userDelConfirm") +
            "','" + thisUserId+ "')");
  }
  if (isDomainSync && !isGroupManager) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserMS?Iduser=" + thisUserId);
    if (userObject.isBlockedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userUnblock"), resource.getString("JDP.userUnblock"),
              "userUnblock?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userBlock"), resource.getString("JDP.userBlock"),
              "userBlock?Iduser=" + thisUserId);
    }
    operationPane
        .addOperation(resource.getIcon("JDP.userSynchro"), resource.getString("JDP.userSynchro"),
            "userSynchro?Iduser=" + thisUserId);
    operationPane.addOperation(resource.getIcon("JDP.userUnsynchro"),
        resource.getString("JDP.userUnsynchro"), "userUnSynchro?Iduser=" + thisUserId);
  }
  if (isX509Enabled && !isGroupManager) {
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("JDP.x509"), resource.getString("JDP.getX509"),
        "userGetP12?Iduser=" + thisUserId);
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
  <script type="text/javascript">
    function ConfirmAndSend(textToDisplay, userId) {
      if (window.confirm(textToDisplay)) {
        jQuery('#Iduser').val(userId);
        jQuery('#deletionForm').submit();
      }
    }
  </script>
</head>
<body>
<% 
out.println(window.printBefore());
%>
<view:frame>
<fieldset id="identity-main" class="skinFieldset">
  <legend><fmt:message key="myProfile.identity.fieldset.main" bundle="${profile}" /></legend>
  <ul class="fields">
	<!--Last name-->
	<li id="form-row-lastname" class="field">
    	<label class="txtlibform"><fmt:message key="GML.lastName"/></label>
     	<div class="champs">${displayedLastName}</div>
	</li>
	<!--Surname-->
	<li id="form-row-surname" class="field">
		<label class="txtlibform"><fmt:message key="GML.surname"/></label>
		<div class="champs">${displayedFirstName}</div>
	</li>
	<!---Email-->
	<li id="form-row-email" class="field">
		<label class="txtlibform"><fmt:message key="GML.eMail"/></label>
		<div class="champs"><a href="mailto:${displayedEmail}">${displayedEmail}</a></div>
	</li>
	<!---Rights-->
	<li id="form-row-rights" class="field">
		<label class="txtlibform"><fmt:message key="JDP.userRights"/></label>
		<div class="champs">
			<c:choose>
				<c:when test="${userInfos.accessLevel.code == 'A'}">
					<fmt:message key="GML.administrateur"/>
				</c:when>
				<c:when test="${userInfos.accessLevel.code == 'G'}">
					<fmt:message key="GML.guest"/>
				</c:when>
				<c:when test="${userInfos.accessLevel.code == 'K'}">
					<fmt:message key="GML.kmmanager"/>
				</c:when>
				<c:when test="${userInfos.accessLevel.code == 'D'}">
					<fmt:message key="GML.domainManager"/>
				</c:when>
				<c:when test="${userInfos.accessLevel.code == 'U'}">
					<fmt:message key="GML.user"/>
				</c:when>
				<c:otherwise>
					<fmt:message key="GML.no"/>
				</c:otherwise>
			</c:choose>
		</div>
   		</li>
   		<!---State-->
		<li id="form-row-rights" class="field">
     		<label class="txtlibform"><fmt:message key="JDP.userState"/></label>
     		<div class="champs"><fmt:message key="GML.user.account.state.${userInfos.state.name}"/></div>
   		</li>
		<!--Login-->
		<li id="form-row-login" class="field">
     		<label class="txtlibform"><fmt:message key="GML.login"/></label>
     		<div class="champs">${displayedLogin}</div>
   		</li>
   		<!--Password Silverpeas ?-->
		<li id="form-row-passwordsp" class="field">
			<label class="txtlibform"><fmt:message key="JDP.silverPassword"/></label>
     		<div class="champs">
     			<c:choose>
      				<c:when test="${userInfos.passwordAvailable && userInfos.passwordValid}">
      					<fmt:message key="GML.yes"/>
      				</c:when>
      				<c:otherwise>
      					<fmt:message key="GML.no"/>
      				</c:otherwise>
      			</c:choose>
     		</div>
   		</li>
	</ul>
</fieldset>
<fieldset id="identity-extra" class="skinFieldset">
	<legend class="without-img"><fmt:message key="myProfile.identity.fieldset.extra" bundle="${profile}"/></legend>
	<ul class="fields">
      <%
        String[] properties = userObject.getPropertiesNames();
        for (final String property : properties) {
       	  // Not display the password !
          if (!property.startsWith("password")) {
      %>
      	<!--Specific Info-->
		<li id="form-row-<%=property%>" class="field">
			<label class="txtlibform">
			<%=EncodeHelper.javaStringToHtmlString(userObject.
	        		    getSpecificLabel(resource.getLanguage(),
	        		        property))%>
		 	</label>
			<div class="champs">
				<%
	            if ("STRING".equals(userObject.getPropertyType(property)) ||
	                "USERID".equals(userObject.getPropertyType(property))) {
				%>
				<%=EncodeHelper.javaStringToHtmlString(userObject.getValue(property))%>
				<%
	            } else if ("BOOLEAN".equals(userObject.getPropertyType(property))) {
	
	              if (userObject.getValue(property) != null &&
	                  "1".equals(userObject.getValue(property))) {
	            %>
	            	<fmt:message key="GML.yes"/>
	            <%
	              } else if (userObject.getValue(property) == null ||
	                  "".equals(userObject.getValue(property)) ||
	                  "0".equals(userObject.getValue(property))) {
	            %>
	            	<fmt:message key="GML.no"/>
	           <%
	              }
	            }
	           %>
			</div>
        </li>
      <%
          }
        }
      %>
	</ul>
</fieldset>
  <form id="deletionForm" action="userDelete" method="POST">
    <input id="Iduser" type="hidden" name="Iduser"/>
  </form>
</view:frame>
<% 
out.println(window.printAfter());
%>
</body>
</html>