<%--

    Copyright (C) 2000 - 2020 Silverpeas

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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="DIRECTORY_DEFAULT" constant="org.silverpeas.web.directory.control.DirectorySessionController.DIRECTORY_DEFAULT"/>
<view:setConstant var="DIRECTORY_DOMAIN" constant="org.silverpeas.web.directory.control.DirectorySessionController.DIRECTORY_DOMAIN"/>
<view:setConstant var="DIRECTORY_MINE" constant="org.silverpeas.web.directory.control.DirectorySessionController.DIRECTORY_MINE"/>
<view:setConstant var="SORT_PERTINENCE" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_PERTINENCE"/>
<view:setConstant var="SORT_ALPHA" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_ALPHA"/>
<view:setConstant var="SORT_NEWEST" constant="org.silverpeas.web.directory.control.DirectorySessionController.SORT_NEWEST"/>
<view:setConstant var="VIEW_ALL" constant="org.silverpeas.web.directory.control.DirectorySessionController.VIEW_ALL"/>
<view:setConstant var="VIEW_CONNECTED" constant="org.silverpeas.web.directory.control.DirectorySessionController.VIEW_CONNECTED"/>

<c:set var="breadcrumb" value="${requestScope.BreadCrumb}"/>
<c:set var="fragments" value="${requestScope.UserFragments}"/>
<c:set var="memberPage" value="${requestScope.memberPage}"/>
<jsp:useBean id="memberPage" type="org.silverpeas.core.admin.PaginationPage"/>
<c:set var="userTotalNumber" value="${requestScope.userTotalNumber}"/>
<c:set var="query" value="${requestScope.Query}"/>
<c:set var="sort" value="${requestScope.Sort}"/>
<c:set var="scope" value="${requestScope.Scope}"/>
<c:set var="view" value="${silfn:defaultString(requestScope.View, VIEW_ALL)}"/>
<c:set var="showHelp" value="${requestScope.ShowHelp}"/>
<c:set var="quickUserSelectionEnabled" value="${requestScope.QuickUserSelectionEnabled}"/>
<c:set var="domains" value="${requestScope.Domains}"/>
<c:set var="groups" value="${requestScope.Groups}"/>
<c:set var="sources" value="${requestScope.DirectorySources}"/>

<c:set var="currentSource" value=""/>
<c:set var="currentSourceCSS" value=""/>
<c:forEach var="source" items="${sources}">
  <c:if test="${source.selected}">
    <c:set var="currentSource" value="${source}"/>
    <c:set var="currentSourceCSS" value="source-${source.id}"/>
  </c:if>
</c:forEach>

<fmt:message key="GML.print" var="labelPrint"/>

<%
  Form extraForm = (Form) request.getAttribute("ExtraForm");
  PagesContext extraFormContext = (PagesContext) request.getAttribute("ExtraFormContext");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <view:includePlugin name="popup"/>
  <link type="text/css" rel="stylesheet" href='<c:url value="/directory/jsp/css/print.css" />' media="print"/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery.cookie.js"/>"></script>
  <% if (extraForm != null) {
    extraForm.displayScripts(out, extraFormContext);
  }
  %>
  <script type="text/javascript">
    function viewIndex(index) {
      spProgressMessage.show();
      location.href = index;
    }

    function isTermOK(term) {
      var firstCharacter = term.substring(0, 1);
      var lastChar = term.substring(term.length-1, term.length);
      // Lucene cannot parse a query starting with '*' or '?'. This characters are not allowed as first character in WildcardQuery.
      // Lucene cannot parse a query ending with '!'. This characters are not allowed as last character in WildcardQuery.
      return firstCharacter != "*" && firstCharacter != "?" && lastChar != "!";
    }

    function search() {
      var query = $("#select-user-group-directory input").val();
      if (!query) {
        // in case of quick user selection is not enabled
        query = $("#searchField").val();
      }
      var queryOK = true;
      if (query) {
        var terms = query.split(" ");
        for (var i = 0; queryOK && i < terms.length; i++) {
          queryOK = isTermOK(terms[i]);
        }
      }
      if (!queryOK) {
        $("#dialog-message").dialog("open");
      } else {
        spProgressMessage.show();
        $(document.search).submit();
      }
    }

    function jumpToUser(selectionUserAPI) {
      var userIds = selectionUserAPI.getSelectedUserIds();
      if (userIds.length) {
        location.href = webContext+"/Rprofil/jsp/Main?userId="+userIds[0];
      }
    }

    function clear() {
      spProgressMessage.show();
      location.href = "Clear";
    }

    function sort(val) {
      spProgressMessage.show();
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
              $.cookie(helpCookieName, "IKnowIt", { expires : 3650, path : '/', secure: ${pageContext.request.secure} });
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

    function showContact(url) {
      jQuery.popup.load(url).show('free', {
        title : 'Contact'
      });
    }

    <c:if test="${(scope == DIRECTORY_MINE)}">
    function deleteRelationCallback(withUser) {
      spProgressMessage.show();
      let explodedUrl = sp.url.explode(location.href);
      explodedUrl.base = '<c:url value="/Rdirectory/jsp/RemoveUserFromLists"/>';
      explodedUrl.parameters['UserId'] = withUser.id;
      sp.ajaxRequest(sp.url.formatFromExploded(explodedUrl)).loadTarget('#myContacts', true).then(function() {
        spProgressMessage.hide();
        activateUserZoom();
      });
    }
    </c:if>

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

      $("#indexAndSearch").keypress(function(e) {
        if (e.which == 13) {
          e.preventDefault();
          search();
          return false;
        }
        return true;
      });

      $("#sources select").change(function() {
        spProgressMessage.show();
        location.href = "LimitTo?SourceId="+$(this).val();
      });

      var flip = 0;

      $(document).ready(function() {
        <c:if test="${showHelp}">
          whenSilverpeasEntirelyLoaded().then(function() {
            setTimeout(showAutoHelp, 0);
          });
        </c:if>

        // hide all extra fields by default
        $("#extraForm .field").hide();
        $("#extraForm .sp_button").hide();
        // show fields according to Javascript file of specific form
        try {
          callbackShowExtraFields();
        } catch (e) {}
        // hide/show other fields
        $("#advanced").click(function() {
          if (flip%2 === 1){
            $("#extraForm li").not(".alwaysShown").hide();
            $("#extraForm .sp_button").hide();
            $("a#advanced span").text("<fmt:message key="GML.search.advanced"/>");
            $("a#advanced").removeClass("simple").addClass("advanced");
          } else {
            $("#extraForm li").not(".alwaysShown").show();
            $("#extraForm .sp_button").show();
            $("a#advanced span").text("<fmt:message key="GML.search.simple"/>");
            $("a#advanced").removeClass("advanced").addClass("simple");
          }
          flip++;
        });
      });
    });
  </script>

</head>

<body id="directory" class="${currentSourceCSS}">
<view:browseBar extraInformations="${breadcrumb}"/>
<view:operationPane>
  <view:operation action="javascript:window.print()" altText="${labelPrint}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="indexAndSearch">
      <form name="search" action="searchByKey" method="get">
      <div id="search">
        <c:choose>
          <c:when test="${not quickUserSelectionEnabled}">
            <input type="text" name="key" id="searchField" size="40" maxlength="60" value="${query}"/>
          </c:when>
          <c:otherwise>
            <viewTags:selectUsersAndGroups selectionType="USER" noUserPanel="true" noSelectionClear="true"
                                           doNotSelectAutomaticallyOnDropDownOpen="true"
                                           queryInputName="key" id="directory" initialQuery="${query}"
                                           navigationalBehavior="true" onChangeJsCallback="jumpToUser"
                                           domainsFilter="${domains}" groupsFilter="${groups}"/>
          </c:otherwise>
        </c:choose>
        <fmt:message key="GML.search" var="buttonLabel"/>
        <view:button label="${buttonLabel}" action="javascript:search()"/>
        <% if (extraForm != null) { %>
          <a href="#" id="advanced" class="advanced"><span><fmt:message key="GML.search.advanced"/></span></a>
          <a href="javascript:onclick=clear()" id="clear"><span><fmt:message key="GML.search.clear"/></span></a>
        <% } %>
        <a href="javascript:onclick=showHelp()" id="help"><span><fmt:message key="GML.help"/></span></a>
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

      <c:if test="${not empty sources && fn:length(sources)>1}">
        <div id="sources">
          Voir :
          <select name="SourceId">
            <option value="-1">Tout</option>
            <c:forEach var="source" items="${sources}">
              <c:choose>
                <c:when test="${source.selected}">
                  <option value="${source.id}" selected="selected">${source.label}</option>
                </c:when>
                <c:otherwise>
                  <option value="${source.id}">${source.label}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
      </c:if>

      <% if (extraForm != null) { %>
      <div id="extraForm">
        <%
          extraForm.display(out, extraFormContext);
        %>
        <div id="extraForm-button"><view:button label="${buttonLabel}" action="javascript:search()" /></div>
      </div>
      <% } %>

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
      </form>
    </div>
    <div id="myContacts">
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
              ${silfn:formatPaginationCounter(memberPage, userTotalNumber)} ${paginationCounterSuffix}
          </div>
          <div id="users">
            <view:listPane var="directoryMembers"
                                 routingAddress="Pagination"
                                 page="${memberPage}">
            <ol class="message_list aff_colonnes">
              <view:listItems items="${fragments}" var="fragment">
                <li class="intfdcolor ${fragment.type} showActionsOnMouseOver" id="user-${fragment.userId}">
                    ${fragment.fragment}
                  <br clear="all"/>
                </li>
              </view:listItems>
            </ol>
            </view:listPane>
            <script type="text/javascript">
              whenSilverpeasReady(function() {
                activateUserZoom();
                sp.listPane.ajaxControls('#myContacts', {
                  before : function(ajaxConfig) {
                    $.progressMessage();
                    window.history.replaceState(null, "", ajaxConfig.getUrl());
                  }
                });
              });
            </script>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
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