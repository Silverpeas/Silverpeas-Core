<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.silverpeas.core.util.SilverpeasList" %>
<%@ page import="org.silverpeas.web.jobdomain.servlets.UserUIEntity" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="domainsLabel" key="JDP.domains"/>

<fmt:message var="verifyLabel" key="GML.verify"/>
<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="backLabel" key="GML.back"/>
<fmt:message var="domainsLabel" key="JDP.domains"/>
<fmt:message var="filterRuleLabel" key="JDP.filterRule"/>
<fmt:message var="domainUserFilterRuleModify" key="JDP.domainUserFilterRuleModify"/>
<fmt:message var="usersLabel" key="GML.users"/>
<fmt:message var="firstName" key="GML.surname"/>
<fmt:message var="lastName" key="GML.lastName"/>
<fmt:message var="login" key="GML.login"/>

<c:set var="domain" value="${requestScope.domainObject}"/>
<jsp:useBean id="domain" type="org.silverpeas.core.admin.domain.model.Domain"/>
<c:set var="back" value="domainContent?Iddomain=${domain.id}"/>
<c:set var="domainUserFilterManager" value="${requestScope.domainUserFilterManager}"/>
<jsp:useBean id="domainUserFilterManager" type="org.silverpeas.core.admin.domain.DomainDriver.UserFilterManager"/>
<c:set var="domainUserFilterRule" value="${domainUserFilterManager.rule}"/>
<c:set var="technicalError" value="${requestScope.technicalError}"/>

<%@ include file="check.jsp" %>

<c:set var="domainType" value="${isGoogleDomain(domain) ? 'domainGoogle' : 'unknown'}"/>

<c:set var="prefixedNotationHelp"><view:applyTemplate locationBase="core:expression" name="prefixedNotationHelp"/></c:set>
<c:set var="domainUserFilterRuleInfo">
  <view:applyTemplate locationBase="core:admin/domain" name="domainUserFilterRuleInfo">
    <view:templateParam name="${domainType}" value="true"/>
    <view:templateParam name="prefixedNotationHelp" value="${silfn:escapeJs(prefixedNotationHelp)}"/>
  </view:applyTemplate>
</c:set>
<c:set var="successMessage">
  <view:applyTemplate locationBase="core:admin/domain" name="domainUserFilterRuleSaveSuccess">
    <view:templateParam name="rule" value="###"/>
    <view:templateParam name="propertyFile" value="${domain.propFileName}"/>
    <view:templateParam name="key" value="${domainUserFilterManager.ruleKey}"/>
  </view:applyTemplate>
</c:set>

<%
  final SilverpeasList<UserUIEntity> users = ArrayPane
      .computeDataUserSessionIfAbsent(request, "domainModifyUserFilter_cacheKey_users",
          () -> (SilverpeasList) request.getAttribute("users"));
%>
<c:set var="users" value="<%=users%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title></title>
  <view:looknfeel withFieldsetStyle="true"/>
  <style type="text/css">
    .infoBulle {
      padding-left: 5px;
      position: absolute;
      display: inline-block;
    }
  </style>
  <script language="JavaScript" type="text/javascript">
    var arrayPaneAjaxControl;
    function verify() {
      performAction('verify');
    }
    function validate() {
      performAction('validate').then(function(value) {
        setTimeout(function() {
          handleValidateResponse(value);
        }, 0);
      });
    }
    function performAction(action) {
      spProgressMessage.show();
      var value = sp.element.querySelector('#domainUserFilterRule').value;
      return sp.ajaxRequest('domainModifyUserFilter')
          .withParam('domainUserFilterRule', encodeURIComponent(value))
          .withParam('action', action)
          .send()
          .then(function(request) {
            spProgressMessage.hide();
            arrayPaneAjaxControl.refreshFromRequestResponse(request);
            return value;
          });
    }

    whenSilverpeasReady(function() {
      TipManager.simpleRule('#rule-info', '${filterRuleLabel}', '${silfn:escapeJs(domainUserFilterRuleInfo)}');
    });
  </script>
</head>
<body class="page_content_admin">
<view:browseBar componentId="${domainsLabel}">
  <view:browseBarElt link="${back}" label="<%=getDomainLabel(domain, resource)%>"/>
  <view:browseBarElt link="" label="${domainUserFilterRuleModify}"/>
</view:browseBar>
<view:window>
  <view:frame>
    <div class="inlineMessage"><view:applyTemplate locationBase="core:admin/domain" name="domainUserFilterRuleHelp"/></div>
    <view:board>
      <div class="fields oneFieldPerLine">
        <div class="field">
          <label class="txtlibform">${filterRuleLabel} :</label>
          <div class="champs">
            <input type="text" id="domainUserFilterRule" name="domainUserFilterRule"
                   size="70" value="${domainUserFilterRule}">
            <img id="rule-info" class="infoBulle" src="<c:url value="/util/icons/info.gif"/>" alt="info"/>
          </div>
        </div>
      </div>
    </view:board>
    <div id="dynamic-content">
      <c:if test="${users != null}">
        <c:set var="title" value="${usersLabel} (${fn:length(users)})"/>
        <view:arrayPane var="listOfFilteredUsers" routingAddress="domainModifyUserFilter" numberLinesPerPage="25" title="${title}">
          <view:arrayColumn title="${lastName}" compareOn="${u -> fn:toLowerCase(u.data.lastName)}"/>
          <view:arrayColumn title="${firstName}" compareOn="${u -> fn:toLowerCase(u.data.firstName)}"/>
          <view:arrayColumn title="${login}" compareOn="${u -> fn:toLowerCase(u.data.login)}"/>
          <view:arrayLines var="aUser" items="${users}">
            <view:arrayLine>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.lastName)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.firstName)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.login)}"/>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>
      </c:if>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-content');
          SilverpeasError.add("${silfn:escapeJs(technicalError)}").show();
        });
        function handleValidateResponse(value) {
          <c:if test="${empty technicalError}">
          var message = '${silfn:escapeJs(successMessage)}'.replace('###', value);
          jQuery.popup.info(message, function() {
            sp.navRequest('${back}').go();
          });
          </c:if>
        }
      </script>
    </div>
    <p>
      <view:buttonPane>
        <view:button label="${verifyLabel}" action="javascript:verify()"/>
        <view:button label="${validateLabel}" action="javascript:validate()"/>
        <view:button label="${backLabel}" action="${back}"/>
      </view:buttonPane>
    </p>
  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>