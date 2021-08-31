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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle" var="profile"/>

<fmt:message key="myLinks.retour" var="goBackLabel"/>
<fmt:message key="myLinks.links" var="myLinksAppName"/>
<fmt:message key="myLinks.addLink" var="addLinkLabel"/>
<fmt:message key="myLinks.addLink" var="addLinkIcon" bundle="${icons}"/>
<c:url var="addLinkIcon" value="${addLinkIcon}"/>
<fmt:message key="myLinks.updateLink" var="updateLinkLabel"/>
<fmt:message key="myLinks.update" var="updateLinkIcon" bundle="${icons}"/>
<c:url var="updateLinkIcon" value="${updateLinkIcon}"/>
<fmt:message key="myLinks.deleteLinks" var="deleteSelectedLinksLabel"/>
<fmt:message key="myLinks.deleteLinks" var="deleteSelectedLinksIcon" bundle="${icons}"/>
<fmt:message key="myLinks.newPositionLink.messageConfirm" var="newPositionLinkConfirmMsg"/>

<c:set var="links" value="${requestScope.Links}"/>
<jsp:useBean id="links" type="java.util.Collection<org.silverpeas.core.mylinks.model.LinkDetail>"/>
<c:set var="url" value="${requestScope.UrlReturn}"/>
<c:set var="instanceId" value="${requestScope.InstanceId}"/>
<c:set var="appScope" value="${silfn:isDefined(instanceId)}"/>

<%@ include file="check.jsp"%>

<view:sp-page>
<view:sp-head-part withFieldsetStyle="true">
<script type="text/javascript">

function addLink() {
  cleanMyLinkForm();
  $("#linkFormId").attr('action', 'CreateLink');
  createLinkPopup();
}

function editLink(id) {
  spProgressMessage.show();
  cleanMyLinkForm();
  $("#linkFormId").attr('action', 'UpdateLink');
  new Promise(function(resolve) {
    getMyLink(id).then(function(link) {
      sp.log.debug("Update mylink identifier = #" + link.linkId);
      $("#hiddenLinkId").val(link.linkId);
      $("#urlId").val(link.url);
      $("#nameId").val(link.name);
      $("#descriptionId").val(link.description);
      $("#visibleId").prop('checked', link.visible);
      $("#popupId").prop('checked', link.popup);
      updateLinkPopup();
      resolve();
    }, function(request) {
      sp.log.debug("request.status=" + request.status);
      sp.log.debug("Cannot edit link because " + request.statusText);
      resolve();
    });
  }).then(function() {
    spProgressMessage.hide();
  })
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

function cleanMyLinkForm() {
  $("#linkFormId").attr('action', '');
  $("#hiddenLinkId").val("");
  $("#urlId").val("");
  $("#nameId").val("");
  $("#descriptionId").val("");
  $("#visibleId").prop('checked', false);
  $("#popupId").prop('checked', false);
}

function deleteSelectLinksConfirm() {
  jQuery.popup.confirm('<fmt:message key="myLinks.deleteSelection"/>', function() {
    spProgressMessage.show();
    document.readForm.mode.value = 'delete';
    document.readForm.submit();
  });
}

function ifCorrectFormExecute(callback) {
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
    callback.call(this);
  }
}

function createLinkPopup() {
  $('#mylink-popup-content').popup('validation', {
      title : "<fmt:message key="myLinks.addLink" />",
      width : "700px",
      isMaxWidth: false,
    callback : function() {
      sp.log.debug("User create new link !!!");
      ifCorrectFormExecute(submitLink);
      return false;
    }
  });
}

function updateLinkPopup() {
  $('#mylink-popup-content').popup('validation', {
      title : "<fmt:message key="myLinks.updateLink" />",
      width : "700px",
      isMaxWidth: false,
    callback : function() {
      sp.log.debug("User update the following link identifier = " + $("#linkId").attr('value'));
      ifCorrectFormExecute(submitLink);
      return false;
    }
  });
}

function submitLink() {
  spProgressMessage.show();
  const $linkUrl = $("#urlId");
  const cleanUrl = $linkUrl.val().replace(new RegExp("^" + webContext), '');
  $linkUrl.val(cleanUrl);
  $("#linkFormId").submit();
}

whenSilverpeasReady(function() {
  sp.selection.newCheckboxMonitor('#linkList');
});
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
    <view:operationOfCreation action="javaScript:addLink()" icon="${addLinkIcon}" altText="${addLinkLabel}"/>
    <view:operation action="javaScript:deleteSelectLinksConfirm()" icon="${deleteSelectedLinksIcon}" altText="${deleteSelectedLinksLabel}"/>
  </view:operationPane>
<view:window>
<view:frame>
<view:areaOfOperationOfCreation/>
<form name="readForm" action="DeleteLinks" method="post">
  <input type="hidden" name="mode"/>
  <view:arrayPane var="linkList" routingAddress="ViewLinks"
                  sortableLines="true" numberLinesPerPage="-1"
                  moveLineJsCallback="saveArrayLinesOrder(e, ui)">
    <fmt:message key="GML.nom" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false"/>
    <fmt:message key="GML.description" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false"/>
    <fmt:message key="GML.operations" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false"/>
    <c:forEach items="${links}" var="link">
      <view:arrayLine>
        <c:set var="id" value="${link.linkId}"/>
        <c:set var="linkUrl" value="${link.url}"/>
        <c:set var="name" value="${silfn:isDefined(link.name) ? link.name : linkUrl}"/>
        <c:if test="${not fn:contains(linkUrl, '://') and not fn:startsWith(linkUrl, '/website')}">
          <c:url var="linkUrl" value="${link.url}"/>
        </c:if>
        <view:arrayCellText>
          <c:set var="nameTarget" value=""/>
          <c:set var="nameClass" value=""/>
          <c:choose>
            <c:when test="${link.popup}">
              <c:set var="nameTarget" value="_blank"/>
            </c:when>
            <c:when test="${fn:startsWith(linkUrl, silfn:applicationURL())}">
              <c:set var="nameClass" value="sp-link"/>
            </c:when>
          </c:choose>
          <a href="${linkUrl}" class="${nameClass}" target="${nameTarget}">${silfn:escapeHtml(name)}</a>
        </view:arrayCellText>
        <view:arrayCellText text="${silfn:escapeHtml(link.description)}"/>
        <view:arrayCellText>
          <a href="javaScript:editLink('${id}')" title="${updateLinkLabel}">
            <img src="${updateLinkIcon}" alt="${updateLinkLabel}" title="${updateLinkLabel}">
            <span>&#160;&#160;</span>
            <input type="checkbox" name="linkCheck" value="${id}">
            <input type="hidden" name="hiddenLinkId" value="${id}">
          </a>
        </view:arrayCellText>
      </view:arrayLine>
    </c:forEach>
  </view:arrayPane>
</form>
  <c:if test="${appScope}">
    <br/>
    <view:buttonPane>
      <view:button label="${goBackLabel}" action="${url}"/>
    </view:buttonPane>
  </c:if>
</view:frame>
</view:window>

<div id="mylink-popup-content" style="display: none">
  <form name="linkForm" action="" method="post" id="linkFormId">
    <div>
      <label id="url_label" class="label-ui-dialog" for="urlId"><fmt:message key="myLinks.url"/></label>

      <div class="champ-ui-dialog">
        <input id="urlId" name="url" size="60" maxlength="150" type="text"/>&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
      </div>
      <label id="name_label" class="label-ui-dialog" for="nameId"><fmt:message key="GML.nom"/></label>

      <div class="champ-ui-dialog">
        <input id="nameId" name="name" size="60" maxlength="150" type="text"/>&nbsp;<img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
        <input type="hidden" name="linkId" value="" id="hiddenLinkId"/>
      </div>
      <label id="description_label" class="label-ui-dialog" for="descriptionId"><fmt:message key="GML.description"/></label>

      <div class="champ-ui-dialog">
        <input type="text" name="description" size="60" maxlength="150" value="" id="descriptionId"/>
      </div>

      <c:choose>
        <c:when test="${appScope}">
          <input type="hidden" name="instanceId" value="${instanceId}"/>
        </c:when>
        <c:otherwise>
          <label id="visible_label" class="label-ui-dialog" for="visibleId"><fmt:message key="myLinks.visible"/></label>
          <div class="champ-ui-dialog">
            <input type="checkbox" name="visible" value="true" id="visibleId"/>
          </div>
        </c:otherwise>
      </c:choose>

      <label id="popup_label" class="label-ui-dialog" for="popupId"><fmt:message key="myLinks.popup"/></label>

      <div class="champ-ui-dialog">
        <input type="checkbox" name="popup" value="true" id="popupId"/>
      </div>
    </div>

    <div id="mandatory_label">
      (<img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
      : <fmt:message key="GML.mandatory"/>)
    </div>
  </form>
</div>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>