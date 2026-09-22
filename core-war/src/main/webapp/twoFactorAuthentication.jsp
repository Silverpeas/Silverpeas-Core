<%--
  ~ Copyright (C) 2000 - 2026 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<fmt:setLocale value="${pageContext.request.locale.language}"/>
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication"/>
<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
  <link rel="icon" href="<c:url value="/favicon.ico"/>" />
  <meta name="viewport" content="initial-scale=1.0"/>
</view:sp-head-part>
<view:sp-body-part>
  <form id="formTwoFactor" action="<c:url value="/AuthenticationServlet"/>" method="post"
        accept-charset="UTF-8">
    <input type="hidden" name="Login" value="<c:out value="${sessionScope.Silverpeas_TwoFactor_Login}"/>"/>
    <input type="hidden" name="DomainId" value="<c:out value="${sessionScope.Silverpeas_TwoFactor_Domain}"/>"/>
    <div class="page">
      <div class="titre"><fmt:message key="authentication.logon.title"/></div>
      <div id="background">
        <div class="cadre">
          <div id="header" style="display: table; width: 100%">
            <div class="information" style="display: table-cell; width: 100%; text-align: right">
              <fmt:message key="authentication.logon.twoFactor.title"/>
            </div>
            <div class="clear"></div>
          </div>
          <p><fmt:message key="authentication.logon.twoFactor.instructions"/></p>
          <c:if test="${requestScope.twoFactorError}">
            <p class="error"><fmt:message key="authentication.logon.twoFactor.invalidCode"/></p>
          </c:if>
          <p id="totpMode">
            <label>
              <span><fmt:message key="authentication.logon.twoFactor.code"/></span>
              <input type="text" name="TwoFactorCode" id="TwoFactorCode"
                     inputmode="numeric" autocomplete="one-time-code"
                     maxlength="6" pattern="[0-9]{6}" autofocus/>
            </label>
          </p>
          <p id="recoveryMode" style="display:none">
            <label>
              <span><fmt:message key="authentication.logon.twoFactor.recoveryCode"/></span>
              <input type="text" id="RecoveryCode"
                     inputmode="text" autocomplete="off"
                     maxlength="12" pattern="[A-Za-z0-9-]{10,12}"/>
            </label>
          </p>
          <p>
            <a href="#" id="useRecoveryCode"><fmt:message key="authentication.logon.twoFactor.useRecoveryCode"/></a>
            <a href="#" id="useTotpCode" style="display:none"><fmt:message key="authentication.logon.twoFactor.useTotpCode"/></a>
          </p>
          <p>
            <input type="submit" value="<fmt:message key="authentication.logon.twoFactor.submit"/>"/>
          </p>
        </div>
      </div>
    </div>
  </form>
</view:sp-body-part>
</view:sp-page>
