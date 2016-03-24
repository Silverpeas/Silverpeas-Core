<%@ tag import="org.silverpeas.core.admin.user.model.UserDetail" %>
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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<c:url var="iconUser" value="/util/icons/user.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>

<fmt:message key="GML.delete" bundle="${generalBundle}" var="labelDelete"/>
<fmt:message key="GML.user" bundle="${generalBundle}" var="labelUser"/>
<fmt:message key="GML.eMail" bundle="${generalBundle}" var="labelEmail"/>

<%-- Creator --%>
<%@ attribute name="user" required="true" type="org.silverpeas.core.admin.user.model.UserFull"
              description="The user to display" %>

<%@ attribute name="allFieldsUpdatable" required="false" type="java.lang.Boolean"
              description="If true all fields are updatable" %>

<%@ attribute name="readOnly" required="true" type="java.lang.Boolean"
              description="True if all fields must be readonly" %>

<%@ attribute name="includeEmail" required="true" type="java.lang.Boolean"
              description="True if email must be displayed" %>

<%@ attribute name="linear" required="false" type="java.lang.Boolean"
              description="True if fields must be displayed one per line" %>

<c:set var="isCurrentUserAdmin" value="<%=UserDetail.getCurrentRequester().isAccessAdmin() %>"/>

<view:setConstant var="propertyTypeUser" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_USERID"/>
<view:setConstant var="propertyTypeString" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_STRING"/>
<view:setConstant var="propertyTypeBoolean" constant="org.silverpeas.core.admin.domain.model.DomainProperty.PROPERTY_TYPE_BOOLEAN"/>

<c:set var="passwordPrefix" value="password"/>

<c:if test="${allFieldsUpdatable == null}">
  <c:set var="allFieldsUpdatable" value="${false}"/>
</c:if>

<c:set var="listStyleTwoFieldsPerLine" value="fields"/>
<c:set var="listStyleOneFieldPerLine" value="oneFieldPerLine"/>

<c:set var="listStyle" value="${readOnly ? listStyleTwoFieldsPerLine : listStyleOneFieldPerLine}"/>
<c:if test="${linear}">
  <c:set var="listStyle" value="${listStyleOneFieldPerLine}"/>
</c:if>

<script type="text/javascript">
  function removeRelatedUser(propertyName) {
    $("#prop_"+propertyName).val("");
    $("#user_"+propertyName).val("");
  }
</script>

<div class="${listStyle}">
  <c:if test="${silfn:isDefined(user.eMail) and includeEmail}">
    <div class="field" id="email">
      <label class="txtlibform">${labelEmail}</label>
      <div class="champs">${user.eMail}</div>
    </div>
  </c:if>
  <c:forEach items="${user.propertiesNames}" var="propertyName">
    <c:set var="passwordField" value="${fn:startsWith(propertyName, passwordPrefix)}"/>
    <c:if test="${not passwordField}">
      <c:set var="domainProperty" value="${user.getProperty(propertyName)}"/>
      <c:set var="propertyValue" value="${user.getValue(propertyName)}"/>
      <div class="field" id="${propertyName}">
        <label class="txtlibform">
          ${silfn:escapeHtml(user.getSpecificLabel(_language, propertyName))}
        </label>
        <div class="champs">
          <c:set var="propertyUpdatable" value="${not readOnly and (allFieldsUpdatable or (isCurrentUserAdmin and user.isPropertyUpdatableByAdmin(propertyName)) or user.isPropertyUpdatableByUser(propertyName))}"/>
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
              <c:if test="${propertyUpdatable}">
                <c:set var="checkedPropertyValue" value="${anotherUser == null ? '' : propertyValue}"/>
                <input type="hidden" name="prop_${propertyName}" id="prop_${propertyName}" value="${checkedPropertyValue}"/>
                <input type="text" disabled="disabled" readonly="readonly" id="user_${propertyName}" size="50" value="${anotherUser == null ? "" : anotherUser.displayedName}"/>
                <a href="#" onclick="javascript:SP_openWindow('/silverpeas/RselectionPeasWrapper/jsp/open?elementId=prop_${propertyName}&elementName=user_${propertyName}&selectedUser=${silfn:escapeHtml(checkedPropertyValue)}&domainIdFilter=${user.domainId}','selectUser',800,600,'');return false;"/>
                <img src="${iconUser}" width="15" height="15" border="0" alt="${labelUser}" align="top" title="${labelUser}"/></a>
                <a href="#" onclick="javascript:removeRelatedUser('${propertyName}');return false;"/>
                <img src="${iconDelete}" width="15" height="15" border="0" alt="${labelDelete}" align="top" title="${labelDelete}"/></a>
              </c:if>
              <c:if test="${not propertyUpdatable}">
                <c:if test="${anotherUser != null}">
                  <view:username userId="${propertyValue}"/>
                </c:if>
              </c:if>
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
  </c:forEach>
</div>