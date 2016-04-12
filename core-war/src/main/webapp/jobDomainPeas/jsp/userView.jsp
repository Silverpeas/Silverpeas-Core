<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %><%--

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="profile"/>

<c:set var="userInfos" value="${requestScope.UserFull}" />

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

<%
    Board board = gef.getBoard();

	Domain 		domObject 		= (Domain)request.getAttribute("domainObject");
    UserFull	userObject 		= (UserFull)request.getAttribute("UserFull");

  if (domObject != null) {
    browseBar.setComponentName(getDomainLabel(domObject, resource));
  }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true"/>
<view:includePlugin name="popup"/>
<title><%=userObject.getDisplayedName()%></title>
<script type="text/javascript">
  function ConfirmAndSend(textToDisplay, targetURL) {
    jQuery.popup.confirm(textToDisplay, function() {
      window.location.href = targetURL;
    });
  }

function resizeMe() {
	<%
		int height = 400;
		if (userObject!=null)
		{
			int nbProperty = userObject.getPropertiesNames().length;
			height = height+11*nbProperty;
		}
	%>
    window.resizeTo(550,<%=height%>);
}
</script>
</head>
<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onLoad="resizeMe();" id="profil">
<%
out.println(window.printBefore());
%>
<view:frame>
<%
out.println(board.printBefore());
%>
	<fieldset class="skinFieldset" id="identity-main">
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
			<div class="champs">${displayedEmail}</div>
		</li>
			<!--Login-->
			<li id="form-row-login" class="field">
			<label class="txtlibform"><fmt:message key="GML.login"/></label>
			<div class="champs">${displayedLogin}</div>
		</li>
		</ul>
	</fieldset>

    <fieldset class="skinFieldset" id="identity-extra">
		<legend class="without-img"><fmt:message key="myProfile.identity.fieldset.extra" bundle="${profile}"/></legend>
		<ul class="fields">
		  <%
		    String[] properties = userObject.getPropertiesNames();
		    for (final String property : properties) {
				// Not display the password !
			if (!property.startsWith("password")) {
		  %>
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
<%
out.println(board.printAfter());
ButtonPane bouton = gef.getButtonPane();
bouton.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false));
out.print("<BR/>");
out.print(bouton.print());
%>
<br/>
</view:frame>
<%
out.print(window.printAfter());
%>
</body>
</html>