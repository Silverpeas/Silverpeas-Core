<%--

    Copyright (C) 2000 - 2026 Silverpeas

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
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="checkPdc.jsp"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="deletionConfirmation" key="pdcPeas.confirmDeleteAxis"/>
<fmt:message var="toolName" key="pdcPeas.pdc"/>
<fmt:message var="functionName" key="pdcPeas.pdcDefinition"/>
<fmt:message var="primaryAxis" key="pdcPeas.primaryAxis"/>
<fmt:message var="secondaryAxis" key="pdcPeas.secondaryAxis"/>
<fmt:message var="axisName" key="pdcPeas.axisName"/>
<fmt:message var="definition" key="pdcPeas.definition"/>
<fmt:message var="operation" key="pdcPeas.axisOperation"/>
<fmt:message var="axisView" key="pdcPeas.viewAxis"/>
<fmt:message var="subscriptions" key="GML.manageSubscriptions"/>

<fmt:message var="iconAxis" key="pdcPeas.icoComponent" bundle="${icons}"/>
<c:url var="iconsAxisUrl" value="${iconAxis}"/>

<c:set var="displayLanguage" value="${requestScope.DisplayLanguage}"/>
<c:set var="creationAllowed" value="${requestScope.CreationAllowed.equals('1')}"/>
<c:set var="isAdmin" value="${requestScope.IsAdmin}"/>
<c:set var="viewType" value="${requestScope.ViewType}"/>
<c:set var="axisList" value="${requestScope.AxisList}"/>
<c:set var="manageableAxisList" value="${requestScope.ManageableAxis}"/>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
<title><%=resource.getString("GML.popupTitle")%></title>
<script type="application/javascript">
	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("${deletionConfirmation}");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
    const boxItems = document.viewAxis.deleteAxis;
    let selectItems = "";
    if (boxItems !== null){
			// au moins une checkbox exist
      const nbBox = boxItems.length;
      if (nbBox === null && boxItems.checked === true ){
				// il n'y a qu'une checkbox sélectionnée
				selectItems += boxItems.value;
			} else{
				// search checked boxes
				for (let i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked === true){
						selectItems += boxItems[i].value+",";
					}
				}
				selectItems = selectItems.substring(0,selectItems.length-1); // erase the last coma
			}
			if ( (selectItems.length > 0) && (areYouSure())  ){
				// an axis has been selected !
				document.viewAxis.Ids.value = selectItems;
				document.viewAxis.action = "DeleteAxis";
				document.viewAxis.submit();
			}
		}
	}

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '700', '600','scrollbars=yes, resizable, alwaysRaised');
	}
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin">
	<form name="viewAxis" action="Main" method="post">
	<input type="hidden" name="Ids"/>

  <view:browseBar domainName="${toolName}" componentId="${functionName}">
    <view:browseBarLangSelector link="ChangeLanguage" lang="${displayLanguage}"/>
  </view:browseBar>

  <c:if test="${isAdmin}">
    <fmt:message var="axisDeletion" key="pdcPeas.deleteAxis"/>
    <fmt:message var="iconAxisDeletion" key="pdcPeas.icoDeleteAxis" bundle="${icons}"/>
    <c:url var="iconsAxisDeletionUrl" value="${iconAxisDeletion}"/>
    <view:operationPane>
    <c:if test="${creationAllowed}">
      <fmt:message var="axisCreation" key="pdcPeas.createAxis"/>
      <fmt:message var="iconAxisCreation" key="pdcPeas.icoCreateAxis" bundle="${icons}"/>
      <c:url var="iconAxisCreationUrl" value="${iconAxisCreation}"/>
      <view:operationOfCreation action="javascript:openSPWindow('NewAxis','newaxis')"
                                icon="${iconAxisCreationUrl}"
                                altText="${axisCreation}"/>
    </c:if>
    <c:if test="${axisList != null && !axisList.isEmpty()}">
      <view:operation action="javascript:getSelectedItems()"
                      icon="${iconsAxisDeletionUrl}"
                      altText="${axisDeletion}"/>
    </c:if>
    <view:operation
        action="/silverpeas/RpdcSubscriptionPeas/jsp/ViewSubscriptionTaxonomy?userId=all&context=pdc&scope=${toolName}"
        altText="${subscriptions}" />
  </view:operationPane>
  </c:if>

  <view:window>
    <view:areaOfOperationOfCreation/>

    <view:tabs>
      <view:tab label="${primaryAxis}" action="ChangeViewType?ViewType=P"
                selected="${viewType.equals('P')}"/>
      <view:tab label="${secondaryAxis}" action="ChangeViewType?ViewType=S"
                selected="${viewType.equals('S')}"/>
    </view:tabs>

    <view:frame>

      <view:arrayPane var="PdcPeas" routingAddress="Main">
        <view:arrayColumn title="&nbsp;" sortable="false"/>
        <view:arrayColumn title="${axisName}" sortable="false"/>
        <view:arrayColumn title="${definition}" sortable="false"/>
        <view:arrayColumn title="${operation}" sortable="false"/>
        <view:arrayLines var="axis" items="${axisList}">
          <jsp:useBean id="axis" type="org.silverpeas.core.pdc.pdc.model.AxisHeader"/>
          <view:arrayLine>
            <c:set var="axisName"
                   value="${silfn:escapeHtml(axis.getName(displayLanguage))}"/>
            <c:set var="axisDescription"
                   value="${silfn:escapeHtmlWhitespaces(axis.getDescription(displayLanguage))}"/>
            <view:arrayCellText>
              <a href="ViewAxis?Id=${axis.PK.id}" title="${axisView} ${axisName}">
                <div style="align-content: center">
                  <img src="${iconsAxisUrl}"
                       alt="${axisView} ${axisName}"
                       title="${axisView} ${axisName}"/>
                </div>
              </a>
            </view:arrayCellText>
            <view:arrayCellText>
              <a href="ViewAxis?Id=${axis.PK.id}" title="${axisView} ${axisName}">
                <span class="textePetitBold">${axisName}</span>
              </a>
            </view:arrayCellText>
            <view:arrayCellText>
              <span class="textePetitBold">${axisDescription}</span>
            </view:arrayCellText>
            <view:arrayCellText>
              <div style="vertical-align: center">
                <c:if
                    test="${isAdmin or (manageableAxisList != null and manageableAxisList.contains(axis.PK.id))}">
                  <fmt:message var="axisEdition" key="pdcPeas.editAxis"/>
                  <fmt:message var="iconAxisUpdate" key="pdcPeas.update" bundle="${icons}"/>
                  <c:url var="iconAxisUpdateUrl" value="${iconAxisUpdate}"/>
                  <view:icon iconName="${iconAxisUpdateUrl}"
                               altText="${axisEdition}"
                               action="javascript:openSPWindow('EditAxis?Id=${axis.PK.id}&Translation=${axis.language}','editaxis')">
                  </view:icon>
                </c:if>
                <c:if test="${isAdmin}">
                  <span style="padding-left: 1em;"></span>
                  <input id="delete${axis.PK.id}" type="checkbox" name="deleteAxis"
                         value="${axis.PK.id}"/>
                </c:if>
              </div>
            </view:arrayCellText>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>

    </view:frame>
  </view:window>
  </form>
  <form name="refresh" action="Main" method="post"></form>
</view:sp-body-part>
</view:sp-page>