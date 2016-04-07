<%@ page import="org.silverpeas.core.util.logging.Level" %>
<%@ page import="org.silverpeas.core.util.logging.LoggerConfigurationManager" %>
<%@ page import="org.silverpeas.core.util.logging.LogsAccessor" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%--

    Copyright (C) 2000 - 2015 Silverpeas

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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server

  List<String> loggingLevels = new ArrayList<>();
  for (Level level : Level.values()) {
    loggingLevels.add(level.name());
  }
  pageContext.setAttribute("loggingLevels", loggingLevels, PageContext.PAGE_SCOPE);
  pageContext.setAttribute("configurations",
      LoggerConfigurationManager.get().getAvailableLoggerConfigurations(), PageContext.PAGE_SCOPE);
  pageContext.setAttribute("logs", LogsAccessor.get().getAllLogs(), PageContext.PAGE_SCOPE);
%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.util.logging.multilang.loggingAdmin"/>
<fmt:message key="GML.update" var="validate"/>
<fmt:message key="logging.admin.PrintLogContent" var="display"/>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title><fmt:message key="logging.admin.Title"/></title>
  <view:looknfeel withFieldsetStyle="true"/>
  <view:includePlugin name="qtip"/>
  <script type="application/javascript">
    var DEFAULT_LEVEL_TEXT = "<fmt:message key='logging.admin.DefaultLevel'/>";
    var LEVEL_TEXT = "<fmt:message key='logging.admin.level'/> ";
    var loggerConfigurations = {
    <c:forEach var="configuration" items="${configurations}">
      <c:set var="level" value="${configuration.level}"/>
      <c:if test="${configuration.level == null}">
        <c:set var="level" value="PARENT"/>
      </c:if>
      '${configuration.namespace}': {
        uri: webContext + '/services/logging/${configuration.namespace}/configuration',
        logger: '${configuration.namespace}',
        level: '${level}'
      },
    </c:forEach>
      configurationOf: function(logger) {
        return this[logger];
      }
    };

    function changeLoggingLevel() {
      var logger = $('#logger').val();
      var config = loggerConfigurations.configurationOf(logger);
      config.level = $('#level').val();
      $.ajax({
        url : config.uri,
        type : 'PUT',
        data : JSON.stringify(config),
        contentType : 'application/json',
        dataType : 'json',
        success : function(configuration) {
          var $item = $('#logger').find('option[value="' + logger + '"]');
          loggerConfigurations[logger] = configuration;
          var level;
          if (configuration.level === 'PARENT') {
            level = DEFAULT_LEVEL_TEXT;
            $item.addClass('default');
          } else {
            level = LEVEL_TEXT + configuration.level;
            $item.removeClass('default');
          }
          $item.text(configuration.logger + ' (' + level + ')');
          notySuccess("<fmt:message key='logging.admin.updateSuccess'/>");
        },
        error : function(xhr, status, error) {
          notyError("<fmt:message key='logging.admin.updateError'/>" + ' ' + error);
        }
      });
    }

    function fetchLastLogRecords() {
      var log = $('#log').val();
      var count = $('#record-count').val();
      $.ajax({
        url : webContext + '/services/logging/logs/' + log + '?count=' + count,
        type : 'GET',
        dataType : 'json',
        success : function(records) {
          var size = records.length > 50 ? 50:records.length;
          var $logContent = $('#log-content');
          $logContent.show();
          $logContent.attr('rows', '' + size).text(records.join('\n'));
          $logContent.scrollTop($logContent[0].scrollHeight);
        },
        error : function(xhr, status, error) {
          notyError("<fmt:message key='logging.admin.LogError'/>" + ' ' + error);
        }
      });
    }
  </script>
</head>
<body>
<view:window>
  <view:frame>
    <form id="logger-level-setting">
      <fieldset class="skinFieldset parameters">
        <legend><fmt:message key="logging.admin.LoggingLevelChange"/></legend>
        <ul class="fields">
          <li class="group-field">
            <p class="group-field-description">
              <fmt:message key="logging.admin.help.Loggers"/>
            </p>
          </li>
          <li class="field"  id="logger-configurations">
            <label class="txtlibform" for="logger"><fmt:message key="logging.admin.Loggers"/></label>
            <div class="champs">
              <select id="logger" size="15">
              <c:forEach var="configuration" items="${configurations}">
                <c:choose>
                  <c:when test="${configuration.level == null}">
                    <option class="default" value="${configuration.namespace}">${configuration.namespace} (<fmt:message key="logging.admin.DefaultLevel"/>)</option>
                  </c:when>
                  <c:otherwise>
                    <option value="${configuration.namespace}">${configuration.namespace} (<fmt:message key="logging.admin.level"/> ${configuration.level})</option>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
              </select>
            </div>
          </li>
          <li class="field" id="logging-level">
            <c:url var="infoIcon" value="/util/icons/info.gif"/>
            <img id="level-help" data-hasqtip="true" src="${infoIcon}" class="parameterInfo" />
            <label class="txtlibform" for="level"><fmt:message key="logging.admin.LoggingLevel"/></label>
            <div class="champs">
              <select id="level">
                <option value="PARENT"><fmt:message key="logging.admin.DefaultLevel"/></option>
                <c:forEach var="levelValue" items="${loggingLevels}">
                  <option value="${levelValue}">${levelValue}</option>
                </c:forEach>
              </select>
            </div>
          </li>
        </ul>
        <view:button label="${validate}" action="javascript:changeLoggingLevel();"/>
      </fieldset>
    </form>

    <form id="logging-content">
      <fieldset class=" skinFieldset parameters">
        <legend><fmt:message key="logging.admin.LogContent"/></legend>
        <ul class="fields">
          <li class="field" id="select-log">
            <label class="txtlibform"  for="log"><fmt:message key="logging.admin.Logs"/></label>
            <div class="champs">
              <select id="log">
              <c:forEach var="log" items="${logs}">
                <option value="${log}">${log}</option>
              </c:forEach>
              </select>
            </div>
          </li>
          <li class="field"  id="choice-record-count">
            <img id="record-count-help" data-hasqtip="true" src="${infoIcon}" class="parameterInfo" />
            <label class="txtlibform" for="record-count"><fmt:message key="logging.admin.LogRecordCount"/></label>
            <div class="champs">
              <input id="record-count" type="number" min="0" value="100">
            </div>
          </li>
          <li class="field"  id="btn-show-log-content">
            <view:button label="${display}" action="javascript:fetchLastLogRecords();"/>
          </li>
        </ul>
        <div id="champs"><textarea id="log-content" readonly="readonly" rows="10"></textarea></div>
      </fieldset>
    </form>
  </view:frame>
</view:window>
<script type="application/javascript">
  $(document).ready(function() {
    function updateDefaultLevelState(logger) {
      var $parentOptions = $('#level').find('option[value="PARENT"]');
      if (logger === 'silverpeas') {
        $parentOptions.attr('disabled', 'disabled');
      } else {
        $parentOptions.removeAttr('disabled');
      }
    }

    TipManager.simpleHelp($('#level-help'), "<fmt:message key='logging.admin.help.LoggingLevels'/>");
    TipManager.simpleHelp($('#record-count-help'), "<fmt:message key='logging.admin.help.LogRecordCount'/>");

    var $logger = $('#logger');
    var namespace = $logger.find('option:first-child').attr('selected', 'selected').val();
    $('#level').val(loggerConfigurations.configurationOf(namespace).level);
    updateDefaultLevelState(namespace);

    $logger.change(function() {
      var namespace = $(this).val();
      $('#level').val(loggerConfigurations.configurationOf(namespace).level);
      updateDefaultLevelState(namespace);
    });

    $('#logger-level-setting').submit(function() {
      changeLoggingLevel();
      return false;
    });
    $('#logging-content').submit(function() {
      fetchLastLogRecords();
      return false;
    });
  });
</script>
</body>
</html>
