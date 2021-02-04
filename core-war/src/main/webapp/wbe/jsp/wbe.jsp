<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ page import="org.silverpeas.web.wbe.WbeFileUIEntity" %>
<%@ page import="org.silverpeas.web.wbe.WbeUserUIEntity" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="zoneId" value="${sessionScope['SilverSessionController'].favoriteZoneId}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="isEnabled" value="${requestScope['isEnabled']}"/>
<c:set var="clientAdministrationUrl" value="${requestScope['clientAdministrationUrl']}"/>
<c:set var="allUsers" value="${requestScope['AllUsers']}"/>
<jsp:useBean id="allUsers" type="org.silverpeas.core.util.SilverpeasList<org.silverpeas.core.wbe.WbeUser>"/>
<c:set var="allFiles" value="${requestScope['AllFiles']}"/>
<jsp:useBean id="allFiles" type="org.silverpeas.core.util.SilverpeasList<org.silverpeas.core.wbe.WbeFile>"/>
<c:set var="selectedUserIds" value="${requestScope.SelectedUserIds}"/>
<jsp:useBean id="selectedUserIds" type="java.util.Set<java.lang.String>"/>
<c:set var="selectedFileIds" value="${requestScope.SelectedFileIds}"/>
<jsp:useBean id="selectedFileIds" type="java.util.Set<java.lang.String>"/>

<fmt:message var="browseBarAll" key="wbe.breadcrumb"/>
<fmt:message var="enable" key="wbe.action.enable"/>
<fmt:message var="disable" key="wbe.action.disable"/>
<fmt:message var="disableConfirm" key="wbe.action.disable.confirm"/>
<fmt:message var="disableConfirmHelp" key="wbe.action.disable.confirm.help"/>
<fmt:message var="revokeSelected" key="wbe.action.selection.revoke"/>
<fmt:message var="revokeSelectedConfirm" key="wbe.action.selection.revoke.confirm"/>
<fmt:message var="revokeAll" key="wbe.action.all.revoke"/>
<fmt:message var="revokeAllConfirm" key="wbe.action.all.revoke.confirm"/>

<fmt:message var="userNameLabel" key="wbe.user.name"/>
<fmt:message var="userEditedFileLabel" key="wbe.user.editedFiles"><fmt:param value="${1}"/></fmt:message>
<fmt:message var="userEditedFilesLabel" key="wbe.user.editedFiles"><fmt:param value="${2}"/></fmt:message>
<fmt:message var="userNbEditedFilesLabel" key="wbe.user.editedFiles.nb"/>
<fmt:message var="userLastEditionLabel" key="wbe.user.lastEdition"/>
<fmt:message var="fileNameLabel" key="wbe.file.name"/>
<fmt:message var="fileLocationLabel" key="wbe.file.location"/>
<fmt:message var="fileEditorLabel" key="wbe.file.editors"><fmt:param value="${1}"/></fmt:message>
<fmt:message var="fileEditorsLabel" key="wbe.file.editors"><fmt:param value="${2}"/></fmt:message>
<fmt:message var="fileNbEditorsLabel" key="wbe.file.editors.nb"/>
<fmt:message var="fileLastEditionLabel" key="wbe.file.lastEdition"/>

<view:sp-page>
  <view:sp-head-part>
    <c:if test="${isEnabled}">
      <style type="text/css">
        #disableFormDialog .help {
          font-size: 0.8em;
        }
        #dynamic-containers {
          display: table;
          width: 100%;
        }
        #dynamic-user-container,
        #dynamic-user-container {
          display: table-cell;
        }
        #dynamic-user-container {
          width: auto;
          padding-right: 5px;
        }
        #dynamic-file-container {
          width: auto;
          padding-left: 5px;
        }
        .tip-extra-info {
          max-width: 1000px;
        }
      </style>
    </c:if>
    <script type="text/javascript">
      <c:choose>
      <c:when test="${isEnabled}">
      let refreshTimer;
      const userSelectionOptions = {
        paramSelectedIds : 'selectedUserIds', paramUnselectedIds : 'unselectedUserIds'
      };
      const fileSelectionOptions = {
        paramSelectedIds : 'selectedFileIds', paramUnselectedIds : 'unselectedFileIds'
      };
      let userArrayPaneAjaxControl;
      let fileArrayPaneAjaxControl;
      const userCheckboxMonitor = sp.selection.newCheckboxMonitor(
          '#dynamic-user-container input[name=selection]');
      const fileCheckboxMonitor = sp.selection.newCheckboxMonitor(
          '#dynamic-file-container input[name=selection]');

      function revokeSelected() {
        jQuery.popup.confirm('${silfn:escapeJs(revokeSelectedConfirm)}', function() {
          spProgressMessage.show();
          const ajaxRequest = sp.ajaxRequest("revokeSelected").byPostMethod();
          userCheckboxMonitor.prepareAjaxRequest(ajaxRequest, userSelectionOptions);
          fileCheckboxMonitor.prepareAjaxRequest(ajaxRequest, fileSelectionOptions);
          ajaxRequest.send().then(function(request) {
            userArrayPaneAjaxControl.refreshFromRequestResponse(request);
            fileArrayPaneAjaxControl.refreshFromRequestResponse(request);
          });
        });
      }

      function revokeAll() {
        jQuery.popup.confirm("${silfn:escapeJs(revokeAllConfirm)}", function() {
          spProgressMessage.show();
          const ajaxRequest = sp.ajaxRequest("revokeAll").byPostMethod();
          ajaxRequest.send().then(function(request) {
            userArrayPaneAjaxControl.refreshFromRequestResponse(request);
            fileArrayPaneAjaxControl.refreshFromRequestResponse(request);
          });
        });
      }

      function disable() {
        jQuery('#disableFormDialog').popup('confirmation', {
          callback : function() {
            spProgressMessage.show();
            sp.formRequest('disable').byPostMethod().submit();
          }
        });
      }

      function updateRefreshTimeout(timeout) {
        clearTimeout(refreshTimer);
        refreshTimer = setTimeout(function() {
          let ajaxRequest = sp.ajaxRequest("Main");
          userCheckboxMonitor.prepareAjaxRequest(ajaxRequest, userSelectionOptions);
          fileCheckboxMonitor.prepareAjaxRequest(ajaxRequest, fileSelectionOptions);
          ajaxRequest.send().then(function(request) {
            userArrayPaneAjaxControl.refreshFromRequestResponse(request);
            fileArrayPaneAjaxControl.refreshFromRequestResponse(request);
          });
        }, timeout ? timeout : 10000);
      }

      let qtipApi;

      function showEditedFilesTip(element, editedFiles) {
        clearTimeout(refreshTimer);
        let $container = document.createElement('div');
        editedFiles.forEach(function(editedFile) {
          let $fileContainer = document.createElement('div');
          let fileHTML = editedFile.name;
          if (editedFile.location) {
            fileHTML = editedFile.location + ' > ' + fileHTML;
          }
          $fileContainer.innerHTML = fileHTML;
          $container.appendChild($fileContainer);
        });
        qtipApi = TipManager.simpleDetails(element, function() {
          return $container;
        }, tipOptionsWithSingularOrPluralLabel(editedFiles.length, '${silfn:escapeJs(userEditedFileLabel)}', '${silfn:escapeJs(userEditedFilesLabel)}'));
        qtipApi.show();
      }

      function showEditorsTip(element, editors) {
        clearTimeout(refreshTimer);
        let $container = document.createElement('div');
        editors.forEach(function(editor) {
          let $fileContainer = document.createElement('div');
          $fileContainer.innerHTML = editor.name;
          $container.appendChild($fileContainer);
        });
        qtipApi = TipManager.simpleDetails(element, function() {
          return $container;
        }, tipOptionsWithSingularOrPluralLabel(editors.length, '${silfn:escapeJs(fileEditorLabel)}', '${silfn:escapeJs(fileEditorsLabel)}'));
        qtipApi.show();
      }

      function hideTip() {
        updateRefreshTimeout(2000);
        qtipApi.destroy();
      }

      function tipOptionsWithSingularOrPluralLabel(nbItems, singular, plural) {
        return {
          content : {
            title : {
              text : nbItems > 1 ? plural : singular
            }
          },
          style : {
            classes : 'tip-extra-info qtip-free-width'
          }
        }
      }
      </c:when>
      <c:otherwise>
      function enable() {
        sp.formRequest('enable').byPostMethod().submit();
      }
      </c:otherwise>
      </c:choose>
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="page_content_admin wbe_admin">
    <view:browseBar extraInformations="${browseBarAll}"/>
    <view:operationPane>
      <view:operation action="javascript:${isEnabled ? 'dis' : 'en' }able()" icon="" altText="${isEnabled ? disable : enable}"/>
      <c:if test="${isEnabled}">
        <view:operationSeparator/>
        <view:operation action="javascript:revokeSelected()" icon="" altText="${revokeSelected}"/>
        <view:operation action="javascript:revokeAll()" icon="" altText="${revokeAll}"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <view:frame>
        <c:choose>
          <c:when test="${isEnabled}">
            <c:if test="${not empty clientAdministrationUrl}">
              <p>
                <a href="${clientAdministrationUrl}" target="_blank"><fmt:message key="wbe.client.admin.url"/></a>
              </p>
            </c:if>
            <div id="dynamic-containers">
              <div id="dynamic-user-container">
                <view:arrayPane var="arrayOfWbeUsers" routingAddress="Main" numberLinesPerPage="25">
                  <view:arrayColumn width="10" sortable="false"/>
                  <view:arrayColumn title="${userNameLabel}" compareOn="${r -> r.data.asSilverpeas().displayedName}"/>
                  <view:arrayColumn title="${userNbEditedFilesLabel}" compareOn="${r -> r.editedFiles().size()}"/>
                  <view:arrayColumn title="${userLastEditionLabel}" compareOn="${r -> r.data.lastEditionDate}"/>
                  <view:arrayLines var="user" items="<%=WbeUserUIEntity.convertList(allUsers, selectedUserIds)%>">
                    <view:arrayLine>
                      <view:arrayCellCheckbox name="selection" checked="${user.selected}" value="${user.id}"/>
                      <view:arrayCellText>${user.data.asSilverpeas().displayedName}</view:arrayCellText>
                      <c:set var="editedFilesAsJson">
                        [<c:forEach var="file" varStatus="status" items="${user.editedFiles()}">
                        <c:if test="${not status.first}">,</c:if>{'name':'${file.data.name()}','location':'${file.getLocation(lang)}'}
                        </c:forEach>]</c:set>
                      <view:arrayCellText><a href="javascript:void(0)" onmouseenter="showEditedFilesTip(this, ${editedFilesAsJson})" onmouseleave="hideTip()">${user.editedFiles().size()}</a></view:arrayCellText>
                      <view:arrayCellText>${silfn:formatTemporal(user.data.lastEditionDate, zoneId, lang)}</view:arrayCellText>
                    </view:arrayLine>
                  </view:arrayLines>
                </view:arrayPane>
                <script type="text/javascript">
                  whenSilverpeasReady(function() {
                    userCheckboxMonitor.pageChanged();
                    userArrayPaneAjaxControl =
                        sp.arrayPane.ajaxControls('#dynamic-user-container', {
                          before : function(ajaxRequest) {
                            userCheckboxMonitor.prepareAjaxRequest(ajaxRequest, userSelectionOptions)
                          }
                        });
                    updateRefreshTimeout();
                  });
                </script>
              </div>
              <div id="dynamic-file-container">
                <view:arrayPane var="arrayOfWbeFiles" routingAddress="Main" numberLinesPerPage="25">
                  <view:arrayColumn width="10" sortable="false"/>
                  <view:arrayColumn title="${fileNameLabel}" compareOn="${r -> r.data.name()}"/>
                  <view:arrayColumn title="${fileLocationLabel}" compareOn="${r -> r.getLocation(lang)}"/>
                  <view:arrayColumn title="${fileNbEditorsLabel}" compareOn="${r -> r.editors().size()}"/>
                  <view:arrayColumn title="${fileLastEditionLabel}" compareOn="${r -> r.data.lastEditionDate}"/>
                  <view:arrayLines var="file" items="<%=WbeFileUIEntity.convertList(allFiles, selectedFileIds)%>">
                    <view:arrayLine>
                      <view:arrayCellCheckbox name="selection" checked="${file.selected}" value="${file.id}"/>
                      <view:arrayCellText>${file.data.name()}</view:arrayCellText>
                      <view:arrayCellText>${file.getLocation(lang)}</view:arrayCellText>
                      <c:set var="editorsAsJson">
                        [<c:forEach var="user" varStatus="status" items="${file.editors()}">
                        <c:if test="${not status.first}">,</c:if>{'name':'${user.data.asSilverpeas().displayedName}'}
                      </c:forEach>]</c:set>
                      <view:arrayCellText><a href="javascript:void(0)" onmouseenter="showEditorsTip(this, ${editorsAsJson})" onmouseleave="hideTip()">${file.editors().size()}</a></view:arrayCellText>
                      <view:arrayCellText>${silfn:formatTemporal(file.data.lastEditionDate, zoneId, lang)}</view:arrayCellText>
                    </view:arrayLine>
                  </view:arrayLines>
                </view:arrayPane>
                <script type="text/javascript">
                  whenSilverpeasReady(function() {
                    fileCheckboxMonitor.pageChanged();
                    fileArrayPaneAjaxControl =
                        sp.arrayPane.ajaxControls('#dynamic-file-container', {
                          before : function(ajaxRequest) {
                            fileCheckboxMonitor.prepareAjaxRequest(ajaxRequest, fileSelectionOptions)
                          }
                        });
                    updateRefreshTimeout();
                  });
                </script>
              </div>
            </div>
            <div id="disableFormDialog" style="display: none;">
                ${disableConfirm}
              <div class="help">(${disableConfirmHelp})</div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="inlineMessage"><fmt:message key="wbe.info.disabled"/></div>
          </c:otherwise>
        </c:choose>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
