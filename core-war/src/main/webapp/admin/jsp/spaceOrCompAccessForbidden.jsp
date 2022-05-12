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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="cause" value="${requestScope['Cause']}"/>

<view:sp-page>
  <fmt:setLocale value="${requestScope.userLanguage}"/>
  <view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
  <fmt:message var="errorMsg" key="GML.ForbiddenAccessContent"/>
  <fmt:message var="backMsg" key="GML.back"/>
  <view:sp-head-part/>
  <view:sp-body-part>
    <view:window>
      <view:frame>
        <div class="inlineMessage-nok">
          <h3>
            <c:if test="${not empty cause}">
              <c:out value="${cause}"/>
            </c:if>
            <c:if test="${empty cause}">
              ${errorMsg}
            </c:if>
          </h3>
        </div>
        <view:buttonPane>
          <view:button label="${backMsg}" action="javascript:backToHomePage()"/>
        </view:buttonPane>
      </view:frame>
    </view:window>

    <script type="application/javascript">
      function backToHomePage() {
        if (window.spWindow) {
          spWindow.loadHomePage();
        }
      }

      whenSilverpeasReady().then(function() {
        if (window.spLayout) {
          spLayout.getHeader().load();
          let nav = spLayout.getBody().getNavigation();
          if (nav.isShown()) {
            nav.load();
          }
        }
      });
    </script>
  </view:sp-body-part>
</view:sp-page>