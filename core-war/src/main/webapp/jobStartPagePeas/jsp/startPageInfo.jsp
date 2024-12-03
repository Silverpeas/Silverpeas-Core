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
<%@page import="org.silverpeas.web.jobstartpage.JobStartPagePeasSettings"%>

<%@ page import="org.silverpeas.core.admin.space.SpaceHomePageType" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="space" value="${requestScope.Space}" />
<jsp:useBean id="space" type="org.silverpeas.core.admin.space.SpaceInst"/>

<%@ include file="check.jsp" %>

<fmt:message key="JSPP.ModifyStartPage" var="modifyStartPageLabel"/>
<fmt:message key="GML.popupTitle" var="pageTitle"/>

<c:set var="maintenanceState" value="${requestScope.MaintenanceState}" />
<c:set var="m_SpaceId" value="${requestScope.currentSpaceId}" />
<c:set var="m_firstPageType" value="${requestScope.FirstPageType}" />

<c:set var="objectsSelectedInClipboard" value="${requestScope.ObjectsSelectedInClipboard}" />
<c:set var="m_SpaceExtraInfos" value="${requestScope.SpaceExtraInfos}"/>
<c:set var="isUserAdmin" value="${requestScope.isUserAdmin}" />
<c:set var="isBackupEnable" value="${requestScope.IsBackupEnable}" />
<c:set var="isInHeritanceEnable" value="${requestScope.IsInheritanceEnable}" />
<c:set var="copiedComponentNames" value="${requestScope.CopiedComponents}" />
<c:set var="m_context" value="<%=m_context%>" />

<c:set var="maintenancePlatform" value="<%=JobStartPagePeasSessionController.MAINTENANCE_PLATFORM%>"/>
<c:set var="isComponentSpaceQuotaActivated" value="<%=JobStartPagePeasSettings.componentsInSpaceQuotaActivated%>"/>
<c:if test="${isComponentSpaceQuotaActivated and (QuotaLoad.UNLIMITED eq space.componentSpaceQuota.load)}">
  <c:set var="isComponentSpaceQuotaActivated" value="false"/>
</c:if>
<c:set var="isDataStorageQuotaActivated" value="<%=JobStartPagePeasSettings.dataStorageInSpaceQuotaActivated%>"/>

<c:set var="m_SpaceName" value="${requestScope.spaceName}" />
<c:set var="m_SpaceDescription" value="${requestScope.spaceDescription}" />
<c:set var="spaceLook" value="${space.look}" />
<c:set var="availableLooks" value="<%=gef.getAvailableLooks()%>" />

<c:set var="pageTypesLabels" value='<%=new String[]{resource.getString("JSPP.main"),resource.getString("JSPP.peas"),resource.getString("JSPP.portlet"),resource.getString("JSPP.webPage")}%>' />

<%
  String 			m_sName 		= space.getName(resource.getLanguage());
%>

<fmt:message key="GML.description" var="Description" />
<fmt:message key="JSPP.SpaceAppearance" var="SpaceLook" />
<fmt:message key="JSPP.Manager" var="SpaceManager" />

<fmt:message key="JSPP.SpacePanelModifyTitle" var="SpacePanelModifyTitle" />
<fmt:message key="JSPP.ModifyStartPage" var="ModifyStartPageLabel" />
<fmt:message key="JSPP.SpaceOrder" var="SpaceOrderLabel" />
<fmt:message key="JSPP.maintenanceModeToOff" var="MaintenanceModeToOffLabel" />
<fmt:message key="JSPP.maintenanceModeToOn" var="MaintenanceModeToOnLabel" />
<fmt:message key="JSPP.SpacePanelDeleteTitle" var="SpacePanelDeleteTitle" />
<fmt:message key="JSPP.spaceRecover" var="SpaceRecoverLabel" />
<fmt:message key="JSPP.BackupSpace" var="BackupSpaceLabel" />
<fmt:message key="JSPP.space.copy" var="CopySpaceLabel" />
<fmt:message key="JSPP.space.cut" var="CutSpaceLabel" />
<fmt:message key="GML.paste" var="PasteLabel" />
<fmt:message key="JSPP.SubSpacePanelCreateTitle" var="SubSpacePanelCreateTitle" />
<fmt:message key="JSPP.ComponentPanelCreateTitle" var="ComponentPanelCreateTitle" />
<fmt:message key="JSPP.space.go" var="SpaceGoLabel" />
<fmt:message key="JSPP.inheritanceBlockedComponent" var="InheritanceBlockedComponentLabel" />

<fmt:message key="JSPP.inheritanceSpaceNotUsed" var="InheritanceSpaceNotUsedLabel" />
<fmt:message key="JSPP.inheritanceSpaceUsed" var="InheritanceSpaceUsedLabel" />
<fmt:message key="JSPP.homepageType" var="HomepageTypeLabel" />

<fmt:message key="JSPP.SpaceLook" var="SpaceLookLabel" />
<fmt:message key="GML.Id" var="IdLabel" />

<fmt:message key="JSPP.copyoptions.dialog.title" var="CopyDialogOptionsLabel" />
<fmt:message key="JSPP.update" var="UpdateLabel" />

<c:set var="FullMessageSuppressionSpaceLabel" value='<%=resource.getStringWithParams("JSPP.MessageSuppressionSpace", WebEncodeHelper.escapeXml(m_sName))%>' />

<fmt:message key="JSPP.spaceUpdate" var="SpaceUpdateIcon" bundle="${icons}"/>
<fmt:message key="JSPP.updateHomePage" var="UpdateHomePageIcon" bundle="${icons}"/>
<fmt:message key="JSPP.SpaceOrder" var="SpaceOrderIcon" bundle="${icons}"/>
<fmt:message key="JSPP.spaceUnlock" var="SpaceUnlockIcon" bundle="${icons}"/>
<fmt:message key="JSPP.spaceLock" var="SpaceLockIcon" bundle="${icons}"/>
<fmt:message key="JSPP.spaceDel" var="SpaceDelIcon" bundle="${icons}"/>
<fmt:message key="JSPP.spaceBackup" var="SpaceBackupIcon" bundle="${icons}"/>
<fmt:message key="JSPP.CopyComponent" var="CopyIcon" bundle="${icons}"/>
<fmt:message key="JSPP.PasteComponent" var="PasteIcon" bundle="${icons}"/>
<fmt:message key="JSPP.subspaceAdd" var="SubspaceAddIcon" bundle="${icons}"/>
<fmt:message key="JSPP.instanceAdd" var="InstanceAddIcon" bundle="${icons}"/>
<fmt:message key="JSPP.update" var="UpdateIcon" bundle="${icons}"/>

<view:browseBar spaceId="${m_SpaceId}" spaceJsCallback="parent.jumpToSpace" extraInformations="${Description}" componentJsCallback="parent.jumpToComponent" />

<view:sp-page>
  <view:sp-head-part title="${pageTitle}">
    <c:if test="${m_SpaceExtraInfos.admin}">
      <view:includePlugin name="adminspacehomepage"/>
    </c:if>
    <style>
      .txtlibform {
        white-space: nowrap;
      }
    </style>
    <script type="text/javascript">
      const currentLanguage = "${space.language}";

      function openPopup(action, larg, haut) {
        const windowName = "actionWindow";
        const windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
        SP_openWindow(action, windowName, larg, haut, windowParams, false);
      }
      <c:if test="${m_SpaceExtraInfos.admin}">
        <c:if test="${isUserAdmin && !empty m_SpaceName}">
          function deleteSpace() {
            jQuery.popup.confirm(
                "${FullMessageSuppressionSpaceLabel}",
                function() {
                  $('#spaceForm').attr('action', 'DeleteSpace');
                  $('#Id').val('${space.id}');
                  $.progressMessage();
                  setTimeout("jQuery('#spaceForm').submit();", 500);
                });
          }
        </c:if>
        function updateSpace() {
          $('#Translation').val(currentLanguage);
          $('#spaceForm').attr('action', 'UpdateSpace').submit();
        }
      </c:if>

      function clipboardPaste() {
        showPasteOptions();
      }

      function clipboardCopy() {
        top.IdleFrame.location.href = "copy?Type=Space&Id=${space.id}";
      }

      function clipboardCut() {
        top.IdleFrame.location.href = "Cut?Type=Space&Id=${space.id}";
      }

      function recoverRights() {
        $.progressMessage();
        $('#Id').val('${space.id}');
        $('#spaceForm').attr('action', 'RecoverSpaceRights').submit();
      }

      function showPasteOptions() {
        // Display copy options only if there is at least one copied compliant app (ignore cut/paste)
        new Promise(function(resolve, reject) {
          <c:choose>
          <c:when test="${empty copiedComponentNames}">
          resolve();
          </c:when>
          <c:otherwise>
          <c:forEach items="${copiedComponentNames}" var="componentName">
          $.ajax({
            url: webContext + '/${componentName}/jsp/copyApplicationDialog.jsp',
            type: "GET",
            dataType: "html",
            success: function (data) {
              $('#pasteOptions').html(data);
              resolve();
            },
            error: function () {
              resolve();
            }
          });
          </c:forEach>
          </c:otherwise>
          </c:choose>
        }).then(function () {
          if ($('#pasteOptions').is(':empty')) {
            $.progressMessage();
            location.href = "Paste";
          } else {
            $('#pasteOptionsDialog').popup('validation', {
              title: "${CopyDialogOptionsLabel}",
              callback: function () {
                $.progressMessage();
                document.pasteForm.submit();
                return true;
              }
            });
          }
        });
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="startPageInfo page_content_admin">
    <c:if test="${m_SpaceExtraInfos.admin}">
      <view:operationPane>
        <view:operation icon="${SpaceUpdateIcon}" altText="${SpacePanelModifyTitle}" action="javascript:onclick=updateSpace()"/>
        <view:operation icon="${UpdateHomePageIcon}" altText="${ModifyStartPageLabel}" action="javascript:onClick=spaceHomepageApp.api.open()"/>
        <c:if test="${isUserAdmin or m_SpaceName != null}">
          <view:operation icon="${SpaceOrderIcon}" altText="${SpaceOrderLabel}" action="javascript:onClick=openPopup('PlaceSpaceAfter', 750, 250)"/>
        </c:if>
        <c:if test="${maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_THISSPACE}">
          <view:operation icon="${SpaceUnlockIcon}" altText="${MaintenanceModeToOffLabel}" action="DesactivateMaintenance"/>
        </c:if>
        <c:if test="${maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_OFF}">
          <view:operation icon="${SpaceLockIcon}" altText="${MaintenanceModeToOnLabel}" action="ActivateMaintenance"/>
        </c:if>
        <c:if test="${isUserAdmin or m_SpaceName != null}">
          <view:operation icon="${SpaceDelIcon}" altText="${SpacePanelDeleteTitle}" action="javascript:onClick=deleteSpace()"/>
          <c:if test="${JobStartPagePeasSettings.recoverRightsEnable}">
            <view:operation icon="useless" altText="${SpaceRecoverLabel}" action="javascript:onClick=recoverRights()"/>
          </c:if>
        </c:if>
        <c:if test="${isBackupEnable}">
          <c:set var="spaceBackupAction" value="javascript:onClick=openPopup('${m_context}+${URLUtil.getURL(URLUtil.CMP_JOBBACKUP)}Main?spaceToSave=${m_SpaceId}',750, 550)"/>
          <view:operation icon="${SpaceBackupIcon}" altText="${BackupSpaceLabel}" action="${spaceBackupAction}"/>
        </c:if>

        <c:if test="${JobStartPagePeasSettings.useComponentsCopy or objectsSelectedInClipboard}">
          <view:operationSeparator/>
          <c:if test="${JobStartPagePeasSettings.useComponentsCopy}">
            <view:operation icon="${CopyIcon}" altText="${CopySpaceLabel}" action="javascript:onclick=clipboardCopy()"/>
            <c:if test="${maintenanceState >= maintenancePlatform}">
              <view:operation icon="${CopyIcon}" altText="${CutSpaceLabel}" action="javascript:onclick=clipboardCut()"/>
            </c:if>
          </c:if>
          <c:if test="${objectsSelectedInClipboard}">
            <view:operation icon="${PasteComponentIcon}" altText="${PasteLabel}" action="javascript:onclick=clipboardPaste()"/>
          </c:if>
        </c:if>
        <view:operationSeparator/>
        <view:operationOfCreation icon="${m_context}${SubspaceAddIcon}" altText="${SubSpacePanelCreateTitle}" action="CreateSpace?SousEspace=SousEspace"/>
        <c:if test="${not isComponentSpaceQuotaFull}">
          <view:operationOfCreation icon="${m_context}${InstanceAddIcon}" altText="${ComponentPanelCreateTitle}" action="ListComponent"/>
        </c:if>
      </view:operationPane>
    </c:if>
    <view:window>
      <view:tabs>
        <view:tab label="${Description}" action="#" selected="true" />
        <view:tab label="${SpaceLook}" action="SpaceLook" selected="false" />
        <view:tab label="${SpaceManager}" action="SpaceManager" selected="false" />
        <c:if test="${isInHeritanceEnable}">
          <fmt:message key="JSPP.admin" var="admin" />
          <fmt:message key="JSPP.publisher" var="publisher" />
          <fmt:message key="JSPP.writer" var="writer" />
          <fmt:message key="JSPP.reader" var="reader" />
          <c:set var="adminAction" value="SpaceManager?Role=admin"/>
          <c:set var="publisherAction" value="SpaceManager?Role=publisher"/>
          <c:set var="writerAction" value="SpaceManager?Role=writer"/>
          <c:set var="readerAction" value="SpaceManager?Role=reader"/>
          <view:tab label="${admin}" action="${adminAction}" selected="false" />
          <view:tab label="${publisher}" action="${publisherAction}" selected="false" />
          <view:tab label="${writer}" action="${writerAction}" selected="false" />
          <view:tab label="${reader}" action="${readerAction}" selected="false" />
        </c:if>
      </view:tabs>

      <fmt:message key="JSPP.maintenanceStatus.${maintenanceState}" var="maintenanceStateLabel" />
      <view:frame>
        <c:if test="${maintenanceState >= maintenancePlatform}">
          <div class="inlineMessage">
            ${maintenanceStateLabel}
          </div>
          <br/>
        </c:if>

        <c:if test="${isComponentSpaceQuotaFull}">
          <div class="inlineMessage-nok"><%=space.getComponentSpaceQuotaReachedErrorMessage(resource.getLanguage())%></div>
          <br/>
        </c:if>

        <c:if test="${isDataStorageQuotaFull}">
          <div class="inlineMessage-nok"><%=space.getDataStorageQuotaReachedErrorMessage(resource.getLanguage())%></div>
          <br/>
        </c:if>

        <view:areaOfOperationOfCreation/>

        <div class="rightContent" id="right-content-adminSpace">
          <div id="goToApplication">
            <a class="navigation-button" href="javascript:onclick=window.top.spWindow.leaveAdmin({fromSpaceId:'${space.id}'});"><span>${SpaceGoLabel}</span></a>
          </div>
          <viewTags:displayLastUserCRUD
              permalink="${space.permalink}"
              displayHour="true"
              createDate="${space.creationDate}" createdBy="${space.creator}"
              updateDate="${space.lastUpdateDate}" updatedBy="${space.lastUpdater}"/>
        </div>

        <c:if test="${m_SpaceExtraInfos.admin}">
          <div id="spaceHomepage">
            <silverpeas-admin-space-homepage-popin
                v-bind:admin-access="true"
                v-on:api="api = $event"
                v-on:validated="save($event)"
                v-bind:title="'${silfn:escapeJs(modifyStartPageLabel)}'"
                v-bind:space-id="spaceId"
                v-bind:homepage="homepage"></silverpeas-admin-space-homepage-popin>
          </div>
        </c:if>

        <div class="principalContent">
          <div id="principal-content-adminSpace">
            <div id="gauges-content-adminSpace">
              <c:if test="${isComponentSpaceQuotaActivated}">
                <fmt:message key="JSPP.componentSpaceQuotaUsed" var="tmpText"/>
                <viewTags:displayGauge title="${tmpText}" quotaBean="${space.componentSpaceQuota}"/>
              </c:if>

              <c:if test="${isDataStorageQuotaActivated}">
                <fmt:message key="JSPP.dataStorageUsed" var="tmpText"/>
                <viewTags:displayGauge title="${tmpText}" quotaBean="${space.dataStorageQuota}"/>
              </c:if>
            </div>
            <h2 id="spaceName" class="principal-content-title">
              ${m_SpaceName}
            </h2>

            <c:if test="${not empty m_SpaceDescription}">
              <p class="descriptionType" id="description-adminSpace">
                ${m_SpaceDescription}
              </p>
            </c:if>

            <table class="tableBoard">
              <tr><th/></tr>
              <tbody>
              <tr>
                <td>
                  <table>
                    <tr><th/></tr>
                    <tbody>
                    <c:if test="${not space.root && isInHeritanceEnable}">
                      <tr>
                        <td class="txtlibform">${InheritanceBlockedComponentLabel} :
                        </td>
                        <td>
                          <c:if test="${space.inheritanceBlocked}">
                            <input type="radio" disabled="disabled" checked="checked"/>&nbsp;${InheritanceSpaceNotUsedLabel}<br/>
                            <input type="radio" disabled="disabled"/>&nbsp;${InheritanceSpaceUsedLabel}
                          </c:if>
                          <c:if test="${not space.inheritanceBlocked}">
                            <input type="radio" disabled="disabled"/>&nbsp;${InheritanceSpaceNotUsedLabel}<br/>
                            <input type="radio" disabled="disabled" checked="checked"/>&nbsp;${InheritanceSpaceUsedLabel}
                          </c:if>
                        </td>
                      </tr>
                    </c:if>
                    <tr>
                      <td class="txtlibform">${HomepageTypeLabel} :</td>
                      <td>
                        <a href="javascript:onClick=spaceHomepageApp.api.open();">
                          ${pageTypesLabels[m_firstPageType]}
                            <c:if test="${m_firstPageType == SpaceHomePageType.HTML_PAGE.ordinal()}">
                              &nbsp;(${space.firstPageExtraParam})
                            </c:if>
                          <img alt="${UpdateLabel}" title="${UpdateLabel}" src="${m_context}${UpdateIcon}">
                        </a>
                      </td>
                    </tr>
                    <c:if test="${availableLooks.size() >= 2 and spaceLook != null}">
                      <tr>
                        <td class="txtlibform">${SpaceLookLabel} :</td>
                        <td><a href="SpaceLook">${spaceLook}&nbsp;<img alt="${UpdateLabel}" title="${UpdateLabel}" src="${m_context}${UpdateIcon}"></a></td>
                      </tr>
                    </c:if>
                    <tr>
                      <td class="txtlibform">${IdLabel} :</td>
                      <td>WA${m_SpaceId}</td>
                    </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </view:frame>
    </view:window>

    <c:if test="${m_SpaceExtraInfos.admin}">
      <c:set var="spaceHomepageTypes" value="<%=SpaceHomePageType.values()%>"/>
      <script type="text/javascript">
        window.spaceHomepageApp = SpVue.createApp({
          data : function() {
            return {
              api : undefined,
              spaceId : '${space.id}',
              homepage : {
                'type' : '${spaceHomepageTypes[space.firstPageType]}',
                'value' : '${silfn:escapeJs(space.firstPageExtraParam)}',
              }
            }
          },
          methods : {
            save : function(newData) {
              const spaceHomepageForm = new FormData();
              spaceHomepageForm.set("type", newData.type);
              spaceHomepageForm.set("value", newData.value);
              sp.ajaxRequest("SaveHomepageChoice")
                .byPostMethod()
                .sendAndPromiseJsonResponse(spaceHomepageForm)
                .then(function() {
                  this.homepage.type = newData.type;
                  this.homepage.value = newData.value;
                  newData.deferredSave.resolve();
                }.bind(this), function() {
                  newData.deferredSave.reject();
                }.bind(this));
            }
          }
        }).mount('#spaceHomepage');
      </script>
    </c:if>
    <view:progressMessage/>
    <div id="pasteOptionsDialog" style="display:none">
    <form name="pasteForm" action="Paste" method="GET">
    <div id="pasteOptions"></div>
    </form>
      <form id="spaceForm" action="" method="GET">
        <input id='Translation' name='Translation' type='hidden'/>
        <input id='Id' name='Id' type='hidden'/>
      </form>
    </div>
  </view:sp-body-part>
</view:sp-page>
