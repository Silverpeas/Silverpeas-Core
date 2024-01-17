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

<fmt:message key="GML.back" var="goBackLabel"/>
<fmt:message key="myLinks.links" var="myLinksAppName"/>
<fmt:message key="myLinks.categoryManagement" var="categoryManagementLabel"/>
<fmt:message key="myLinks.addCategory" var="addCategoryLabel"/>
<fmt:message key="myLinks.addCategory" var="addCategoryIcon" bundle="${icons}"/>
<c:url var="addCategoryIcon" value="${addCategoryIcon}"/>
<fmt:message key="myLinks.updateCategory" var="updateCategoryLabel"/>
<fmt:message key="myLinks.update" var="updateCategoryIcon" bundle="${icons}"/>
<c:url var="updateCategoryIcon" value="${updateCategoryIcon}"/>
<fmt:message key="myLinks.deleteCategories" var="deleteSelectedCategoriesLabel"/>
<fmt:message key="myLinks.deleteCategories" var="deleteSelectedCategoriesIcon" bundle="${icons}"/>
<fmt:message key="myLinks.newPositionCategory.messageConfirm" var="newPositionCategoryConfirmMsg"/>

<c:set var="categories" value="${requestScope.Categories}"/>
<jsp:useBean id="categories" type="java.util.Collection<org.silverpeas.core.mylinks.model.CategoryDetail>"/>

<%@ include file="check.jsp" %>

<view:sp-page>
  <view:sp-head-part withFieldsetStyle="true">
    <script type="text/javascript">
      const checkboxMonitor = sp.selection.newCheckboxMonitor('#category-list input[name=categoryCheck]');
      let arrayPaneAjaxControl;

      function addCategory() {
        MyLinksCtrl.addCategoryIntoContext().then(refreshCategoryArray);
      }

      function editCategory(id) {
        MyLinksCtrl.editCategoryIntoContext(id).then(refreshCategoryArray);
      }

      function deleteSelectedCategories() {
        MyLinksCtrl
            .deleteCategories(checkboxMonitor.getSelectedValues())
            .then(arrayPaneAjaxControl.refreshFromRequestResponse);
      }

      function refreshCategoryArray() {
        return sp.ajaxRequest('ViewCategories').send()
            .then(arrayPaneAjaxControl.refreshFromRequestResponse);
      }

      function saveArrayLinesOrder(e, ui) {
        const ajaxUrl = webContext + '/services/mylinks/categories/saveLinesOrder';
        const positionData = {
          "position" : ui.item.index(), "catId" : ui.item.find("input[name=hiddenCategoryId]").val()
        };
        spProgressMessage.show();
        sp.ajaxRequest(ajaxUrl).byPostMethod().send(positionData).then(function() {
          notySuccess("${silfn:escapeJs(newPositionCategoryConfirmMsg)}");
          spProgressMessage.hide();
        });
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar>
      <view:browseBarElt label="${myLinksAppName}" link="ViewLinks"/>
      <view:browseBarElt label="${categoryManagementLabel}"/>
    </view:browseBar>
    <view:operationPane>
      <view:operationOfCreation action="javaScript:addCategory()" icon="${addCategoryIcon}" altText="${addCategoryLabel}"/>
      <view:operation action="javaScript:deleteSelectedCategories()" icon="${deleteSelectedCategoriesIcon}" altText="${deleteSelectedCategoriesLabel}"/>
    </view:operationPane>
    <view:window>
      <view:frame>
        <view:areaOfOperationOfCreation/>
        <div id="category-list">
          <view:arrayPane var="categoryList" routingAddress="ViewCategories"
                          sortableLines="true" numberLinesPerPage="-1"
                          moveLineJsCallback="saveArrayLinesOrder(e, ui)">
            <fmt:message key="GML.nom" var="tmpLabel"/>
            <view:arrayColumn title="${tmpLabel}" sortable="false"/>
            <fmt:message key="GML.description" var="tmpLabel"/>
            <view:arrayColumn title="${tmpLabel}" sortable="false"/>
            <fmt:message key="GML.operations" var="tmpLabel"/>
            <view:arrayColumn title="${tmpLabel}" sortable="false"/>
            <c:forEach items="${categories}" var="category">
              <view:arrayLine>
                <c:set var="id" value="${category.id}"/>
                <view:arrayCellText text="${silfn:escapeHtml(category.name)}"/>
                <view:arrayCellText text="${silfn:escapeHtml(category.description)}"/>
                <view:arrayCellText>
                  <a href="javaScript:editCategory('${id}')" title="${updateCategoryLabel}">
                    <img src="${updateCategoryIcon}" alt="${updateCategoryLabel}" title="${updateCategoryLabel}"/>
                  </a>
                  <span>&#160;&#160;</span>
                  <input type="checkbox" name="categoryCheck" value="${id}"/>
                  <input type="hidden" name="hiddenCategoryId" value="${id}"/>
                </view:arrayCellText>
              </view:arrayLine>
            </c:forEach>
          </view:arrayPane>
          <script type="text/javascript">
            whenSilverpeasReady(function() {
              checkboxMonitor.pageChanged();
              arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#category-list');
            });
          </script>
        </div>
        <br/>
        <view:buttonPane>
          <view:button label="${goBackLabel}" action="ViewLinks"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>