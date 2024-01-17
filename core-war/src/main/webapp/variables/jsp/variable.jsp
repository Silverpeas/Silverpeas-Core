<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="variable" value="${requestScope['Variable']}"/>
<c:set var="values" value="${variable.variableValues}"/>

<jsp:useBean id="variable" type="org.silverpeas.core.variables.Variable"/>
<jsp:useBean id="values" type="org.silverpeas.core.variables.VariableValueSet"/>

<fmt:message var="browseBarAll" key="variables.breadcrumb.all"/>
<fmt:message var="deleteConfirm" key="variables.variable.value.delete.confirm"/>
<fmt:message var="addValue" key="variables.variable.value.create"/>
<fmt:message var="updateValue" key="variables.variable.value.update"/>
<fmt:message var="labelValues" key="variables.variable.values"/>

<fmt:message var="fieldsetVariable" key="variables.variable"/>
<fmt:message var="colLabel" key="GML.label"/>
<fmt:message var="colValue" key="GML.valeur"/>
<fmt:message var="colStart" key="variables.variable.start"/>
<fmt:message var="colEnd" key="variables.variable.end"/>
<fmt:message var="colOperations" key="GML.operations"/>

<fmt:message var="opUpdate" key="GML.update"/>
<fmt:message var="opDelete" key="GML.delete"/>

<c:url var="iconUpdate" value="/util/icons/update.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>
<c:url var="iconAdd" value="/variables/jsp/icons/addPeriod.png"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript">
    var arrayPaneAjaxControl;
    var baseURL = webContext+"/services/variables/${variable.id}";
    function checkForm() {
      var value = $("#value").val().trim();

      if (value === "") {
        SilverpeasError.add("'${colValue}' <fmt:message key="GML.MustBeFilled"/>");
      }

      var dateErrors = isPeriodValid('startDate', 'endDate');
      $(dateErrors).each(function(index, error) {
        SilverpeasError.add(error.message);
      });

      return !SilverpeasError.show();
    }

    function newScheduledValue() {
      clearForm();
      $('#variablePeriod-popin').popup('validation', {
        title : "${addValue}",
        width : "700px",
        isMaxWidth: false,
        callback : function() {
          if (checkForm()) {
            var url = baseURL+"/values";
            var scheduledValue = getScheduledValue();
            var ajaxRequest = sp.ajaxRequest(url).byPostMethod();
            ajaxRequest.send(scheduledValue).then(function() {
              reloadScheduledValues();
            });
          } else {
            return false;
          }
        }
      });
    }

    function getScheduledValue() {
      return {
        "value": $("#dnForm #value").val(),
        "beginDate": sp.moment.formatUiDateAsLocalDate($("#dnForm #startDate").val()),
        "endDate": sp.moment.formatUiDateAsLocalDate($("#dnForm #endDate").val())
      };
    }

    function editScheduledValue(id) {
      clearForm();
      $("#dnForm #value").val($("#"+id+" .colValue").html());
      $("#dnForm #startDate").val($("#"+id+" .colBegin").text());
      $("#dnForm #endDate").val($("#"+id+" .colEnd").text());

      $('#variablePeriod-popin').popup('validation', {
        title : "${updateValue}",
        width : "700px",
        isMaxWidth: false,
        callback : function() {
          if (checkForm()) {
            var url = baseURL+"/values/"+id;
            var scheduledValue = getScheduledValue();
            var ajaxRequest = sp.ajaxRequest(url).byPostMethod();
            ajaxRequest.send(scheduledValue).then(function() {
              reloadScheduledValues();
            });
          } else {
            return false;
          }
        }
      });
    }

    function clearForm() {
      $("#dnForm #value").val("");
      $("#dnForm #startDate").val("");
      $("#dnForm #endDate").val("");
    }

    function deleteScheduledValue(id) {
      jQuery.popup.confirm("${deleteConfirm}", function() {
        var url = baseURL+"/values/"+id;
        var ajaxRequest = sp.ajaxRequest(url).byDeleteMethod();
        ajaxRequest.send().then(function() {
          $("#values #"+id).remove();
        });
      });
    }

    function reloadScheduledValues() {
      var ajaxRequest = sp.ajaxRequest("EditVariable").withParam("Id", "${variable.id}");
      ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
    }

    function updateVariable() {
      var newLabel = $("#valueForm #label").val();
      var value = {
        "label": newLabel
      };

      var ajaxRequest = sp.ajaxRequest(baseURL).byPostMethod();
      ajaxRequest.send(value).then(function() {
        $("#breadCrumb .information").text(newLabel);
      });
    }
  </script>
</head>
<body class="page_content_admin variable_admin">
<view:browseBar extraInformations="${variable.label}">
  <view:browseBarElt link="Main" label="${browseBarAll}"/>
</view:browseBar>
<view:operationPane>
  <view:operationOfCreation action="javascript:newScheduledValue()" icon="${iconAdd}" altText="${addValue}" />
</view:operationPane>
<view:window>
<view:frame>
  <form id="valueForm">
    <input type="hidden" name="Id" value="${variable.id}"/>
    <fieldset id="main" class="skinFieldset">
      <legend>${fieldsetVariable}</legend>
      <div class="fields">
        <div class="field" id="form-row-name">
          <label class="txtlibform"><fmt:message key="GML.label"/></label>
          <div class="champs">
            <input type="text" id="label" name="Label" value="${variable.label}" size="20" maxlength="150"/>
          </div>
        </div>
      </div>
      <view:button label="${opUpdate}" action="javascript:updateVariable()"/>
    </fieldset>
  </form>

  <view:areaOfOperationOfCreation/>

  <fieldset id="values" class="skinFieldset">
    <legend>${labelValues}</legend>
    <div id="dynamic-container">
      <view:arrayPane var="arrayOfVariableValues" routingAddress="EditVariable" numberLinesPerPage="1000">
        <view:arrayColumn title="${colValue}" />
        <view:arrayColumn title="${colStart}"/>
        <view:arrayColumn title="${colEnd}"/>
        <view:arrayColumn title="${colOperations}" sortable="false"/>

        <c:forEach var="value" items="${values}">
        <view:arrayLine id="${value.id}">
          <view:arrayCellText text="${value.value}" classes="ArrayCell colValue"/>
          <view:arrayCellText text="${silfn:formatDate(silfn:toDate(value.period.startDate), lang)}" compareOn="${value.period.startDate}" classes="ArrayCell colBegin"/>
          <view:arrayCellText text="${silfn:formatDate(silfn:toDate(value.period.endDate), lang)}" compareOn="${value.period.endDate}" classes="ArrayCell colEnd"/>
          <view:arrayCellText>
            <a href="#" onclick="editScheduledValue('${value.id}')" title="${opUpdate}" class="edit-value"><img src="${iconUpdate}" alt="${opUpdate}"/></a>
            <a href="#" onclick="deleteScheduledValue('${value.id}')" title="${opDelete}" class="delete-value"><img src="${iconDelete}" alt="${opDelete}"/></a>
          </view:arrayCellText>
        </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-container');
        });
      </script>
    </div>
  </fieldset>
</view:frame>
</view:window>

<div id="variablePeriod-popin" style="display: none">
  <form name="dnForm" id="dnForm">
    <div>
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
    <div id="mandatory_label">
      (<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
      : <fmt:message key="GML.mandatory"/>)
    </div>
  </form>
</div>

<view:progressMessage/>

</body>
</html>