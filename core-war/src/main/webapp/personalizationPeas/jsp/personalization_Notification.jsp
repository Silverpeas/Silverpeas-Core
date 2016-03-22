<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %><%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkPersonalization.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="validationMessage" value="${requestScope.validationMessage}" />
<c:set var="testExplanation" value="${requestScope.testExplanation}"/>
<c:set var="isMultiChannelNotif" value="${requestScope.multichannel}"/>
<c:set var="notifAddresses" value="${requestScope.notificationAddresses}"/>
<c:url value="/RSILVERMAIL/jsp/Main" var="silvermailURL"/>
<c:url value="/RSILVERMAIL/jsp/SentUserNotifications" var="sentNotificationsURL"/>
<fmt:message key="MesNotifications" var="myNotifications"/>
<fmt:message key="ParametrerNotification" var="parametrize"/>
<fmt:message key="operationPane_addadress" var="addAddress"/>
<fmt:message key="operationPane_paramnotif" var="op_parametrizeNotif"/>
<fmt:message key="LireNotification" var="viewNotif"/>
<fmt:message key="SentUserNotifications" var="sentNotif"/>
<fmt:message key="ParametrerNotification" var="parametrizeNotif"/>
<fmt:message key="arrayPane_Default" var="defaultColumn"/>
<fmt:message key="arrayPane_Nom" var="nameColumn"/>
<fmt:message key="arrayPane_Adresse" var="addressColumn"/>
<fmt:message key="arrayPane_Operations" var="operationsColumn"/>
<fmt:message key="GML.validate" var="buttonValidationLabel"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<view:looknfeel/>
<script type="text/javascript">
function editNotif(id){
	SP_openWindow("editNotification.jsp?id=" + id,"addNotif","600","250","scrollbars=yes");
}
function paramNotif(){
	SP_openWindow("paramNotif.jsp","paramNotif","750","400","scrollbars=yes");
}
function deleteCanal(id){
  if (window.confirm("<fmt:message key='MessageSuppressionCanal'/>")) {
    document.channelForm.action = "ParametrizeNotification";
    document.channelForm.Action.value = "Delete";
    document.channelForm.id.value = id;
    document.channelForm.submit();
  }
}

function setDefault(id) {
  document.channelForm.action = "ParametrizeNotification";
  document.channelForm.Action.value = "SetDefault";
  document.channelForm.id.value = id;
  document.channelForm.submit();
}

function testNotification(id) {
  document.channelForm.action = "ParametrizeNotification";
  document.channelForm.Action.value = "Test";
  document.channelForm.id.value = id;
  document.channelForm.submit();
}

function sendChoiceChannel() {
  document.channelForm.SelectedChannels.value = getChannels();
  document.channelForm.SelectedFrequency.value = getFrequency();
  document.channelForm.action = "SaveChannels";
  document.channelForm.submit();
}

function getChannels() {
  var  items = "";
  try {
    var boxItems = document.channelForm.SelectChannel;
    if (boxItems != null){
      // au moins une checkbox exist
      var nbBox = boxItems.length;
      if ( (nbBox == null) && (boxItems.checked == true) ){
        items += boxItems.value+",";
      } else{
        for (i=0;i<boxItems.length ;i++ ){
          if (boxItems[i].checked == true){
            items += boxItems[i].value+",";
          }
        }
      }
    }
  } catch (e) {
    //Checkboxes are not displayed
  }
  return items;
}

function getFrequency() {
  return $("#SelectFrequency").val();
}

function onChangeFrequency() {
  document.channelForm.action = "ParametrizeNotification";
  document.channelForm.Action.value = "SetFrequency";
  document.channelForm.id.value = getFrequency();
  document.channelForm.submit();
}
</script>
</head>
<body>
<view:browseBar componentId="${myNotifications}" path="${parametrize}"/>
<view:operationPane>
  <view:operation icon="${addProtocol}" altText="${addAddress}" action="javascript:editNotif(-1);"/>
  <view:operationSeparator/>
  <view:operation icon="${param.notif}" altText="${op_parametrizeNotif}" action="javascript:paramNotif();"/>
</view:operationPane>
<view:window>
  <view:tabs>
    <view:tab label="${viewNotif}" action="${silvermailURL}" selected="false"/>
    <view:tab label="${sentNotif}" action="${sentNotificationsURL}" selected="false"/>
    <view:tab label="${parametrizeNotif}" action="ParametrizeNotification" selected="true"/>
  </view:tabs>

  <view:frame>

  <c:if test="${not empty validationMessage}">
    <div class="inlineMessage-ok">
      <c:out value="${validationMessage}" />
    </div>
  </c:if>
  <c:if test="${not empty testExplanation}">
    <div class="inlineMessage">${testExplanation}</div>
  </c:if>

  <p align="left"><b><fmt:message key="channelChoiceLabel" /></b></p>
  <form name="channelForm" action="" method="POST">
    <input type="hidden" name="Action"/>
    <input type="hidden" name="id"/>
    <input type="hidden" name="SelectedChannels"/>
    <input type="hidden" name="SelectedFrequency"/>

    <view:arrayPane var="personalization" routingAddress="ParametrizeNotification">
      <view:arrayColumn title="${defaultColumn}" sortable="false"/>
      <view:arrayColumn title="${nameColumn}" sortable="true"/>
      <view:arrayColumn title="${addressColumn}" sortable="true"/>
      <view:arrayColumn title="${operationsColumn}" sortable="false"/>
      <c:forEach var="props" items="${notifAddresses}">
        <c:set var="name" value="${fn:escapeXml(props['name'])}"/>
        <c:set var="address" value="${fn:escapeXml(props['address'])}"/>
        <view:arrayLine>
          <%
            IconPane actions = gef.getIconPane();
            Properties props = (Properties) pageContext.getAttribute("props");
            ArrayLine arrayLine = (ArrayLine) pageContext.getAttribute(ArrayLineTag.ARRAY_LINE_PAGE_ATT);
            String id = Encode.forHtml(props.getProperty("id"));
          %>
          <c:choose>
            <c:when test="${!isMultiChannelNotif}">
              <%
                Icon anAction = actions.addIcon();
                if (props.getProperty("isDefault").equalsIgnoreCase("true")) {
                  anAction.setProperties(on_default, "", "");
                } else {
                  anAction.setProperties(off_default, resource.getString("iconPane_Default"),
                      "javascript:setDefault('" + id + "');");
                }
                arrayLine.addArrayCellIconPane(actions);
              %>
            </c:when>
            <c:otherwise>
              <%
                // print the choice cases for a multiple selection
                String usedCheck = "";
                if (props.getProperty("isDefault").equalsIgnoreCase("true")) {
                  usedCheck = "checked=\"checked\"";
                }
                pageContext.setAttribute("cellText",
                    "<input type='checkbox' name='SelectChannel' value='" + id + "' " + usedCheck + "/>");
              %>
              <view:arrayCellText text="${cellText}"/>
            </c:otherwise>
          </c:choose>
          <view:arrayCellText text="${name}"/>
          <view:arrayCellText text="${address}"/>
          <%
            // add the icons related to the suppression and to the modification
            actions = gef.getIconPane();
            if (props.getProperty("canEdit").equalsIgnoreCase("true")) {
              Icon modifier = actions.addIcon();
              modifier.setProperties(modif, resource
                  .getString("GML.modify"), "javascript:editNotif(" + id + ");");
            } else {
              Icon modifier = actions.addIcon();
              modifier.setProperties(ArrayPnoColorPix, "", "");
            }
            if (props.getProperty("canDelete").equalsIgnoreCase("true")) {
              Icon del = actions.addIcon();
              del.setProperties(delete, resource.getString("GML.delete"),
                  "javascript:deleteCanal('" + id + "');");
            } else {
              Icon del = actions.addIcon();
              del.setProperties(ArrayPnoColorPix, "", "");
            }

            if (props.getProperty("canTest").equalsIgnoreCase("true")) {
              Icon tst = actions.addIcon();
              tst.setProperties(test,
                  resource.getString("iconPane_Test"),
                  "javascript:testNotification('" + id + "');");
            } else {
              Icon tst = actions.addIcon();
              tst.setProperties(ArrayPnoColorPix, "", "");
            }

            arrayLine.addArrayCellIconPane(actions);
          %>

        </view:arrayLine>
      </c:forEach>
    </view:arrayPane>

    <br/>
    <p align="left"><b><fmt:message key="frequencyChoiceLabel" /></b>
      <fmt:message key="frequency${requestScope.delayedNotification.defaultFrequency.name}" var="defaultFrequencyLabel" />
      <c:set var="currentUserFrequencyCode" value="${requestScope.delayedNotification.currentUserFrequencyCode}" />
      <c:set var="frequencyOnChange" value="" />
      <c:if test="${!isMultiChannelNotif}">
        <c:set var="frequencyOnChange" value="javascript:onChangeFrequency();" />
      </c:if>
      <select id="SelectFrequency" name="SelectFrequency" onchange="${frequencyOnChange}">
        <c:set var="currentUserFrequencyCode" value="${requestScope.delayedNotification.currentUserFrequencyCode}" />
        <c:choose>
          <c:when test="${empty currentUserFrequencyCode or empty requestScope.delayedNotification.frequencies}">
            <option value="" selected="selected"><fmt:message key="frequencyDefault"><fmt:param value="${defaultFrequencyLabel}"/></fmt:message></option>
          </c:when>
          <c:otherwise>
            <option value=""><fmt:message key="frequencyDefault"><fmt:param value="${defaultFrequencyLabel}"/></fmt:message></option>
          </c:otherwise>
        </c:choose>
        <c:forEach items="${requestScope.delayedNotification.frequencies}" var="frequency">
          <c:choose>
            <c:when test="${frequency.code eq currentUserFrequencyCode}">
              <option value="${frequency.code}" selected="selected"><fmt:message key="frequency${frequency.name}" /></option>
            </c:when>
            <c:otherwise>
              <option value="${frequency.code}"><fmt:message key="frequency${frequency.name}" /></option>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </select>
    </p>

    <c:if test="${isMultiChannelNotif}">
      <view:buttonPane verticalPosition="center">
        <view:button label="${buttonValidationLabel}" action="javascript:onClick=sendChoiceChannel();" disabled="false"/>
      </view:buttonPane>
    </c:if>
  </form>
  </view:frame>
</view:window>
</body>
</html>