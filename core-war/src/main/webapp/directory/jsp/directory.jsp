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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="DIRECTORY_DEFAULT" constant="org.silverpeas.web.directory.control.DirectorySessionController.DIRECTORY_DEFAULT"/>
<view:setConstant var="DIRECTORY_DOMAIN" constant="org.silverpeas.web.directory.control.DirectorySessionController.DIRECTORY_DOMAIN"/>
<view:setConstant var="SORT_PERTINENCE" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_PERTINENCE"/>
<view:setConstant var="SORT_ALPHA" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_ALPHA"/>
<view:setConstant var="SORT_NEWEST" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_NEWEST"/>
<view:setConstant var="VIEW_ALL" constant="org.silverpeas.web.directory.control.DirectorySessionController.VIEW_ALL"/>
<view:setConstant var="VIEW_CONNECTED" constant="org.silverpeas.web.directory.control.DirectorySessionController.VIEW_CONNECTED"/>

<c:set var="breadcrumb" value="${requestScope.BreadCrumb}"/>
<c:set var="pagination" value="${requestScope.pagination}"/>
<c:set var="paginationCounter" value="${requestScope.paginationCounter}"/>
<c:set var="fragments" value="${requestScope.UserFragments}"/>
<c:set var="userTotalNumber" value="${requestScope.userTotalNumber}"/>
<c:set var="query" value="${silfn:escapeHtml(requestScope.Query)}"/>
<c:set var="sort" value="${requestScope.Sort}"/>
<c:set var="scope" value="${requestScope.Scope}"/>
<c:set var="view" value="${silfn:defaultString(requestScope.View, VIEW_ALL)}"/>
<c:set var="showHelp" value="${requestScope.ShowHelp}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <view:includePlugin name="invitme"/>
  <view:includePlugin name="messageme"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery.cookie.js"/>"></script>
  <script type="text/javascript">
    function viewIndex(index) {
      $.progressMessage();
      location.href = index;
    }

    function doPagination(page) {
      $.progressMessage();
      location.href = "Pagination?Index=" + page;
    }

    function isTermOK(term) {
      var firstCharacter = term.substring(0, 1);
      // Lucene cannot parse a query starting with '*' or '?'. This characters are not allowed as first character in WildcardQuery.
      return (firstCharacter != "*" && firstCharacter != "?");
    }

    function search() {
      var query = $("#searchField").val();
      var terms = query.split(" ");
      var queryOK = true;
      for (var i = 0; queryOK && i < terms.length; i++) {
        queryOK = isTermOK(terms[i]);
      }
      if (!queryOK) {
        $("#dialog-message").dialog("open");
      } else {
        $.progressMessage();
        $(document.search).submit();
      }
    }

    function sort(val) {
      $.progressMessage();
      location.href = "Sort?Type=" + val;
    }

    function showAutoHelp() {
      var helpCookieName = "Silverpeas_Directory_Help";
      var helpCookieValue = $.cookie(helpCookieName);
      if ("IKnowIt" != helpCookieValue) {
        $("#help-message").dialog({
          modal : true,
          resizable : false,
          width : 570,
          dialogClass : 'help-modal-message',
          buttons : {
            "<fmt:message key="GML.help.cookie.buttons.ok" />" : function() {
              $.cookie(helpCookieName, "IKnowIt", { expires : 3650, path : '/' });
              $(this).dialog("close");
            },
            "<fmt:message key="GML.help.cookie.buttons.remind" />" : function() {
              $(this).dialog("close");
            }
          }
        });
      }
    }

    function showHelp() {
      $('#help-message').popup('help', {
        title : "<fmt:message key="directory.help.title"/>"
      });
    }

    $(function() {
      $("#dialog-message").dialog({
        modal : true,
        autoOpen : false,
        width : 350,
        resizable : false,
        buttons : {
          Ok : function() {
            $(this).dialog("close");
          }
        }
      });

      $("#searchField").keypress(function(e) {
        if (e.which == 13) {
          e.preventDefault();
          search();
          return false;
        }
        return true;
      });

      <c:if test="${showHelp}">
      $(document).ready(function() {
        showAutoHelp();
      });
      </c:if>
    });
  </script>

</head>
<body id="directory">
<view:browseBar extraInformations="${breadcrumb}"/>
<view:window>
  <view:frame>
    <div id="indexAndSearch">

      <div id="search">
        <form name="search" action="searchByKey" method="post">
          <input type="text" name="key" id="searchField" size="40" maxlength="60" value="${query}"/>
          <fmt:message key="GML.search" var="buttonLabel"/>
          <view:button label="${buttonLabel}" action="javascript:search()"/>
        </form>
        <span id="help"><a href="javascript:onclick=showHelp()"><fmt:message key="GML.help"/></a></span>
      </div>

      <div id="index">
        <c:set var="alphabet" value="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"/>
        <c:forTokens var="letter" items="${alphabet}" delims=",">
          <c:set var="indexCSS" value="${(letter eq view) ? 'class=\"active\"' : ''}"/>
          <a ${indexCSS} href="javascript:viewIndex('${letter}')">${letter} </a>
        </c:forTokens>
        <c:out value=" - "/>
        <c:set var="indexCSS" value="${(VIEW_ALL eq view) ? 'class=\"active\"' : ''}"/>
        <a ${indexCSS} href="javascript:viewIndex('${VIEW_ALL}')"><fmt:message key="directory.scope.all"/></a>
        <c:out value=" - "/>
        <c:set var="indexCSS" value="${(VIEW_CONNECTED eq view) ? 'class=\"active\"' : ''}"/>
        <a ${indexCSS} href="javascript:viewIndex('${VIEW_CONNECTED}')"><fmt:message key="directory.scope.connected"/></a>
      </div>

      <c:if test="${(scope == DIRECTORY_DEFAULT or scope == DIRECTORY_DOMAIN) and !(SORT_PERTINENCE == sort)}">
        <div id="sort">
          <fmt:message key="directory.sort"/>
          <c:set var="indexCSS" value="${(SORT_ALPHA eq sort) ? 'class=\"active\"' : ''}"/>
          <a ${indexCSS} href="javascript:sort('${SORT_ALPHA}')"><fmt:message key="directory.sort.alpha"/></a>
          <c:out value=" - "/>
          <c:set var="indexCSS" value="${(SORT_NEWEST eq sort) ? 'class=\"active\"' : ''}"/>
          <a ${indexCSS} href="javascript:sort('${SORT_NEWEST}')"><fmt:message key="directory.sort.newest"/></a>
        </div>
      </c:if>
    </div>
    <c:choose>
      <c:when test="${empty fragments}">
        <div class="inlineMessage">
          <fmt:message key="directory.result.none"/>
        </div>
        <br clear="all"/>
      </c:when>
      <c:otherwise>
        <div class="ArrayNavigation" align="center">
          <fmt:message key="directory.result.some" var="paginationCounterSuffix">
            <fmt:param value="${fn:length(fragments)}"/>
          </fmt:message>
            ${paginationCounter} ${paginationCounterSuffix}
        </div>
        <div id="users">
          <ol class="message_list aff_colonnes">
            <c:forEach items="${fragments}" var="fragment">
              <li class="intfdcolor ${fragment.type}" id="user-${fragment.userId}">
                  ${fragment.fragment}
                <br clear="all"/>
              </li>
            </c:forEach>
          </ol>
          <div id="pagination">
              ${pagination.printIndex('doPagination')}
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </view:frame>

</view:window>

<view:progressMessage/>

<div id="dialog-message" title="<fmt:message key="directory.query.error.title"/>">
  <fmt:message key="directory.query.error.msg"/>
</div>

<div id="help-message" title="<fmt:message key="directory.help.title"/>" style="display: none;" class="help-modal-message">
  <view:applyTemplate locationBase="core:directory" name="help"/>
</div>
</body>
</html>