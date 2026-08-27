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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="slfn" uri="silverpeas.tags.silverFunctions" %>
<%@ include file="check.jsp" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="deletionConfirm" key="confirmDeleteSubscription"/>

<c:set var="webCtxt"><%= m_context%></c:set>
<c:set var="context" value="${requestScope.context}"/>
<c:set var="path" value="${requestScope.path}"/>
<c:set var="ctrl" value="${requestScope.pdcSubscriptionPeas}"/>
<jsp:useBean id="ctrl" type="org.silverpeas.web.pdcsubscription.control.PdcSubscriptionSessionController"/>
<c:set var="action" value="${requestScope.action}"/>
<c:set var="readOnly" value="${action != null and action == 'showUserSubscriptions'}"/>
<c:set var="subscriptions" value="${requestScope.subscriptionList}"/>
<%-- In the window of the PdC, only the forced subscriptions are listed and they are managed by
     the managers of the PdC. In the window of the personal subscriptions, a forced subscription
     can neither be updated nor deleted by the subscriber himself. --%>
<c:set var="forcedContext" value="${context == 'pdc'}"/>
<c:set var="canManageForced" value="${forcedContext and ctrl.pdcManager}"/>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <script type="text/javascript" src="${webCtxt}/pdcPeas/jsp/javascript/formUtil.js"></script>
    <script type="text/javascript">
      function deleteSubscription() {
        const form = document.subscriptionList;
        const checked = $('input[name="pdcCheck"]:checked', form).length > 0;
        if (checked) {
          jQuery.popup.confirm('<view:encodeJs string="${deletionConfirm}"/>', function() {
            form.mode.value = 'delete';
            form.submit();
          });
        }
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="txtlist">
    <form name="subscriptionList" action="${action}" method="post">
      <input type="hidden" name="mode"/>
      <input type="hidden" name="context" value="${context}"/>
      <input type="hidden" name="scope" value="${requestScope.scope}"/>

      <view:browseBar componentId="${path}"/>

      <c:if test="${not readOnly}">
        <fmt:message var="subscrAdding" key="AddSC"/>
        <fmt:message var="subscrAddingIcon" key="icoAddNew" bundle="${icons}"/>
        <c:url var="addingIconURL" value="${subscrAddingIcon}"/>
        <c:url var="subscrAddingUrl"
               value="/RpdcSubscriptionPeas/jsp/PdcSubscription?path=${path}&context=${context}&scope=${requestScope.scope}"/>
        <view:operationPane>
          <view:operationOfCreation action="${subscrAddingUrl}"
                                    icon="${addingIconURL}"
                                    altText="${subscrAdding}"/>
          <c:if test="${not subscriptions.isEmpty()}">
            <fmt:message var="subscrDeletion" key="DeleteSC"/>
            <fmt:message var="subscrDeletionIcon" key="icoDelete" bundle="${icons}"/>
            <c:url var="deletionIconURL" value="${subscrDeletionIcon}"/>
            <view:operation action="javascript:deleteSubscription()"
                            altText="${subscrDeletion}"
                            icon="${deletionIconURL}"/>
          </c:if>
        </view:operationPane>
      </c:if>

      <view:window>
        <c:if test="${context == 'userSubscriptions'}">
          <fmt:message var="pdcTitle" key="pdc"/>
          <c:set var="userId" value="${requestScope.userId}"/>
          <c:set var="actionUrl"
                 value="ViewSubscriptionOfCategory?userId=${userId}&action=${action}&subResCategory="/>
          <view:tabs>
            <c:forEach var="category" items="${ctrl.subscriptionCategories}">
              <jsp:useBean id="category"
                           type="org.silverpeas.web.pdcsubscription.control.SubscriptionCategory"/>
              <view:tab label="${slfn:escapeHtml(category.label)}"
                        action="${actionUrl}${category.id}"
                        selected="false"
                        name="${category.id}"/>
            </c:forEach>
            <view:tab label="${pdcTitle}" action="#" selected="true" name="PDC"/>
          </view:tabs>
        </c:if>

        <view:frame>
          <view:areaOfOperationOfCreation/>
          <view:arrayPane var="tableau1" routingAddress="${action}?userId=${userId}">
            <fmt:message var="title" key="name"/>
            <fmt:message var="value" key="value"/>
            <c:if test="${not forcedContext}">
              <fmt:message var="subscriptionType" key="SubscriptionType"/>
              <view:arrayColumn title="${subscriptionType}" sortable="true"/>
            </c:if>
            <view:arrayColumn title="${title}"/>
            <view:arrayColumn title="${value}" sortable="false"/>
            <c:if test="${not readOnly}">
              <fmt:message var="operations" key="Operations"/>
              <view:arrayColumn title="${operations}" sortable="false"/>
            </c:if>
            <view:arrayLines var="subscr" items="${subscriptions}">
              <jsp:useBean id="subscr"
                           type="org.silverpeas.web.pdcsubscription.control.PdcSubscriptionData"/>
              <c:set var="manageable" value="${canManageForced or not subscr.forced}"/>
              <view:arrayLine>
                <c:if test="${not forcedContext}">
                  <view:arrayCellText text="${subscr.nature}"/>
                </c:if>
                <view:arrayCellText text="${silfn:escapeHtml(subscr.name)}"/>
                <view:arrayCellText text="${subscr.pdCPositions}"/>
                <c:if test="${not readOnly}">
                  <view:arrayCellText>
                    <c:if test="${manageable}">
                      <fmt:message var="edit" key="EditSC"/>
                      <c:url var="editIcon" value="/util/icons/update.gif"/>
                      <c:url var="editUrl"
                             value="/RpdcSubscriptionPeas/jsp/PdcSubscription?pdcSId=${subscr.id}&path=${path}&context=${context}&scope=${requestScope.scope}"/>
                      <view:icon iconName="${editIcon}" altText="${edit}" action="${editUrl}"/>
                      <span style="padding-left: 1em;"></span>
                      <input id="${subscr.id}" type="checkbox" name="pdcCheck" value="${subscr.id}"/>
                    </c:if>
                  </view:arrayCellText>
                </c:if>
              </view:arrayLine>
            </view:arrayLines>
          </view:arrayPane>

          <c:if test="${context == 'pdc'}">
            <br/>
            <view:buttonPane cssClass="center">
              <fmt:message var="goBack" key="GML.back"/>
              <view:button label="${goBack}" action="javascript:spAdminWindow.loadOperation(21)"/>
            </view:buttonPane>
          </c:if>
        </view:frame>
      </view:window>
    </form>
  </view:sp-body-part>
</view:sp-page>