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

<%
    response.setHeader("Cache-Control","no-store"); //HTTP 1.1
    response.setHeader("Pragma","no-cache");        //HTTP 1.0
    response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.pdcSubscriptionPeas.multilang.pdcSubscriptionBundle" />
<c:set var="isNewSubscription" value="${requestScope.IsNewPDCSubscription}"/>
<c:set var="positions" value="${requestScope.PdcSubscriptionPositions}"/>
<c:set var="subscriptionName" value="${silfn:escapeHtml(requestScope.PDCSubscriptionName)}"/>
<c:set var="path" value="${requestScope.path}"/>
<c:set var="context" value="${requestScope.context}"/>
<c:set var="isForced" value="${context eq 'pdc'}"/>
<c:set var="subscribedUsers" value="${requestScope.SubscribedUsers}"/>
<c:set var="subscribedGroups" value="${requestScope.SubscribedGroups}"/>

<fmt:message key="GML.ok" var="okLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>
<fmt:message key="pdcSubscription.Name.NotEmpty" var="invalidName"/>
<fmt:message key="pdcSubscription.Values.NotEmpty" var="invalidValues"/>
<fmt:message key="${isForced ? 'pdcSubscription.UpdateForced' : 'pdcSubscription.Update'}"
             var="updateSubscription"/>
<fmt:message key="${isForced ? 'pdcSubscription.NewForced' : 'pdcSubscription.New'}"
             var="newSubscription"/>

<c:url var="subscriptionListUrl" value="ViewSubscriptionTaxonomy">
  <c:param name="context" value="${context}"/>
  <c:param name="scope" value="${requestScope.scope}"/>
</c:url>
<c:choose>
<c:when test="${!isNewSubscription}">
	<view:browseBar extraInformations="${updateSubscription}">
	    <view:browseBarElt label="${path}" link="${subscriptionListUrl}"/>
	</view:browseBar>
</c:when>
<c:otherwise>
	<view:browseBar extraInformations="${newSubscription}">
		<view:browseBarElt label="${path}" link="${subscriptionListUrl}"/>
	</view:browseBar>
</c:otherwise>
</c:choose>

<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="pdc"/>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:window>
    <view:frame>
        <form id="PdcSubscription" name="PdcSubscription" action="addSubscription" method="POST">
          <input type="hidden" name="AxisValueCouples"/>
          <input type="hidden" name="context" value="${context}"/>
          <input type="hidden" name="scope" value="${requestScope.scope}"/>
            <view:board>
              <span class="txtlibform"><fmt:message key="pdcSubscription.Name"/>&nbsp;:</span>
              <input type="text" name="SubscriptionName" size="50" maxlength="100" value="${subscriptionName}"/>
            </view:board>
            <view:board>
                <fieldset id="used_pdc" class="skinFieldset"></fieldset>
            </view:board>
            <c:if test="${isForced}">
              <view:board>
                <span class="txtlibform"><fmt:message key="pdcSubscription.Subscribers"/>&nbsp;:</span>
                <c:choose>
                  <c:when test="${empty subscribedUsers and empty subscribedGroups}">
                    <span class="txtnote"><fmt:message key="pdcSubscription.NoSubscriber"/></span>
                  </c:when>
                  <c:otherwise>
                    <ul class="subscribers">
                      <c:forEach var="group" items="${subscribedGroups}">
                        <li class="group">${silfn:escapeHtml(group.name)}</li>
                      </c:forEach>
                      <c:forEach var="user" items="${subscribedUsers}">
                        <li class="user">${silfn:escapeHtml(user.displayedName)}</li>
                      </c:forEach>
                    </ul>
                  </c:otherwise>
                </c:choose>
                <fmt:message key="pdcSubscription.UpdateSubscribers" var="updateSubscribers"/>
                <view:button label="${updateSubscribers}" action="javascript:selectSubscribers()"/>
              </view:board>
            </c:if>
            <view:buttonPane>
              <view:button label="${okLabel}" action="javascript:sendSubscription()"/>
              <view:button label="${cancelLabel}" action="javascript:goBack()"/>
            </view:buttonPane>
        </form>
    </view:frame>
    </view:window>
    <script type="text/javascript">
      const values = [];
      <c:forEach var="criterion" items="${positions}">
        values.push({ axisId: ${criterion.axisId}, id: "${criterion.value}" });
      </c:forEach>
      $('#used_pdc').pdc('all', {
        values: values
      });

      function collectPositions() {
        $('input[name="AxisValueCouples"]').val(
            $('#used_pdc').pdc('selectedValues').flatten());
      }

      function sendSubscription() {
        const name = $('input[name="SubscriptionName"]').val();
        const values = $('#used_pdc').pdc('selectedValues');
        if (!name) {
          jQuery.popup.error('<view:encodeJs string="${invalidName}"/>');
          return;
        }
        if (values.length === 0) {
          jQuery.popup.error('<view:encodeJs string="${invalidValues}"/>');
          return;
        }
        collectPositions();
      <c:if test="${!isNewSubscription}">
        $('#PdcSubscription').attr('action', 'updateSubscription');
      </c:if>
        $('#PdcSubscription').submit();
      }

      <c:if test="${isForced}">
      function selectSubscribers() {
        // the edition in progress is kept in the session before leaving this screen for the user
        // panel from which the subscribers are designated. It isn't validated here so that the
        // subscribers can be designated at any moment of the edition.
        collectPositions();
        $('#PdcSubscription').attr('action', 'ToUserPanel');
        $('#PdcSubscription').submit();
      }
      </c:if>

      function goBack() {
        $('#PdcSubscription').attr('action', 'ViewSubscriptionTaxonomy');
        $('#PdcSubscription').submit();
      }
    </script>
  </view:sp-body-part>
</view:sp-page>