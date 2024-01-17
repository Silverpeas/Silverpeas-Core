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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/myLinks" prefix="viewTags" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle" var="profile"/>

<fmt:message key="myLinks.retour" var="goBackLabel"/>
<fmt:message key="myLinks.links" var="myLinksAppName"/>
<fmt:message key="myLinks.addLink" var="addLinkLabel"/>
<fmt:message key="myLinks.addLink" var="addLinkIcon" bundle="${icons}"/>
<c:url var="addLinkIcon" value="${addLinkIcon}"/>
<fmt:message key="myLinks.manageCategories" var="manageCategoriesLabel"/>
<fmt:message key="myLinks.manageCategories" var="manageCategoriesIcon" bundle="${icons}"/>
<fmt:message key="myLinks.updateCategoryOfLinks" var="modifyCategoryOfSelectedLinksLabel"/>
<fmt:message key="myLinks.updateCategoryOfLinks" var="modifyCategoryOfSelectedLinksIcon" bundle="${icons}"/>
<fmt:message key="myLinks.deleteLinks" var="deleteSelectedLinksLabel"/>
<fmt:message key="myLinks.deleteLinks" var="deleteSelectedLinksIcon" bundle="${icons}"/>
<fmt:message key="myLinks.newPositionLink.messageConfirm" var="newPositionLinkConfirmMsg"/>

<c:set var="linksByCategory" value="${requestScope.LinksByCategory}"/>
<jsp:useBean id="linksByCategory" type="java.util.Map<org.silverpeas.core.mylinks.model.CategoryDetail,java.util.List<org.silverpeas.core.mylinks.model.LinkDetail>>"/>
<c:set var="url" value="${requestScope.UrlReturn}"/>
<c:set var="instanceId" value="${requestScope.InstanceId}"/>
<c:set var="appScope" value="${silfn:isDefined(instanceId)}"/>

<%@ include file="check.jsp" %>

<view:sp-page>
  <view:sp-head-part withFieldsetStyle="true">
    <view:link href="/myLinksPeas/jsp/styleSheets/myLinksPeas.css"/>
    <view:script src="/myLinksPeas/jsp/javaScript/myLinksPeas.js"/>
    <script type="text/javascript">
      const checkboxMonitors = new function() {
        const monitors = [];
        this.register = function(cssSelector) {
          monitors.push(sp.selection.newCheckboxMonitor(cssSelector));
        };
        this.getSelectedValues = function() {
          const selectedValues = [];
          monitors.forEach(function(monitor) {
            Array.prototype.push.apply(selectedValues, monitor.getSelectedValues());
          });
          return selectedValues;
        };
        this.pageChanged = function() {
          monitors.forEach(function(monitor) {
            monitor.pageChanged();
          })
        };
      };
      const arrayPaneAjaxControls = new function() {
        const controls = {};
        this.register = function (cssSelector) {
          controls[cssSelector.replace(/[#-.()]/g, '_')] = sp.arrayPane.ajaxControls(cssSelector);
        };
        this.refreshFromRequestResponse = function(request) {
          const promises = [];
          for (let key in controls) {
            promises.push(controls[key].refreshFromRequestResponse(request));
          }
          return sp.promise.whenAllResolved(promises);
        }
      };

      function addLink() {
        MyLinksCtrl.addLinkIntoContext().then(refreshLinkArrays);
      }

      function editLink(id) {
        MyLinksCtrl.editLinkIntoContext(id).then(refreshLinkArrays);
      }

      function modifyCategoryOfSelectedLinks() {
        MyLinksCtrl.modifyCategoryOfUserLinks(checkboxMonitors.getSelectedValues()).then(refreshLinkArrays);
      }

      function deleteSelectedLinks() {
        MyLinksCtrl.deleteLinks(checkboxMonitors.getSelectedValues()).then(renderLinkArrays);
      }

      function refreshLinkArrays() {
        return sp.ajaxRequest('ViewLinks').send().then(renderLinkArrays);
      }

      function renderLinkArrays(request) {
        return arrayPaneAjaxControls
            .refreshFromRequestResponse(request)
            .then(ctrl.refresh, ctrl.refresh);
      }

      function saveArrayLinesOrder(e, ui) {
        const ajaxUrl = webContext + '/services/mylinks/saveLinesOrder';
        const positionData = {
          "position" : ui.item.index(), "linkId" : ui.item.find("input[name=hiddenLinkId]").val()
        };
        spProgressMessage.show();
        sp.ajaxRequest(ajaxUrl).byPostMethod().send(positionData).then(function() {
          notySuccess("${silfn:escapeJs(newPositionLinkConfirmMsg)}");
          spProgressMessage.hide();
        });
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${appScope ? instanceId : myLinksAppName}">
      <c:if test="${appScope}">
        <fmt:message key="myLinks.linksByComponent" var="myLinksAppName"/>
        <view:browseBarElt label="${myLinksAppName}"/>
      </c:if>
    </view:browseBar>
    <view:operationPane>
      <c:if test="${not appScope}">
        <view:operation action="ViewCategories" icon="${manageCategoriesIcon}" altText="${manageCategoriesLabel}"/>
        <view:operationSeparator/>
      </c:if>
      <view:operationOfCreation action="javaScript:addLink()" icon="${addLinkIcon}" altText="${addLinkLabel}"/>
      <c:if test="${not appScope}">
        <view:operation action="javaScript:modifyCategoryOfSelectedLinks()" icon="${modifyCategoryOfSelectedLinksIcon}" altText="${modifyCategoryOfSelectedLinksLabel}"/>
      </c:if>
      <view:operation action="javaScript:deleteSelectedLinks()" icon="${deleteSelectedLinksIcon}" altText="${deleteSelectedLinksLabel}"/>
    </view:operationPane>
    <view:window>
      <view:frame>
        <view:areaOfOperationOfCreation/>
        <ul id="categories">
          <c:forEach var="entry" items="${linksByCategory}" varStatus="status">
            <li id="category${entry.key.id eq -1 ? '' : ''.concat(entry.key.id)}" class="category initializing">
              <viewTags:displayCategoryLinks category="${entry.key}" links="${entry.value}" open="${status.first}"/>
            </li>
          </c:forEach>
        </ul>
        <script type="text/javascript">
          const ctrl = new MyLinksController();
        </script>
        <c:if test="${appScope}">
          <br/>
          <view:buttonPane>
            <view:button label="${goBackLabel}" action="${url}"/>
          </view:buttonPane>
        </c:if>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>