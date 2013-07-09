<%--

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

<%
    response.setHeader("Cache-Control","no-store"); //HTTP 1.1
    response.setHeader("Pragma","no-cache");        //HTTP 1.0
    response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.pdcSubscriptionPeas.multilang.pdcSubscriptionBundle" />
<c:set var="isNewSubscription" value="${requestScope.IsNewPDCSubscription}"/>
<c:set var="subscription" value="${requestScope.PDCSubscription}"/>
<c:set var="subscriptionName" value="${requestScope.PDCSubscriptionName}"/>

<fmt:message key="Path" var="path"/>
<fmt:message key="GML.ok" var="okLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>
<fmt:message key="pdcSubscription.Name.NotEmpty" var="invalidName"/>
<fmt:message key="pdcSubscription.Values.NotEmpty" var="invalidValues"/>
<fmt:message key="pdcSubscription.Update" var="updateSubscription"/>
<fmt:message key="pdcSubscription.New" var="newSubscription"/>

<c:choose>
<c:when test="${!isNewSubscription}">
	<view:browseBar extraInformations="${updateSubscription}">
	<view:browseBarElt label="${path}" link="subscriptionList.jsp"></view:browseBarElt>
	</view:browseBar>
</c:when>
<c:otherwise>
	<view:browseBar extraInformations="${newSubscription}">
		<view:browseBarElt label="${path}" link="subscriptionList.jsp"></view:browseBarElt>
	</view:browseBar>
</c:otherwise>
</c:choose>

<html>
  <head>
    <view:looknfeel/>
    <view:includePlugin name="pdc"/>
  </head>
  <body>
    <view:window>
    <view:frame>
        <form id="PdcSubscription" name="PdcSubscription" action="addSubscription" method="POST">
          <input type="hidden" name="AxisValueCouples"/>
            <view:board>
              <span class="txtlibform"><fmt:message key="pdcSubscription.Name"/>&nbsp;:</span>
              <input type="text" name="SubscriptionName" size="50" maxlength="100" value="${subscriptionName}"/>
            </view:board>
            <view:board>
                <fieldset id="used_pdc" class="skinFieldset"></fieldset>
            </view:board>
            <view:buttonPane>
              <view:button label="${okLabel}" action="javascript:sendSubscription()"/>
              <view:button label="${cancelLabel}" action="javascript:goBack()"/>
            </view:buttonPane>
        </form>
    </view:frame>
    </view:window>
    <script type="text/javascript">
      var values = [];
      <c:if test="${subscription != null}">
        <c:forEach var="criterion" items="${subscription.pdcContext}">
          values.push({ axisId: ${criterion.axisId}, id: "${criterion.value}" });
        </c:forEach>
      </c:if>
      $('#used_pdc').pdc('all', {
        values: values
      });

      function sendSubscription() {
        var name = $('input[name="SubscriptionName"]').val();
        var values = $('#used_pdc').pdc('selectedValues');
        if (!name) {
          alert('<view:encodeJs string="${invalidName}"/>');
          return;
        }
        if (values.length === 0) {
          alert('<view:encodeJs string="${invalidValues}"/>');
          return;
        }
        $('input[name="AxisValueCouples"]').val(values.flatten());
      <c:if test="${!isNewSubscription}">
        $('#PdcSubscription').attr('action', 'updateSubscription');
      </c:if>
        $('#PdcSubscription').submit();
      }

      function goBack() {
        $('#PdcSubscription').attr('action', 'subscriptionList');
        $('#PdcSubscription').submit();
      }
    </script>
  </body>
</html>
