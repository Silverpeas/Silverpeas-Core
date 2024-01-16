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
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.subscription.SubscriptionResourceTypeRegistry" %>
<%@ page import="java.util.stream.Collectors" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ include file="check.jsp" %>

<view:setConstant constant="org.silverpeas.core.subscription.constant.SubscriberType.GROUP" var="GROUP_SUBSCRIBER_TYPE"/>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<fmt:message var="rootPath" key="Path"/>
<fmt:message var="deleteActionLabel" key="DeleteSC"/>
<fmt:message var="deleteIconUrl" key="icoDelete" bundle="${icons}"/>
<fmt:message var="pdcTypeLabel" key="pdc"/>
<fmt:message var="subscriptionTypeLabel" key="SubscriptionType"/>
<fmt:message var="operationsLabel" key="Operations"/>

<c:set var="controller" value="<%=sessionController%>"/>
<c:set var="subscriptionResourceTypes" value="<%=SubscriptionResourceTypeRegistry.get().streamAll().collect( Collectors.toList())%>"/>

<c:set var="subscriptions" value="${requestScope.subscriptions}"/>
<jsp:useBean id="subscriptions" type="java.util.List<org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean>"/>
<c:set var="userId" value="${requestScope.userId}"/>
<c:set var="subResType" value="${requestScope.subResType}"/>
<jsp:useBean id="subResType" type="org.silverpeas.core.subscription.SubscriptionResourceType"/>
<c:set var="action" value="${requestScope.action}"/>
<c:set var="isReadOnly" value="${action eq 'showUserSubscriptions'}"/>

<view:sp-page>
  <view:sp-head-part>
    <script type="text/javascript">
      function confirmDelete() {
        const boxItems = document.readForm.subscriptionCheckbox;
        if (boxItems != null) {
          const nbBox = boxItems.length;
          let sendIt = false;
          if ((nbBox == null) && (boxItems.checked == true)) {
            sendIt = true;
          } else {
            for (let i = 0; i < boxItems.length; i++) {
              if (boxItems[i].checked == true) {
                sendIt = true;
              }
            }
          }

          if (sendIt) {
            jQuery.popup.confirm("<%=resource.getString("confirmDeleteSubscription")%>",
                function() {
                  document.readForm.mode.value = 'delete';
                  document.readForm.submit();
                });
          }
        }
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${rootPath}"/>
    <c:if test="${not isReadOnly && not empty subscriptions}">
      <view:operationPane>
        <view:operation icon="${deleteIconUrl}" action="javascript:confirmDelete()" altText="${deleteActionLabel}"/>
      </view:operationPane>
    </c:if>
    <view:window>
      <view:tabs>
        <c:forEach var="subscriptionResourceType" items="${subscriptionResourceTypes}">
          <c:set var="selectedType" value="${subscriptionResourceType eq subResType}"/>
          <c:url var="tabUrl" value="${'ViewSubscriptionOfType'}">
            <c:param name="userId" value="${userId}"/>
            <c:param name="subResType" value="${subscriptionResourceType.name}"/>
            <c:param name="action" value="${action}"/>
          </c:url>
          <view:tab label="${controller.getSubscriptionResourceTypeLabel(subscriptionResourceType)}"
                    action="${selectedType ? 'javascript:void(0)' : tabUrl}" selected="${selectedType}" name="${subscriptionResourceType.name}"/>
        </c:forEach>
        <view:tab label="${pdcTypeLabel}"
                  action="${isReadOnly ? 'showUserSubscriptions' : 'ViewSubscriptionTaxonomy'}?userId=${userId}" selected="false" name="PDC"/>
      </view:tabs>
      <view:frame>
        <form name="readForm" action="DeleteSubscriptionOfType" method="post">
          <input type="hidden" name="mode"/>
          <input type="hidden" name="subResType" value="${subResType.name}"/>
          <c:url var="arrayUrl" value="${'ViewSubscriptionOfType'}">
            <c:param name="userId" value="${userId}"/>
            <c:param name="subResType" value="${subResType.name}"/>
            <c:param name="action" value="${action}"/>
          </c:url>
        <view:arrayPane var="ViewSubscriptionOfTypeList" routingAddress="${arrayUrl}" numberLinesPerPage="25">
          <view:arrayColumn title="${subscriptionTypeLabel}" sortable="true"/>
          <view:arrayColumn title="${controller.getSubscriptionResourceTypeLabel(subResType)}"/>
          <c:if test="${not isReadOnly}">
            <view:arrayColumn title="${operationsLabel}" sortable="false"/>
          </c:if>
          <c:set var="validityCssClasses" value="${s -> s ? '' : 'ArrayCell disabled'}"/>
          <view:arrayLines var="subscription" items="${subscriptions}">
            <c:set var="isSubscriptionValid" value="${subscription.valid}"/>
            <fmt:message var="subscriptionTypeLabel" key="SubscriptionMethod.${subscription.subscriptionMethod}"/>
            <c:if test="${GROUP_SUBSCRIBER_TYPE eq subscription.subscriber.type}">
              <fmt:message var="subscriberTypeLabel" key="SubscriptionType.${subscription.subscriber.type}"/>
              <c:set var="subscriptionTypeLabel" value="${subscriptionTypeLabel} ${subscriberTypeLabel} <strong>${subscription.subscriberName}</strong>"/>
            </c:if>
            <view:arrayLine classes="${validityCssClasses(isSubscriptionValid)}">
              <view:arrayCellText text="${subscriptionTypeLabel}" classes="${validityCssClasses(isSubscriptionValid)}"/>
              <c:choose>
                <c:when test="${not isReadOnly and isSubscriptionValid}">
                  <view:arrayCellText>
                    <a class="sp-link" href="${subscription.link}">${silfn:escapeHtml(subscription.path)}</a>
                  </view:arrayCellText>
                </c:when>
                <c:otherwise>
                  <view:arrayCellText text="${subscription.path}" classes="${validityCssClasses(isSubscriptionValid)}"/>
                </c:otherwise>
              </c:choose>
              <c:if test="${not isReadOnly and not subscription.readOnly}">
                <view:arrayCellCheckbox name="subscriptionCheckbox"
                                        checked="false"
                                        value="${subscription.resource.id}-${subscription.resource.instanceId}-${subscription.creatorId}"/>
              </c:if>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>
        </form>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>