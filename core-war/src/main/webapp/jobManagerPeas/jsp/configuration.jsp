<%--

 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.


--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="view" uri="silverpeas.tags.viewGenerator" %>
<%
  MainSessionController main = (MainSessionController) session.getAttribute("SilverSessionController");
  response.setHeader("Cache-Control", "no-store");
  response.setHeader("Pragma", "no-cache");
  if (main == null || !main.getCurrentUserDetail().isAccessAdmin()) {
    response.sendError(403);
    return;
  }
%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.jobManagerPeas.multilang.configurationConsole"/>
<c:url var="configurationApi" value="/services/admin/configuration"/>
<!DOCTYPE html>
<html lang="${sessionScope['SilverSessionController'].favoriteLanguage}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><fmt:message key="title"/></title>
  <view:looknfeel/>
  <link rel="stylesheet" href="<c:url value='/jobManagerPeas/css/configuration.css'/>">
  <script src="<c:url value='/jobManagerPeas/js/configuration.js'/>" defer></script>
</head>
<body>
<main id="configuration-console" data-api="${configurationApi}">
  <header class="configuration-heading">
    <h1><fmt:message key="title"/></h1>
    <p><fmt:message key="intro"/></p>
  </header>
  <p id="configuration-message" role="status" aria-live="polite"></p>
  <div class="configuration-layout">
    <nav class="configuration-files" aria-label="<fmt:message key='files'/>">
      <label for="configuration-file-filter"><fmt:message key="files"/></label>
      <input id="configuration-file-filter" type="search" autocomplete="off">
      <div id="configuration-tree"></div>
    </nav>
    <section class="configuration-editor" aria-label="<fmt:message key='editor'/>">
      <h2 id="configuration-path"><fmt:message key="select"/></h2>
      <details id="configuration-file-help" hidden>
        <summary><fmt:message key="fileHelp"/></summary>
        <pre></pre>
      </details>
      <div class="configuration-filters">
        <label for="configuration-key-filter"><fmt:message key="filterKeys"/></label>
        <input id="configuration-key-filter" type="search" autocomplete="off" disabled>
        <label><input id="configuration-modified-only" type="checkbox" disabled>
          <fmt:message key="modifiedOnly"/>
        </label>
      </div>
      <div class="configuration-table-scroll">
        <table id="configuration-properties">
          <thead><tr>
            <th scope="col"><fmt:message key="key"/></th>
            <th scope="col"><fmt:message key="value"/></th>
            <th scope="col"><fmt:message key="actions"/></th>
          </tr></thead>
          <tbody></tbody>
        </table>
        <p id="configuration-empty"><fmt:message key="select"/></p>
      </div>
      <section id="configuration-conflict" hidden aria-label="<fmt:message key='conflict'/>">
        <h3><fmt:message key="conflict"/></h3>
        <p><fmt:message key="conflictExplanation"/></p>
        <button id="configuration-compare" type="button"><fmt:message key="compare"/></button>
        <div id="configuration-comparison" hidden>
          <table>
            <thead><tr>
              <th scope="col"><fmt:message key="key"/></th>
              <th scope="col"><fmt:message key="original"/></th>
              <th scope="col"><fmt:message key="current"/></th>
              <th scope="col"><fmt:message key="local"/></th>
            </tr></thead>
            <tbody></tbody>
          </table>
          <button id="configuration-rebase" type="button"><fmt:message key="rebase"/></button>
        </div>
      </section>
      <footer class="configuration-actions">
        <span id="configuration-count" aria-live="polite"></span>
        <button id="configuration-reload" type="button" disabled><fmt:message key="reload"/></button>
        <button id="configuration-cancel" type="button" disabled><fmt:message key="cancel"/></button>
        <button id="configuration-save" type="button" disabled><fmt:message key="save"/></button>
      </footer>
      <p class="configuration-application-note"><fmt:message key="applicationNote"/></p>
    </section>
  </div>
  <div id="configuration-messages" hidden>
    <c:forTokens var="key" items="select,loading,loadError,saveError,saved,modified,count,discard,help,reset,reveal,hide,noFiles,noKeys,conflict,conflictExplanation,rebased,removed,absent,forbidden,invalid,notFound" delims=",">
      <span data-message="${key}"><fmt:message key="${key}"/></span>
    </c:forTokens>
  </div>
</main>
</body>
</html>
