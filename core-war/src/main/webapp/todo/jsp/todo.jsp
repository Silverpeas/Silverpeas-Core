<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.web.todo.control.ToDoHeaderUIEntity" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkTodo.jsp" %>

<view:setConstant var="PARTICIPANT_TODO_VIEW" constant="org.silverpeas.web.todo.control.ToDoSessionController.PARTICIPANT_TODO_VIEW"/>
<view:setConstant var="ORGANIZER_TODO_VIEW" constant="org.silverpeas.web.todo.control.ToDoSessionController.ORGANIZER_TODO_VIEW"/>
<view:setConstant var="CLOSED_TODO_VIEW" constant="org.silverpeas.web.todo.control.ToDoSessionController.CLOSED_TODO_VIEW"/>

<c:set var="TODAY" value="<%=new Date()%>"/>

<c:set var="todoCtrl" value="${requestScope.todo}"/>
<jsp:useBean id="todoCtrl" type="org.silverpeas.web.todo.control.ToDoSessionController"/>

<c:set var="userAction" value="${param.Action != null ? param.Action : ''}"/>
<jsp:useBean id="userAction" type="java.lang.String"/>

<c:set var="applicationLabel" value="${todoCtrl.getString('todo')}"/>
<c:set var="addTodoLabel" value="${todoCtrl.getString('ajouterTache')}"/>
<c:set var="deleteSelectedTodoLabel" value="${todoCtrl.getString('deleteSelectedTodo')}"/>
<c:set var="myTodoLabel" value="${todoCtrl.getString('mesTaches')}"/>
<c:set var="assignmentMonitoringLabel" value="${todoCtrl.getString('suiviAffectation')}"/>
<c:set var="historyLabel" value="${todoCtrl.getString('historique')}"/>
<c:set var="lockOpenLabel" value="${todoCtrl.getString('cadenas_ouvert')}"/>
<c:set var="lockClosedLabel" value="${todoCtrl.getString('cadenas_clos')}"/>

<c:set var="nameLabel" value="${todoCtrl.getString('nomToDo')}"/>
<c:set var="priorityLabel" value="${todoCtrl.getString('priorite')}"/>
<c:set var="broadcastListLabel" value="${todoCtrl.getString('listeDiffusionCourt')}"/>
<c:set var="organizerLabel" value="${todoCtrl.getString('organisateurToDo')}"/>
<c:set var="dueToLabel" value="${todoCtrl.getString('dueDateToDo')}"/>
<c:set var="percentCompletedLabel" value="${todoCtrl.getString('percentCompletedToDo')}"/>
<c:set var="actionLabel" value="${todoCtrl.getString('actions')}"/>

<c:set var="deleteSelectedTodoConfirmMessage" value="${todoCtrl.getString('deleteSelectedTodoConfirm')}"/>

<%
  if ("SetPercent".equals(userAction)) {
    String todoId = request.getParameter("ToDoId");
    String percent = request.getParameter("Percent");
    todo.setToDoPercentCompleted(todoId, percent);
  } else if ("CloseToDo".equals(userAction)) {
    String todoId = request.getParameter("ToDoId");
    todo.closeToDo(todoId);
  } else if ("ReopenToDo".equals(userAction)) {
    String todoId = request.getParameter("ToDoId");
    todo.reopenToDo(todoId);
  } else if ("ViewParticipantTodo".equals(userAction)) {
    todo.setViewType(ToDoSessionController.PARTICIPANT_TODO_VIEW);
  } else if ("ViewOrganizedTodo".equals(userAction)) {
    todo.setViewType(ToDoSessionController.ORGANIZER_TODO_VIEW);
  } else if ("ViewClosedTodo".equals(userAction)) {
    todo.setViewType(ToDoSessionController.CLOSED_TODO_VIEW);
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <title></title>
  <style type="text/css">
    .percent-completed-widget img {
      height: 5px;
      width: 5px;
      border: 0;
    }
  </style>
  <script type="text/javascript">

    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#dynamic-container input[name=selection]');

    function addToDo() {
      sp.navRequest('todoEdit.jsp')
        .withParam('Action', 'Add')
        .go();
    }

    function viewToDo(todoId) {
      sp.navRequest('todoEdit.jsp')
        .withParam('ToDoId', todoId)
        .withParam('Action', 'Update')
        .go();
    }

    function deleteSelectedToDo() {
      jQuery.popup.confirm('${silfn:escapeJs(deleteSelectedTodoConfirmMessage)}', function() {
        var ajaxRequest = sp.ajaxRequest("DeleteTodo").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function setPercent(todoId, percent) {
      performByKeepingSelected({'ToDoId' : todoId, 'Percent' : percent, 'Action' : 'SetPercent'})
          .then(function() {
            notySuccess(percent + '%');
          });
    }

    function closeToDo(todoId) {
      performByKeepingSelected({'ToDoId' : todoId, 'Action' : 'CloseToDo'});
    }

    function reopenToDo(todoId) {
      performByKeepingSelected({'ToDoId' : todoId, 'Action' : 'ReopenToDo'});
    }

    function viewParticipantTodo() {
      sp.navRequest('todo.jsp').withParam('Action', 'ViewParticipantTodo').go();
    }

    function viewOrganizedTodo() {
      sp.navRequest('todo.jsp').withParam('Action', 'ViewOrganizedTodo').go();
    }

    function viewClosedTodo() {
      sp.navRequest('todo.jsp').withParam('Action', 'ViewClosedTodo').go();
    }

    function performByKeepingSelected(params) {
      var ajaxRequest = sp.ajaxRequest("KeepingSelected").withParams(params).byPostMethod();
      checkboxMonitor.prepareAjaxRequest(ajaxRequest);
      return ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
    }

    function goTo(baseURL, Id, Type) {
      spWindow.loadLink(sp.url.format(baseURL + 'searchResult.jsp', {
        'Type' : Type, 'Id' : Id
      }));
    }

    var percentCompletedWidget = function(target) {
      var _todo = JSON.parse(target.getAttribute('todo-data'));
      _todo.htmlParts = [];

      var _getIcon = function(percent, completedPercent) {
        return percent > completedPercent ? 'icons/off.gif' : 'icons/on.gif';
      };

      var _timer;
      var _refresh = function(completedPercent) {
        clearTimeout(_timer);
        _timer = setTimeout(function() {
          _todo.htmlParts.forEach(function(htmlPart) {
            htmlPart.data.img.src = _getIcon(htmlPart.data.percent, completedPercent);
          });
        }, 0);
      };

      for (var i = 0; i < 10; i++) {
        var _data = {percent : (i + 1) * 10};

        var currentHtmlImgPart = document.createElement('img');
        currentHtmlImgPart.src = _getIcon(_data.percent, _todo.completedPercent);
        currentHtmlImgPart.alt = _data.percent + '%';
        _data.img = currentHtmlImgPart;

        var currentHtmlLinkPart = document.createElement('a');
        currentHtmlLinkPart.href = 'javascript:void(0);';
        currentHtmlLinkPart.title = _data.percent + '%';
        currentHtmlLinkPart.addEventListener('click', function() {
          setPercent(_todo.id, this.data.percent);
        });
        currentHtmlLinkPart.addEventListener('mouseout', function() {
          _refresh(_todo.completedPercent);
        });
        currentHtmlLinkPart.addEventListener('mouseover', function() {
          _refresh(this.data.percent);
        });

        currentHtmlLinkPart.appendChild(currentHtmlImgPart);
        currentHtmlLinkPart.data = _data;
        _todo.htmlParts.push(currentHtmlLinkPart);
        target.appendChild(currentHtmlLinkPart);
      }
    };
  </script>
</head>
<body>
<view:browseBar componentId="${applicationLabel}" clickable="false"/>
<view:operationPane>
  <c:url var="opIcon" value="/util/icons/create-action/add-task.png"/>
  <view:operationOfCreation action="javascript:onClick=addToDo()"
                            altText="${addTodoLabel}" icon="${opIcon}"/>
  <c:url var="opIcon" value="/util/icons/delete.gif"/>
  <view:operation action="javascript:onClick=deleteSelectedToDo()"
                  altText="${deleteSelectedTodoLabel}" icon="${opIcon}"/>
</view:operationPane>
<view:window>
  <view:areaOfOperationOfCreation/>
  <view:tabs>
    <view:tab label="${myTodoLabel}" action="javascript:onClick=viewParticipantTodo()" selected="${todoCtrl.viewType eq PARTICIPANT_TODO_VIEW}"/>
    <view:tab label="${assignmentMonitoringLabel}" action="javascript:onClick=viewOrganizedTodo()" selected="${todoCtrl.viewType eq ORGANIZER_TODO_VIEW}"/>
    <view:tab label="${historyLabel}" action="javascript:onClick=viewClosedTodo()" selected="${todoCtrl.viewType eq CLOSED_TODO_VIEW}"/>
  </view:tabs>
  <view:frame>
    <div id="dynamic-container">
      <c:set var="todoEntities" value="<%=ToDoHeaderUIEntity.convertList(todoCtrl, todoCtrl.getToDos(), todoCtrl.getSelectedTodoIds())%>"/>
      <view:arrayPane var="todo-list-${todoCtrl.viewType}" routingAddress="todoPagination" numberLinesPerPage="25">
        <view:arrayColumn title="${nameLabel}" compareOn="${e -> e.path}"/>
        <view:arrayColumn title="${priorityLabel}" compareOn="${e -> e.data.priority}"/>
        <view:arrayColumn title="${todoCtrl.viewType eq ORGANIZER_TODO_VIEW ? broadcastListLabel : organizerLabel}" compareOn="${e -> e.attendeeLabel}"/>
        <view:arrayColumn title="${dueToLabel}" compareOn="${e -> e.data.endDate}"/>
        <view:arrayColumn title="${percentCompletedLabel}" compareOn="${e -> e.data.percentCompleted}"/>
        <view:arrayColumn title="${actionLabel}" sortable="false"/>
        <view:arrayColumn/>
        <view:arrayLines var="todoEntity" items="${todoEntities}" varStatus="line">
          <c:set var="todo" value="${todoEntity.data}"/>
          <c:set var="hotClass" value="${todo.percentCompleted < 100 and todo.completedDay == null
                                     and todo.endDate != null and TODAY.after(todo.endDate)
                                     ? 'ArrayCellHot' : ''}"/>
          <c:set var="path" value="${silfn:escapeHtml(todoEntity.path)}"/>
          <c:set var="pathJsCall" value="viewToDo('${todo.id}')"/>
          <c:if test="${not empty todo.externalId}">
            <%-- Trick for workflow --%>
            <c:set var="externalId" value="${fn:replace(todo.externalId, '#', '$')}"/>
            <c:url var="componentInstanceUrl" value="${silfn:componentURL(todo.componentId)}"/>
            <c:set var="pathJsCall" value="goTo('${componentInstanceUrl}','${externalId}','TodoDetail')"/>
          </c:if>
          <view:arrayLine classes="${hotClass}">
            <view:arrayCellText>
              <a class="${hotClass}" href="javascript:onClick=${pathJsCall}">${path}</a>
            </view:arrayCellText>
            <view:arrayCellText text="${todoEntity.priorityLabel}"/>
            <view:arrayCellText text="${todoEntity.attendeeLabel}"/>
            <view:arrayCellText text="${todo.endDate != null ? silfn:formatDate(todo.endDate, todoCtrl.language) : ''}"/>
            <view:arrayCellText text="${todoEntity.percentCompletedLabel}"/>
            <view:arrayCellText>
              <c:if test="${empty todo.externalId}">
                <c:choose>
                  <c:when test="${todoCtrl.viewType eq PARTICIPANT_TODO_VIEW}">
                    <div class="percent-completed-widget" todo-data='{"id":"${todo.id}","completedPercent":${todo.percentCompleted}}'></div>
                  </c:when>
                  <c:when test="${todoCtrl.viewType eq ORGANIZER_TODO_VIEW}">
                    <a href="javascript:onclick=closeToDo('${todo.id}')" title="${lockOpenLabel}">
                      <img src="icons/unlock.gif" alt="${lockOpenLabel}" width="15" height="15"/>
                    </a>
                  </c:when>
                  <c:when test="${todo.delegatorId eq todoCtrl.userId}">
                    <a href="javascript:onclick=reopenToDo('${todo.id}')" title="${lockClosedLabel}">
                      <img src="icons/lock.gif" alt="${lockOpenLabel}" width="15" height="15"/>
                    </a>
                  </c:when>
                </c:choose>
              </c:if>
            </view:arrayCellText>
            <c:choose>
              <c:when test="${empty todo.externalId}">
                <c:choose>
                  <c:when test="${todo.delegatorId eq todoCtrl.userId}">
                    <view:arrayCellCheckbox name="selection" checked="${todoEntity.selected}" value="${todoEntity.id}"/>
                  </c:when>
                  <c:otherwise>
                    <view:arrayCellText/>
                  </c:otherwise>
                </c:choose>
              </c:when>
              <c:otherwise>
                <c:choose>
                  <c:when test="${fn:length(todoEntity.attendees) eq 1 and todo.delegatorId eq todoEntity.attendees.iterator().next().userId}">
                    <view:arrayCellCheckbox name="selection" checked="${todoEntity.selected}" value="${todoEntity.id}"/>
                  </c:when>
                  <c:otherwise>
                    <view:arrayCellText/>
                  </c:otherwise>
                </c:choose>
              </c:otherwise>
            </c:choose>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          checkboxMonitor.pageChanged();
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-container', {
            before : checkboxMonitor.prepareAjaxRequest
          });
          jQuery('.percent-completed-widget').each(function() {
            new percentCompletedWidget(this);
          });
        });
      </script>
    </div>
  </view:frame>
</view:window>
</body>
</html>