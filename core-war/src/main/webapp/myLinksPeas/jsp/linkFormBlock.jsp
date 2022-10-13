<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle" var="profile"/>

<fmt:message key="GML.mandatory" var="mandatoryLabel"/>

<%@ include file="check.jsp" %>

<c:set var="hideUrl" value="${silfn:booleanValue(param.hideUrl)}"/>
<c:set var="updateCategoryOnly" value="${silfn:booleanValue(requestScope.updateCategoryOnly)}"/>
<c:set var="creationMode" value="${silfn:booleanValue(requestScope.creationMode)}"/>
<c:set var="link" value="${requestScope.Link}"/>
<jsp:useBean id="link" type="org.silverpeas.core.webapi.mylinks.MyLinkEntity"/>
<c:set var="categories" value="${requestScope.AllUserCategories}"/>
<jsp:useBean id="categories" type="java.util.Collection<org.silverpeas.core.webapi.mylinks.CategoryEntity>"/>
<c:set var="instanceId" value="${link.instanceId}"/>
<c:set var="appScope" value="${silfn:isDefined(instanceId)}"/>

<c:set var="hide" value="${c -> c ? 'style=\"display:none;\"' : ''}"/>

<div id="mylink-link-form">
  <div>
    <label ${hide(hideUrl or updateCategoryOnly)} id="url_label" class="label-ui-dialog" for="urlId"><fmt:message key="myLinks.url"/></label>
    <div ${hide(hideUrl or updateCategoryOnly)} class="champ-ui-dialog">
      <input id="urlId" name="url" size="60" maxlength="255" type="text" value="${link.url}"/>&nbsp;<img alt="${mandatoryLabel}" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
    </div>
    <label ${hide(updateCategoryOnly)} id="name_label" class="label-ui-dialog" for="nameId"><fmt:message key="GML.nom"/></label>
    <div ${hide(updateCategoryOnly)} class="champ-ui-dialog">
      <input id="nameId" name="name" size="60" maxlength="255" type="text" value="${link.name}"/>&nbsp;<img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
      <c:if test="${not creationMode}">
        <input type="hidden" name="linkId" value="${link.linkId}" id="hiddenLinkId"/>
        <input type="hidden" name="position" value="${link.position}" id="hiddenPosition"/>
      </c:if>
    </div>
    <label ${hide(updateCategoryOnly)} id="description_label" class="label-ui-dialog" for="descriptionId"><fmt:message key="GML.description"/></label>
    <div ${hide(updateCategoryOnly)} class="champ-ui-dialog">
      <input type="text" name="description" size="60" maxlength="255" value="${link.description}" id="descriptionId"/>
    </div>
    <c:if test="${not empty categories}">
      <label id="cat_label" class="label-ui-dialog" for="cat_id"><fmt:message key="myLinks.category"/></label>
      <div class="champ-ui-dialog">
        <select id="cat_id" name="categoryId">
          <option value=""></option>
          <c:forEach var="category" items="${categories}">
            <option value="${category.categoryId}" title="${category.description}"
              ${category.categoryId eq link.categoryId ? 'selected' : ''}>${silfn:escapeHtml(category.name)}</option>
          </c:forEach>
        </select>
      </div>
    </c:if>
    <c:choose>
      <c:when test="${appScope}">
        <input type="hidden" name="instanceId" value="${instanceId}"/>
      </c:when>
      <c:otherwise>
        <label ${hide(updateCategoryOnly)} id="visible_label" class="label-ui-dialog" for="visibleId"><fmt:message key="myLinks.visible"/></label>
        <div ${hide(updateCategoryOnly)} class="champ-ui-dialog">
          <input type="checkbox" name="visible" id="visibleId" value="true" ${link.visible ? 'checked' : ''}/>
        </div>
      </c:otherwise>
    </c:choose>
    <label ${hide(updateCategoryOnly)} id="popup_label" class="label-ui-dialog" for="popupId"><fmt:message key="myLinks.popup"/></label>
    <div ${hide(updateCategoryOnly)} class="champ-ui-dialog">
      <input type="checkbox" name="popup" id="popupId" value="true" ${link.popup ? 'checked' : ''}/>
    </div>
  </div>
  <div ${hide(updateCategoryOnly)} id="mandatory_label">
    <img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/> : ${mandatoryLabel}
  </div>
</div>
<script type="text/javascript">
  function checkLinkForm() {
    return new Promise(function(resolve) {
      const url = $("#urlId").val().trim();
      const name = $("#nameId").val().trim();
      SilverpeasError.reset();
      if (StringUtil.isNotDefined(url)) {
        SilverpeasError.add('<strong><fmt:message key="myLinks.url"/></strong> <fmt:message key="GML.MustBeFilled"/>');
      }
      if (StringUtil.isNotDefined(name)) {
        SilverpeasError.add('<strong><fmt:message key="GML.nom"/></strong> <fmt:message key="GML.MustBeFilled"/>');
      }
      if (!SilverpeasError.show()) {
        resolve();
      }
    });
  }
</script>