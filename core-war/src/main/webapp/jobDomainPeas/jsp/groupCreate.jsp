<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %><%--

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="profile"/>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<c:set var="grObject" value="${requestScope.groupObject}" />
<c:set var="groupId" value="${grObject.id}" />
<c:set var="superGroupId" value="${grObject.superGroupId}" />
<c:set var="groupName" value="${grObject.name}" />
<c:set var="displayedGroupName"><view:encodeHtml string="${groupName}" /></c:set>
<c:set var="groupDesc" value="${grObject.description}" />
<c:set var="displayedGroupDesc"><view:encodeHtml string="${groupDesc}" /></c:set>
<c:set var="groupRule" value="${grObject.rule}" />
<c:set var="displayedGroupRule"><view:encodeHtml string="${groupRule}" /></c:set>

<%
	Domain domObject = (Domain)request.getAttribute("domainObject");
    Group  grObject = (Group)request.getAttribute("groupObject");
    String action =(String)request.getAttribute("action");
    String groupsPath = (String)request.getAttribute("groupsPath");

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(groupsPath);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="qtip"/>
<script type="text/javascript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.groupForm.groupName.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("GML.name")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg == "")
    {
        document.groupForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}

$(document).ready(function()
{
   $('#rule-info').qtip({
	content: {
		text: "<%=EncodeHelper.javaStringToJsString(resource.getString("JDP.synchroRuleInfo"))%>",
		title: {
			text: "<%=resource.getString("JDP.synchroRuleAvail")%>",
			button: "<%=resource.getString("GML.close")%>"
		}
	},
	hide: {
		event: false
	},
	style: {
		tip: true,
		classes: "qtip-shadow qtip-green"
	},
	position: {
		adjust: {
			method: "flip flip"
		},
		at: "top right",
		my: "bottom left",
		viewport: $(window)
	}
       });
});
</script>
</head>
<body>
<%
out.println(window.printBefore());
%>
<view:frame>
<form name="groupForm" action="<%=action%>" method="POST">
	<c:choose>
		<c:when test="${not empty groupId}">
			<c:set var="grId" value="${groupId}" />
		</c:when>
		<c:otherwise>
			<c:set var="grId" value="" />
		</c:otherwise>
	</c:choose>
	<c:choose>
		<c:when test="${not empty superGroupId}">
			<c:set var="superGrId" value="${superGroupId}" />
		</c:when>
		<c:otherwise>
			<c:set var="superGrId" value="" />
		</c:otherwise>
	</c:choose>
    <input type="hidden" name="Idgroup" value="${grId}">
    <input type="hidden" name="Idparent" value="${superGrId}">

    <fmt:message key="JDP.mandatory" var="mandatoryIcon" bundle="${icons}" />

    <fieldset id="profil-groups-belong" class="skinFieldset">
		<legend><fmt:message key="myProfile.identity.fieldset.main" bundle="${profile}" /></legend>
		<div class="fields">
			<!--Group name-->
		<div class="field" id="form-row-name">
				<label class="txtlibform"><fmt:message key="GML.name"/></label>
				<div class="champs">
					<input type="text" name="groupName" size="50" maxlength="99"
						VALUE="${displayedGroupName}">
						&nbsp;<img border="0" src="${context}${mandatoryIcon}" width="5" height="5">
				</div>
			</div>
		<!--Group name-->
		<div class="field" id="form-row-desc">
				<label class="txtlibform"><fmt:message key="GML.description"/></label>
				<div class="champs">
					<input type="text" name="groupDescription" size="50" maxlength="399"
						VALUE="${displayedGroupDesc}">
				</div>
			</div>
		<!--Synchro rule-->
		<fmt:message key="JDP.info" var="infoIcon" bundle="${icons}" />
		<div class="field" id="form-row-rule">
				<label class="txtlibform"><fmt:message key="JDP.synchroRule"/></label>
				<div class="champs">
					<input type="text" name="groupRule" size="50" maxlength="100" VALUE="${displayedGroupRule}">
					<img border="0" align="absmiddle" src="${context}${infoIcon}" id="rule-info"/>
				</div>
			</div>
		</div>
    </fieldset>

    <div class="legend">
	<img border="0" src="${context}${mandatoryIcon}" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
    </div>

</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "groupContent", false));
		  out.println(bouton.print());
		%>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>