<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ tag import="org.silverpeas.core.admin.user.model.UserFull" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<c:url var="iconUser" value="/util/icons/user.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>

<fmt:message key="GML.delete" bundle="${generalBundle}" var="labelDelete"/>
<fmt:message key="GML.user" bundle="${generalBundle}" var="labelUser"/>
<fmt:message key="GML.eMail" bundle="${generalBundle}" var="labelEmail"/>

<%-- Creator --%>
<%@ attribute name="user" required="true" type="org.silverpeas.core.admin.user.model.User"
              description="The user to display" %>

<%@ attribute name="allFieldsUpdatable" required="false" type="java.lang.Boolean"
              description="If true all fields are updatable" %>

<%@ attribute name="readOnly" required="true" type="java.lang.Boolean"
              description="True if all fields must be readonly" %>

<%@ attribute name="includeEmail" required="true" type="java.lang.Boolean"
              description="True if email must be displayed" %>

<%@ attribute name="linear" required="false" type="java.lang.Boolean"
              description="True if fields must be displayed one per line" %>

<%@ attribute name="displayLabels" required="false" type="java.lang.Boolean"
              description="True if field labels must be displayed" %>

<c:set var="isCurrentUserAdmin" value="<%=UserDetail.getCurrentRequester().isAccessAdmin() %>"/>
<c:set var="isCurrentUserDomainManager" value="<%=UserDetail.getCurrentRequester().isAccessDomainManager() %>"/>
<c:set var="isAdminScope" value="${requestScope.ADMIN_SCOPE}"/>

<view:setConstant var="propertyTypeUser" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_USERID"/>
<view:setConstant var="propertyTypeString" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_STRING"/>
<view:setConstant var="propertyTypeBoolean" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_BOOLEAN"/>

<fmt:message key="JDP.potentialSensitiveData" var="potentialSensitiveData"/>
<fmt:message key="JDP.effectivelySensitiveData" var="sensitiveData"/>

<c:set var="passwordPrefix" value="password"/>

<c:if test="<%=!(user instanceof UserFull)%>">
  <c:set var="user" value="<%=UserFull.getById(user.getId())%>"/>
</c:if>

<c:set var="sensitiveInfo" value="${potentialSensitiveData}"/>
<c:set var="sensitiveCssClass" value="sensitive_no_active"/>
<c:set var="sensitivePicto" value="/util/icons/bulle-attention.png"/>
<c:if test="${user.hasSensitiveData()}">
  <c:set var="sensitiveInfo" value="${sensitiveData}"/>
  <c:set var="sensitiveCssClass" value="sensitive_active"/>
  <c:set var="sensitivePicto" value="/util/icons/info-sensible.png"/>
</c:if>

<c:if test="${allFieldsUpdatable == null}">
  <c:set var="allFieldsUpdatable" value="${false}"/>
</c:if>

<c:if test="${displayLabels == null}">
  <c:set var="displayLabels" value="${true}"/>
</c:if>

<c:set var="listStyleTwoFieldsPerLine" value="fields"/>
<c:set var="listStyleOneFieldPerLine" value="oneFieldPerLine"/>

<c:set var="listStyle" value="${readOnly ? listStyleTwoFieldsPerLine : listStyleOneFieldPerLine}"/>
<c:if test="${linear}">
  <c:set var="listStyle" value="${listStyleOneFieldPerLine}"/>
</c:if>

<div class="${listStyle}">
  <c:if test="${silfn:isDefined(user.emailAddress) and includeEmail}">
    <c:choose>
      <c:when test="${isAdminScope and user.hasSensitiveData()}">
        <div class="field sensitive_active" id="email">
          <c:if test="${displayLabels}">
            <label class="txtlibform">${labelEmail}</label>
          </c:if>
          <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
          <div class="champs">${user.emailAddress}</div>
        </div>
      </c:when>
      <c:when test="${isAdminScope}">
        <div class="field sensitive_no_active" id="email">
          <c:if test="${displayLabels}">
            <label class="txtlibform">
                ${labelEmail}
                <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
            </label>
          </c:if>
          <c:if test="${not displayLabels}">
          <label class="txtlibform">
              <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
          </label>
          </c:if>
          <div class="champs">${user.emailAddress}</div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="field" id="email">
          <c:if test="${displayLabels}">
            <label class="txtlibform">${labelEmail}</label>
          </c:if>
          <div class="champs">${user.emailAddress}</div>
        </div>
      </c:otherwise>
    </c:choose>
  </c:if>
  <c:forEach items="${user.propertiesNames}" var="propertyName">
    <c:set var="passwordField" value="${fn:startsWith(propertyName, passwordPrefix)}"/>
    <c:if test="${not passwordField}">
      <c:set var="domainProperty" value="${user.getProperty(propertyName)}"/>
      <jsp:useBean id="domainProperty"
                   type="org.silverpeas.core.admin.domain.model.DomainProperty"/>
      <c:set var="propertyValue" value="${user.getValue(propertyName)}"/>
      <c:if test="${(readOnly && not empty propertyValue) || not readOnly}">

      <c:set var="displaySensitivePictoInChamps" value="${false}"/>
      <c:choose>
        <c:when test="${isAdminScope and domainProperty.sensitive and user.hasSensitiveData()}">
          <c:set var="displaySensitivePictoInChamps" value="${true}"/>
          <div class="field sensitive_data" id="${propertyName}">
          <c:if test="${displayLabels}">
            <label class="txtlibform">
              ${user.getSpecificLabel(language, propertyName)}
            </label>
          </c:if>
        </c:when>
        <c:when test="${isAdminScope and domainProperty.sensitive}">
          <div class="field sensitive_no_active" id="${propertyName}">
          <c:if test="${displayLabels}">
            <label class="txtlibform">
              ${user.getSpecificLabel(language, propertyName)}
              <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
            </label>
          </c:if>
          <c:if test="${not displayLabels}">
            <label>
              <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
            </label>
          </c:if>
        </c:when>
        <c:otherwise>
          <div class="field" id="${propertyName}">
          <c:if test="${displayLabels}">
            <label class="txtlibform">
                ${user.getSpecificLabel(language, propertyName)}
            </label>
          </c:if>
        </c:otherwise>
      </c:choose>

        <div class="champs">
          <c:if test="${displaySensitivePictoInChamps}">
            <view:image src="${sensitivePicto}" css="${sensitiveCssClass}" alt="${sensitiveInfo}" title="${sensitiveInfo}"/>
          </c:if>
          <c:set var="propertyUpdatable" value="${not readOnly and (allFieldsUpdatable or ((isCurrentUserAdmin or isCurrentUserDomainManager) and user.isPropertyUpdatableByAdmin(propertyName)) or user.isPropertyUpdatableByUser(propertyName))}"/>
          <c:choose>
            <c:when test="${user.getPropertyType(propertyName) eq propertyTypeString}">
              <c:if test="${propertyUpdatable}">
                <c:choose>
                  <c:when test="${domainProperty.maxLength gt 100}">
                    <textarea rows="3" cols="50" name="prop_${propertyName}">${silfn:escapeHtml(propertyValue)}</textarea>
                  </c:when>
                  <c:otherwise>
                    <input type="text" name="prop_${propertyName}" size="50" maxlength="${domainProperty.maxLength}" value="${silfn:escapeHtml(propertyValue)}"/>
                  </c:otherwise>
                </c:choose>
              </c:if>
              <c:if test="${not propertyUpdatable}">
                ${silfn:escapeHtmlWhitespaces(propertyValue)}
              </c:if>
            </c:when>
            <c:when test="${user.getPropertyType(propertyName) eq propertyTypeUser}">
              <jsp:useBean id="propertyValue" type="java.lang.String"/>
              <c:set var="anotherUser" value="<%=UserDetail.getById(propertyValue)%>"/>
              <c:set var="checkedPropertyValue" value="${anotherUser == null ? '' : propertyValue}"/>
              <viewTags:selectUsersAndGroups selectionType="USER"
                                             domainIdFilter="${user.domainId}"
                                             userIds="${{checkedPropertyValue}}"
                                             userInputName="prop_${propertyName}"
                                             readOnly="${not propertyUpdatable}" />
            </c:when>
            <c:when test="${user.getPropertyType(propertyName) eq propertyTypeBoolean}">
              <c:if test="${propertyUpdatable}">
                <c:choose>
                  <c:when test="${silfn:booleanValue(propertyValue)}">
                    <input type="radio" name="prop_${propertyName}" value="1" checked="checked"/><fmt:message key="GML.yes"/>
                    <input type="radio" name="prop_${propertyName}" value="0"/><fmt:message key="GML.no"/>
                  </c:when>
                  <c:otherwise>
                    <input type="radio" name="prop_${propertyName}" value="1"/><fmt:message key="GML.yes"/>
                    <input type="radio" name="prop_${propertyName}" value="0" checked="checked"/><fmt:message key="GML.no"/>
                  </c:otherwise>
                </c:choose>
              </c:if>
              <c:if test="${not propertyUpdatable}">
                <c:choose>
                  <c:when test="${silfn:booleanValue(propertyValue)}">
                    <fmt:message key="GML.yes"/>
                  </c:when>
                  <c:otherwise>
                    <fmt:message key="GML.no"/>
                  </c:otherwise>
                </c:choose>
              </c:if>
            </c:when>
          </c:choose>
        </div>
      </div>
      </c:if>
    </c:if>
  </c:forEach>
</div>