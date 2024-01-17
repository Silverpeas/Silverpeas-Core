<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="mandatoryIconPath" key="JDP.mandatory" bundle="${icons}"/>
<c:url var="mandatoryIconUrl" value="${mandatoryIconPath}"/>
<fmt:message var="requiredMessage" key="GML.requiredField"/>
<fmt:message var="domainsLabel" key="JDP.domains"/>

<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<fmt:message var="domainsLabel" key="JDP.domains"/>
<fmt:message var="addDomainLDAPLabel" key="JDP.domainAdd"/>
<fmt:message var="addDomainSCIMLabel" key="JDP.domainSCIMAdd"/>
<fmt:message var="addDomainGoogleLabel" key="JDP.domainGoogleAdd"/>
<fmt:message var="addDomainSQLLabel" key="JDP.domainSQLAdd"/>
<fmt:message var="modifyDomainLabel" key="JDP.domainUpdate"/>

<fmt:message var="nameLabel" key="JDP.name"/>
<fmt:message var="descriptionLabel" key="GML.description"/>
<fmt:message var="classLabel" key="JDP.class"/>
<fmt:message var="propertiesLabel" key="JDP.properties"/>
<fmt:message var="serverAuthenticationLabel" key="JDP.serverAuthentification"/>
<fmt:message var="silverpeasServerURLLabel" key="JDP.silverpeasServerURL"/>
<fmt:message var="userDomainQuotaMaxCountLabel" key="JDP.userDomainQuotaMaxCount"/>
<fmt:message var="userDomainQuotaMaxCountHelpLabel" key="JDP.userDomainQuotaMaxCountHelp"/>

<fmt:message var="missingFieldStartLabel" key="JDP.missingFieldStart"/>
<fmt:message var="missingFieldEndLabel" key="JDP.missingFieldEnd"/>

<c:set var="action" value="${requestScope.action}"/>
<c:set var="createMode" value="${fn:endsWith(action, 'Create')}"/>
<c:set var="usersInDomainQuotaActivated" value="<%=JobDomainSettings.usersInDomainQuotaActivated%>"/>
<c:set var="domain" value="${requestScope.domainObject}"/>
<jsp:useBean id="domain" type="org.silverpeas.core.admin.domain.model.Domain"/>

<c:set var="creationDomainType" value="${
    (action eq 'domainCreate' ? 'domainLDAP' :
    (action eq 'domainSCIMCreate' ? 'domainSCIM' :
    (action eq 'domainGoogleCreate' ? 'domainGoogle' : 'domainSQL')))}"/>
<c:set var="getCreatePathLabel" value="${
    t -> (t eq 'domainLDAP' ? addDomainLDAPLabel :
         (t eq 'domainSCIM' ? addDomainSCIMLabel :
         (t eq 'domainGoogle' ? addDomainGoogleLabel : addDomainSQLLabel)))}"/>

<c:set var="formatMissingFieldValueMessage" value="${f -> missingFieldStartLabel.concat(f).concat(missingFieldEndLabel)}"/>

<%@ include file="check.jsp" %>

<c:set var="technicalDataStyleAttr" value="${d -> (isSCIMDomain(d) or isGoogleDomain(d)) ? 'style=\"display:none;\"' : ''}"/>
<c:set var="domainPropertyDataStyleAttr" value="${d -> isSCIMDomain(d) ? 'style=\"display:none;\"' : ''}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <view:looknfeel withCheckFormScript="true"/>
  <script language="JavaScript" type="text/javascript">
    function SubmitWithVerif(verifParams) {
      var namefld = stripInitialWhitespace(document.domainForm.domainName.value);
      var driverfld = stripInitialWhitespace(document.domainForm.domainDriver.value);
      var propsfld = stripInitialWhitespace(document.domainForm.domainProperties.value);
      var authfld = stripInitialWhitespace(document.domainForm.domainAuthentication.value);
      var urlfld = stripInitialWhitespace(document.domainForm.silverpeasServerURL.value);
      var errorMsg = "";

      if (verifParams) {
        if (isWhitespace(namefld)) errorMsg = "${formatMissingFieldValueMessage(nameLabel)}";
        if (isWhitespace(driverfld)) errorMsg = "${formatMissingFieldValueMessage(classLabel)}";
        if (isWhitespace(propsfld)) errorMsg = "${formatMissingFieldValueMessage(propertiesLabel)}";
        if (isWhitespace(authfld)) errorMsg =
            "${formatMissingFieldValueMessage(serverAuthenticationLabel)}";
        if (isWhitespace(urlfld)) errorMsg =
            "${formatMissingFieldValueMessage(silverpeasServerURLLabel)}";
      }
      if (errorMsg === "") {
        document.domainForm.submit();
      } else {
        jQuery.popup.error(errorMsg);
      }
    }
  </script>
</head>
<body class="page_content_admin">
<view:browseBar componentId="${domainsLabel}">
  <c:choose>
    <c:when test="${createMode}">
      <view:browseBarElt link="" label="${getCreatePathLabel(creationDomainType)}..."/>
    </c:when>
    <c:otherwise>
      <view:browseBarElt link="domainContent?Iddomain=${domain.id}" label="<%=getDomainLabel(domain, resource)%>"/>
      <view:browseBarElt link="" label="${modifyDomainLabel}..."/>
    </c:otherwise>
  </c:choose>
</view:browseBar>
<view:window>
  <view:frame>
    <view:board>
      <c:set var="domainHelp">
        <view:applyTemplate locationBase="core:admin/domain" name="help">
          <view:templateParam name="${creationDomainType}" value="true"/>
        </view:applyTemplate>
      </c:set>
      <c:if test="${createMode and not empty fn:trim(domainHelp)}">
        <div class="inlineMessage">${domainHelp}</div>
      </c:if>
      <form name="domainForm" action="${action}" method="POST">
      <table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
        <tr>
          <td class="txtlibform">${nameLabel} :</td>
          <td>
            <input type="text" name="domainName" size="70" maxlength="99" VALUE="${silfn:escapeHtml(domain.name)}">&nbsp;<img border="0" src="${mandatoryIconUrl}" width="5" height="5">
          </td>
        </tr>
        <tr>
          <td class="txtlibform">${descriptionLabel} :</td>
          <td>
            <input type="text" name="domainDescription" size="70" maxlength="399" VALUE="${silfn:escapeHtml(domain.description)}">
          </td>
        </tr>
        <tr ${technicalDataStyleAttr(domain)}>
          <td class="txtlibform">${classLabel} :</td>
          <td>
            <input type="text" name="domainDriver" size="70" maxlength="99" VALUE="${silfn:escapeHtml(domain.driverClassName)}">&nbsp;<img border="0" src="${mandatoryIconUrl}" width="5" height="5">
          </td>
        </tr>
        <tr ${domainPropertyDataStyleAttr(domain)}>
          <td class="txtlibform">${propertiesLabel} :</td>
          <td>
            <input type="text" name="domainProperties" size="70" maxlength="99" VALUE="${silfn:escapeHtml(domain.propFileName)}">&nbsp;<img border="0" src="${mandatoryIconUrl}" width="5" height="5">
          </td>
        </tr>
        <tr ${technicalDataStyleAttr(domain)}>
          <td class="txtlibform">${serverAuthenticationLabel} :</td>
          <td>
            <input type="text" name="domainAuthentication" size="70" maxlength="99" VALUE="${silfn:escapeHtml(domain.authenticationServer)}">&nbsp;<img border="0" src="${mandatoryIconUrl}" width="5" height="5">
          </td>
        </tr>
        <tr>
          <td class="txtlibform">${silverpeasServerURLLabel} :</td>
          <td>
            <input type="text" name="silverpeasServerURL" size="70" maxlength="399" VALUE="${silfn:escapeHtml(domain.silverpeasServerURL)}">&nbsp;<img border="0" src="${mandatoryIconUrl}" width="5" height="5">
          </td>
        </tr>
        <c:if test="${usersInDomainQuotaActivated and domain.id eq '0'}">
          <tr>
            <td class="txtlibform">${userDomainQuotaMaxCountLabel} :</td>
            <td>
              <input type="text" name="userDomainQuotaMaxCount" size="40" maxlength="399" value="${domain.userDomainQuota.maxCount}"/>&nbsp;<img src="${mandatoryIconUrl}" width="5" height="5"/> ${userDomainQuotaMaxCountHelpLabel}
            </td>
          </tr>
        </c:if>
        <tr>
          <td colspan="2">
            (<img border="0" src="${mandatoryIconUrl}" width="5" height="5"> : ${requiredMessage})
          </td>
        </tr>
      </table>
    </view:board>
    </form>
    <view:buttonPane>
      <view:button label="${validateLabel}" action="javascript:SubmitWithVerif(true)"/>
      <view:button label="${cancelLabel}" action="domainContent"/>
    </view:buttonPane>
    <br/>
  </view:frame>
</view:window>

</body>
</html>