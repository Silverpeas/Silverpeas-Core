<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.web.variables.VariableUIEntity" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="allVariables" value="${requestScope['AllVariables']}"/>
<jsp:useBean id="allVariables" type="org.silverpeas.core.util.SilverpeasList"/>

<c:set var="selectedVariableIds" value="${requestScope.SelectedIds}"/>
<jsp:useBean id="selectedVariableIds" type="java.util.Set"/>

<fmt:message var="browseBarAll" key="variables.breadcrumb.all"/>
<fmt:message var="deleteSelection" key="GML.action.selection.delete"/>
<fmt:message var="deleteSelectionConfirm" key="GML.action.selection.delete.confirm"/>
<fmt:message var="deleteConfirm" key="variables.delete.confirm"/>
<fmt:message var="addVal" key="variables.create"/>
<fmt:message var="updateVal" key="variables.update"/>

<fmt:message var="colLabel" key="GML.label"/>
<fmt:message var="colValue" key="variables.variable.current"/>
<fmt:message var="colStart" key="variables.variable.start"/>
<fmt:message var="colEnd" key="variables.variable.end"/>
<fmt:message var="colPeriods" key="variables.variable.values.number"/>
<fmt:message var="colOperations" key="GML.operations"/>

<fmt:message var="opUpdate" key="GML.update"/>
<fmt:message var="opDelete" key="GML.delete"/>

<c:url var="iconUpdate" value="/util/icons/update.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>
<c:url var="iconAdd" value="/variables/jsp/icons/addVariable.png"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel withFieldsetStyle="true"/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript">

    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#dynamic-container input[name=selection]');
    var baseURL = webContext+"/services/variables";

    function checkForm() {
      var label = $("#label").val().trim();
      var value = $("#value").val().trim();

      if (label === "") {
        SilverpeasError.add("'${colLabel}' <fmt:message key="GML.MustBeFilled"/>");
      }
      if (value === "") {
        SilverpeasError.add("'${colValue}' <fmt:message key="GML.MustBeFilled"/>");
      }

      var dateErrors = isPeriodValid('startDate', 'endDate');
      $(dateErrors).each(function(index, error) {
        SilverpeasError.add(error.message);
      });

      return !SilverpeasError.show();
    }

    function newVariable() {
      clearForm();
      $('#variable-popin').popup('validation', {
        title : "${addVal}",
        width : "700px",
        isMaxWidth: false,
        callback : function() {
          if (checkForm()) {
            var variable = {
              "label": $("#dnForm #label").val(),
              "values": [{
                "value": $("#dnForm #value").val(),
                "beginDate": sp.moment.formatUiDateAsLocalDate($("#dnForm #startDate").val()),
                "endDate": sp.moment.formatUiDateAsLocalDate($("#dnForm #endDate").val())
              }]
            };

            var ajaxRequest = sp.ajaxRequest(baseURL).byPostMethod();
            ajaxRequest.send(variable).then(function() {
              reloadVariables();
            });
          } else {
            return false;
          }
        }
      });
    }

    function editVariable(id) {
      location.href = "EditVariable?Id="+id;
    }

    function clearForm() {
      $("#dnForm #label").val("");
      $("#dnForm #value").val("");
      $("#dnForm #startDate").val("");
      $("#dnForm #endDate").val("");
    }

    function deleteSelectedVariables() {
      jQuery.popup.confirm('${silfn:escapeJs(deleteSelectionConfirm)}', function() {
        var ajaxRequest = sp.ajaxRequest("DeleteSelectedVariables").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function deleteVariable(id) {
      jQuery.popup.confirm("${silfn:escapeJs(deleteConfirm)}", function() {
        var url = baseURL+"/"+id;
        var ajaxRequest = sp.ajaxRequest(url).byDeleteMethod();
        ajaxRequest.send().then(function() {
          reloadVariables();
        });
      });
    }

    function reloadVariables() {
      var ajaxRequest = sp.ajaxRequest("Main");
      checkboxMonitor.prepareAjaxRequest(ajaxRequest);
      ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
    }
  </script>
</head>
<body class="page_content_admin variables_admin">
<view:browseBar extraInformations="${browseBarAll}"/>
<view:operationPane>
  <view:operationOfCreation action="javascript:newVariable()" icon="${iconAdd}" altText="${addVal}" />
  <view:operation action="javascript:deleteSelectedVariables()" icon="" altText="${deleteSelection}"/>
</view:operationPane>
<view:window>
<view:frame>
  <view:applyTemplate locationBase="core:variables" name="welcome"/>
  <view:areaOfOperationOfCreation/>
  <div id="dynamic-container">
    <c:set var="valueItems" value="<%=VariableUIEntity.convertList(allVariables, selectedVariableIds)%>"/>
    <view:arrayPane var="arrayOfVariables" routingAddress="Main" numberLinesPerPage="25">
      <view:arrayColumn width="10" sortable="false"/>
      <view:arrayColumn title="${colLabel}" compareOn="${r -> r.data.label}"/>
      <view:arrayColumn title="${colValue}" compareOn="${r -> r.refVariableValue.value}"/>
      <view:arrayColumn title="${colStart}" compareOn="${r -> r.refStartDate}"/>
      <view:arrayColumn title="${colEnd}" compareOn="${r -> r.refEndDate}"/>
      <view:arrayColumn title="${colPeriods}" compareOn="${r -> r.data.numberOfValues}"/>
      <view:arrayColumn title="${colOperations}" sortable="false"/>
      <view:arrayLines var="variable" items="${valueItems}">
        <view:arrayLine>
          <view:arrayCellCheckbox name="selection" checked="${variable.selected}" value="${variable.id}"/>
          <view:arrayCellText><a href="javascript:editVariable('${variable.id}')">${variable.data.label}</a></view:arrayCellText>
          <view:arrayCellText text="${variable.refVariableValue.valueForHTML}"/>
          <view:arrayCellText text="${silfn:formatDate(variable.refStartDate, lang)}"/>
          <view:arrayCellText text="${silfn:formatDate(variable.refEndDate, lang)}"/>
          <view:arrayCellText classes="variable-nb-values">${variable.data.numberOfValues}</view:arrayCellText>
          <view:arrayCellText>
            <a href="#" onclick="editVariable('${variable.id}'); return false;" title="${opUpdate}" class="edit-variable"><img src="${iconUpdate}" alt="${opUpdate}"/></a>
            <a href="#" onclick="deleteVariable('${variable.id}'); return false;" title="${opDelete}" class="delete-variable"><img src="${iconDelete}" alt="${opDelete}"/></a>
          </view:arrayCellText>
        </view:arrayLine>
      </view:arrayLines>
    </view:arrayPane>
    <script type="text/javascript">
      whenSilverpeasReady(function() {
        checkboxMonitor.pageChanged();
        arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-container', {
          before : checkboxMonitor.prepareAjaxRequest
        });
      });
    </script>
  </div>
</view:frame>
</view:window>

<div id="variable-popin" style="display: none">
  <form name="dnForm" id="dnForm">
    <div class="table">
      <label class="label-ui-dialog" for="label">${colLabel}</label>
      <div class="champ-ui-dialog">
        <input id="label" name="Label" size="60" maxlength="150" type="text"/>&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
      </div>

      <label class="label-ui-dialog" for="value">${colValue}</label>
      <div class="champ-ui-dialog">
        <textarea id="value" name="Value" cols="60" rows="7" maxlength="2000"></textarea>&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
      </div>

      <label class="label-ui-dialog" for="startDate">${colStart}</label>
      <div class="champ-ui-dialog">
        <input id="startDate" type="text" class="dateToPick" name="StartDate" size="12" maxlength="10"/>
      </div>

      <label class="label-ui-dialog" for="endDate">${colEnd}</label>
      <div class="champ-ui-dialog">
        <input id="endDate" type="text" class="dateToPick" name="EndDate" size="12" maxlength="10"/>
      </div>
    </div>
    <div id="mandatory_label">(<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/> : <fmt:message key="GML.mandatory"/>)</div>
  </form>
</div>

<view:progressMessage/>

</body>
</html>