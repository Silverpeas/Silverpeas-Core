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

<c:set var="creationMode" value="${silfn:booleanValue(requestScope.creationMode)}"/>
<c:set var="category" value="${requestScope.Category}"/>
<jsp:useBean id="category" type="org.silverpeas.core.webapi.mylinks.CategoryEntity"/>

<%@ include file="check.jsp" %>

<div id="mylink-category-form">
  <div>
    <label id="name_label" class="label-ui-dialog" for="nameId"><fmt:message key="GML.nom"/></label>
    <div class="champ-ui-dialog">
      <input id="nameId" name="name" size="60" maxlength="255" type="text" value="${category.name}"/>&nbsp;<img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
      <c:if test="${not creationMode}">
        <input type="hidden" name="catId" value="${category.categoryId}" id="hiddenCategoryId"/>
        <input type="hidden" name="position" value="${category.position}" id="hiddenPosition"/>
      </c:if>
    </div>
    <label id="description_label" class="label-ui-dialog" for="descriptionId"><fmt:message key="GML.description"/></label>
    <div class="champ-ui-dialog">
      <input type="text" name="description" size="60" maxlength="255" value="${category.description}" id="descriptionId"/>
    </div>
  </div>
  <div id="mandatory_label">
    (<img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
    : ${mandatoryLabel})
  </div>
</div>
<script type="text/javascript">
  function checkCategoryForm() {
    return new Promise(function(resolve) {
      const name = $("#nameId").val().trim();
      SilverpeasError.reset();
      if (StringUtil.isNotDefined(name)) {
        SilverpeasError.add('<strong><fmt:message key="GML.nom"/></strong> <fmt:message key="GML.MustBeFilled"/>');
      }
      if (!SilverpeasError.show()) {
        resolve();
      }
    });
  }
</script>