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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/admin/jcr" prefix="adminTags" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="wbeEnabled" value="${requestScope['wbeEnabled']}"/>
<c:set var="datastorePathView" value="${requestScope['datastorePathView']}"/>
<jsp:useBean id="datastorePathView" type="org.silverpeas.core.persistence.jcr.JcrDatastoreManager.DatastorePathView"/>
<c:set var="previousTask" value="${requestScope['previousTask']}"/>
<c:if test="${previousTask != null}">
  <jsp:useBean id="previousTask" type="org.silverpeas.core.persistence.jcr.JcrDatastoreTaskMonitor"/>
</c:if>
<c:set var="currentTask" value="${requestScope['currentTask']}"/>
<c:if test="${currentTask != null}">
  <jsp:useBean id="currentTask" type="org.silverpeas.core.persistence.jcr.JcrDatastoreTaskMonitor"/>
</c:if>
<c:set var="datastoreContentSize" value="${datastorePathView.contentSize}"/>
<c:set var="datastoreContentSizeAvailable" value="${datastoreContentSize.present}"/>
<c:set var="pooling" value="${not datastoreContentSizeAvailable or currentTask != null}"/>

<fmt:message var="actualSizeLabel" key="jcrmonitor.datatore.content.size"/>
<fmt:message var="pendingComputation" key="jcrmonitor.datatore.content.size.pending"/>
<fmt:message var="computeAgainLabel" key="jcrmonitor.datatore.content.size.computeAgain"/>
<fmt:message var="beforePurgeStartingMsg" key="jcrmonitor.datatore.content.size.beforePurge"/>
<fmt:message var="browseBarAll" key="jcrmonitor.breadcrumb"/>
<fmt:message var="forceGC" key="jcrmonitor.action.forceGC"/>
<fmt:message var="forceGCConfirm" key="jcrmonitor.action.forceGC.confirm"/>
<fmt:message var="noTaksMsg" key="jcrmonitor.noTasks"/>

<view:sp-page>
  <view:sp-head-part>
    <style>
      .jcr-datastore-size .sp_button::before {
        content: ' ';
        width: 16px;
        height: 17px;
        background: transparent url(/silverpeas/util/icons/arrow/refresh.png) no-repeat 0 0;
        display: inline-block;
        margin-right: 0.5em;
      }

      .jcr-datastore-size .sp_button {
        background-color: transparent;
        color: #1c94d4;
        text-decoration: underline;
      }

      .jcr-datastore-task {
        border: 2px solid #BBB;
        border-top: 20px solid #BBB;
        background-color: #000;
        color: #FFF;
        font-family: "Courier New", Verdana, Arial, sans-serif;
        width: 800px;
        margin: 1em;
      }

      .jcr-datastore-size .size {
        display: inline-block;
      }

      .jcr-datastore-size .size .value {
        width: 80px;
        height: 53px;
        border: 4px solid #ec9c01;
        border-radius: 50px;
        text-align: center;
        padding-top: 27px;
        font-size: 16px;
        white-space: nowrap;
        display: inline-block;
        background-color: #f9f8dc;
      }

      .jcr-datastore-size .size .value span {
        white-space: initial;
        font-size: 12px;
        line-height: 8px;
      }

      .jcr-datastore-task ul {
        padding: 0;
        margin: 2em;
      }

      .jcr-datastore-task ul li {
        list-style-type: none;
        width: 100%;
        padding: 0;
        margin: 0;
        white-space: normal;
      }

      .jcr-datastore-task ul .task-nb-processed-node {
        border-top: 1px solid #CCC;
        margin-top: 1em;
        padding: 1em 0;
      }

      .inlineMessage-neutral .path {
        font-family: "Courier New", Verdana, Arial, sans-serif;
        font-weight: bolder;
      }

      .jcr-datastore-size {
        margin: 1em;
      }

      .jcr-datastore-size .label {
        white-space: nowrap;
        padding-right: 10px;
        display: inline-block;
        font-size: 16px;
      }

      .jcr-datastore-task .task-status div:first-of-type {
        display: inline-flex;
        padding-bottom: 2px;
      }

      .jcr-datastore-task .task-status .date {
        white-space: nowrap;
        padding-right: 10px;
        color: #75af30;
        font-family: "Courier New", Verdana, Arial, sans-serif;
      }

      .jcr-datastore-task .task-status .report {
        color: #FFF;
        font-family: "Courier New", Verdana, Arial, sans-serif;
      }
    </style>
    <script type="text/javascript">
      function computeAgain() {
        return sp.ajaxRequest("ForceSizeComputing").byPostMethod().send().then(applyRefresh);
      }

      function forceGC() {
        jQuery.popup.confirm('${silfn:escapeJs(forceGCConfirm)}', function() {
          return sp.ajaxRequest("forceGC").byPostMethod().send().then(applyRefresh);
        });
      }

      function refresh() {
        return sp.ajaxRequest("Main").send().then(applyRefresh);
      }

      function applyRefresh(request) {
        sp.updateTargetWithHtmlContent('#topPage', request.responseText, true)
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="page_content_admin wbe_admin">
    <view:browseBar extraInformations="${browseBarAll}"/>
    <view:operationPane>
      <c:if test="${not pooling}">
        <view:operation action="javascript:forceGC()" icon="" altText="${forceGC}"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <view:frame>
        <div class="inlineMessage-neutral">
          <view:applyTemplate locationBase="core:admin/jcrmonitor" name="datastore_info">
            <view:templateParam name="wbeEnabled" value="${wbeEnabled}"/>
            <view:templateParam name="datastore_path" value="${datastorePathView.pathWithVariable}"/>
          </view:applyTemplate>
        </div>
        <div class="jcr-datastore-size">
          <div class="label">${actualSizeLabel}</div>
          <div class="size">
            <c:choose>
              <c:when test="${datastoreContentSizeAvailable}">
                <div class="value">
                    ${silfn:humanReadableSize(datastoreContentSize.get())}
                </div>
                <c:choose>
                  <c:when test="${not pooling}">
                    <a href="javascript:void(0)" class="sp_button" onclick="computeAgain()">${computeAgainLabel}</a>
                  </c:when>
                  <c:when test="${currentTask != null}">
                    <span>${beforePurgeStartingMsg}</span>
                  </c:when>
                </c:choose>
              </c:when>
              <c:otherwise><span>${pendingComputation}</span></c:otherwise>
            </c:choose>
          </div>
        </div>
        <c:if test="${previousTask == null && currentTask == null}">
          <div class="inlineMessage">${noTaksMsg}</div>
        </c:if>
        <c:if test="${previousTask != null}">
          <fmt:message var="previousMsg" key="jcrmonitor.previousTask.${previousTask.type}"/>
          <div class="inlineMessage">${previousMsg}</div>
          <adminTags:jcrDatastoreTaskContent task="${previousTask}"/>
        </c:if>
        <c:if test="${currentTask != null}">
          <fmt:message var="runningMsg" key="jcrmonitor.runningTask.${currentTask.type}"/>
          <div class="inlineMessage-ok">${runningMsg}</div>
          <adminTags:jcrDatastoreTaskContent task="${currentTask}"/>
        </c:if>
        <c:if test="${pooling}">
          <script type="text/javascript">
            whenSilverpeasReady(function() {
              setTimeout(refresh, 10000);
            });
          </script>
        </c:if>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
