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
<%@page import="org.silverpeas.core.admin.quota.constant.QuotaLoad" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceHomePageType" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="space" value="${requestScope.Space}" />
<jsp:useBean id="space" type="org.silverpeas.core.admin.space.SpaceInst"/>

<%@ include file="check.jsp" %>

<fmt:message key="JSPP.ModifyStartPage" var="modifyStartPageLabel"/>

<%
  int	 			maintenanceState 	= (Integer) request.getAttribute("MaintenanceState");
  String	 		m_SpaceId 			= (String) request.getAttribute("currentSpaceId");
  Integer 		m_firstPageType 	= (Integer)request.getAttribute("FirstPageType");

  String 			m_SubSpace 			= (String) request.getAttribute("nameSubSpace");
  boolean			objectsSelectedInClipboard = Boolean.parseBoolean((String) request.getAttribute("ObjectsSelectedInClipboard"));
  DisplaySorted 	m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
  boolean 		isUserAdmin 		= (Boolean)request.getAttribute("isUserAdmin");
  boolean 		isBackupEnable 		= (Boolean)request.getAttribute("IsBackupEnable");
  boolean 		isInHeritanceEnable = (Boolean)request.getAttribute("IsInheritanceEnable");

  Set<String>	copiedComponentNames	= (Set<String>) request.getAttribute("CopiedComponents");

  // Component space quota
  boolean isComponentSpaceQuotaActivated = JobStartPagePeasSettings.componentsInSpaceQuotaActivated;

  boolean isComponentSpaceQuotaFull = isComponentSpaceQuotaActivated && space.isComponentSpaceQuotaReached();
  if (isComponentSpaceQuotaActivated && QuotaLoad.UNLIMITED.equals(space.getComponentSpaceQuota().getLoad())) {
    isComponentSpaceQuotaActivated = false;
  }

  // Data storage quota
  boolean isDataStorageQuotaActivated = JobStartPagePeasSettings.dataStorageInSpaceQuotaActivated;
  boolean isDataStorageQuotaFull = isDataStorageQuotaActivated && space.isDataStorageQuotaReached();
  if (isDataStorageQuotaActivated) {
    if (QuotaLoad.UNLIMITED.equals(space.getDataStorageQuota().getLoad())) {
      isDataStorageQuotaActivated = false;
    }
  }

  String 			m_SpaceName 		= space.getName(resource.getLanguage());
  String 			m_Description 		= space.getDescription(resource.getLanguage());

  List<String>	availableLooks		= gef.getAvailableLooks();
  String		spaceLook				= space.getLook();

  if (spaceLook == null) {
    spaceLook = "&nbsp;";
  }

  String[] pageType = {resource.getString("JSPP.main"),resource.getString("JSPP.peas"),resource.getString("JSPP.portlet"),resource.getString("JSPP.webPage")};

  TabbedPane tabbedPane = gef.getTabbedPane();

  browseBar.setSpaceId(m_SpaceId);
  browseBar.setExtraInformation(resource.getString("GML.description"));
  browseBar.setI18N(space, resource.getLanguage());

  if (m_SpaceExtraInfos.isAdmin()) {
    operationPane.addOperation(resource.getIcon("JSPP.spaceUpdate"),resource.getString("JSPP.SpacePanelModifyTitle"),"javascript:onclick=updateSpace()");
    operationPane.addOperation(resource.getIcon("JSPP.updateHomePage"),resource.getString("JSPP.ModifyStartPage"),"javascript:onClick=spaceHomepageApp.api.open()");
    if (isUserAdmin || m_SubSpace != null) {
      operationPane.addOperation(resource.getIcon("JSPP.SpaceOrder"),resource.getString("JSPP.SpaceOrder"),"javascript:onClick=openPopup('PlaceSpaceAfter', 750, 250)");
    }

    // This space configuration
    if (maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_THISSPACE) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceUnlock"),resource.getString("JSPP.maintenanceModeToOff"),"DesactivateMaintenance");
    } else if (maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_OFF){
      operationPane.addOperation(resource.getIcon("JSPP.spaceLock"),resource.getString("JSPP.maintenanceModeToOn"),"ActivateMaintenance");
    }
    if (isUserAdmin || m_SubSpace != null) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceDel"),resource.getString("JSPP.SpacePanelDeleteTitle"),"javascript:onClick=deleteSpace()");
      if (JobStartPagePeasSettings.recoverRightsEnable) {
        operationPane.addOperation("useless",resource.getString("JSPP.spaceRecover"),"javascript:onClick=recoverRights()");
      }
    }

    if (isBackupEnable) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceBackup"),resource.getString("JSPP.BackupSpace"),"javascript:onClick=openPopup('"+m_context+URLUtil.getURL(URLUtil.CMP_JOBBACKUP)+"Main?spaceToSave=" + m_SpaceId + "', 750, 550)");
    }

    if (JobStartPagePeasSettings.useComponentsCopy || objectsSelectedInClipboard) {
      operationPane.addLine();
      if (JobStartPagePeasSettings.useComponentsCopy) {
        operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.space.copy"),"javascript:onclick=clipboardCopy()");
        if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) {
          operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.space.cut"),"javascript:onclick=clipboardCut()");
        }
      }
      if (objectsSelectedInClipboard) {
        operationPane.addOperation(resource.getIcon("JSPP.PasteComponent"),resource.getString("GML.paste"),"javascript:onclick=clipboardPaste()");
      }
    }
    operationPane.addLine();
    operationPane.addOperationOfCreation(resource.getIcon("JSPP.subspaceAdd"),resource.getString("JSPP.SubSpacePanelCreateTitle"),"CreateSpace?SousEspace=SousEspace");
    if (!isComponentSpaceQuotaFull) {
      operationPane.addOperationOfCreation(resource.getIcon("JSPP.instanceAdd"),resource.getString("JSPP.ComponentPanelCreateTitle"),"ListComponent");
    }
  }

  tabbedPane.addTab(resource.getString("GML.description"), "#", true);
  tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", false);
  tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", false);

  if (isInHeritanceEnable) {
    tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", false);
    tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", false);
    tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", false);
    tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", false);
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=resource.getString("GML.popupTitle")%></title>
  <view:looknfeel withCheckFormScript="true"/>
  <c:if test="<%=m_SpaceExtraInfos.isAdmin()%>">
    <view:includePlugin name="adminspacehomepage"/>
  </c:if>
  <style type="text/css">
    .txtlibform {
      white-space: nowrap;
    }
  </style>
  <script type="text/javascript">
    //<!--
    var currentLanguage = "<%=space.getLanguage()%>";
    <%
      for (String lang : space.getTranslations().keySet()) {
        out.println("var name_"+lang+" = \""+WebEncodeHelper.javaStringToJsString(space.getName(lang))+"\";\n");
        out.println("var desc_"+lang+" = \""+WebEncodeHelper.javaStringToJsString(space.getDescription(lang))+"\";\n");
      }
    %>

    function showTranslation(lang)
    {
      <%=I18NHelper.updateHTMLLinks(space)%>

      document.getElementById("spaceName").innerHTML = eval("name_"+lang);
      document.getElementById("description-adminSpace").innerHTML = eval("desc_"+lang);

    }

    function openPopup(action, larg, haut) {
      windowName = "actionWindow";
      windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
      actionWindow = SP_openWindow(action, windowName, larg, haut, windowParams, false);
    }

    <% if (m_SpaceExtraInfos.isAdmin()) {
        if (isUserAdmin || (m_SubSpace != null)) { %>
          function deleteSpace() {
            jQuery.popup.confirm(
                "<%=resource.getStringWithParams("JSPP.MessageSuppressionSpace", WebEncodeHelper.escapeXml(m_SpaceName))%>",
                function() {
                  $('#spaceForm').attr('action', 'DeleteSpace');
                  $('#Id').val('<%=space.getId()%>');
                  $.progressMessage();
                  setTimeout("jQuery('#spaceForm').submit();", 500);
                });
          }
      <% } %>
      function updateSpace() {
        $('#Translation').val(currentLanguage);
        $('#spaceForm').attr('action', 'UpdateSpace').submit();
      }
    <% } %>

    function clipboardPaste() {
      showPasteOptions();
    }

    function clipboardCopy() {
      top.IdleFrame.location.href = "copy?Type=Space&Id=<%=space.getId()%>";
    }

    function clipboardCut() {
      top.IdleFrame.location.href = "Cut?Type=Space&Id=<%=space.getId()%>";
    }

    function recoverRights() {
      $.progressMessage();
      $('#Id').val('<%=space.getId()%>');
      $('#spaceForm').attr('action', 'RecoverSpaceRights').submit();
    }

    function showPasteOptions() {
      // Display copy options only if there is at least one copied compliant app (ignore cut/paste)
      new Promise(function(resolve, reject) {
        <% if (copiedComponentNames.isEmpty() ){ %>
        resolve();
        <% } else {
        for (String componentName : copiedComponentNames) { %>
        $.ajax({
          url: webContext+'/<%=componentName%>/jsp/copyApplicationDialog.jsp',
          type: "GET",
          dataType: "html",
          success: function(data) {
            $('#pasteOptions').html(data);
            resolve();
          },
          error: function() {
            resolve();
          }
        });
        <% }
         } %>
      }).then(function() {
        if ($('#pasteOptions').is(':empty')) {
          $.progressMessage();
          location.href="Paste";
        } else {
          $('#pasteOptionsDialog').popup('validation', {
            title : "<%=resource.getString("JSPP.copyoptions.dialog.title")%>",
            callback : function() {
              $.progressMessage();
              document.pasteForm.submit();
              return true;
            }
          });
        }
      });

    }
    //-->
  </script>
</head>
<body class="startPageInfo page_content_admin">
<%
  out.println(window.printBefore());
  out.println(tabbedPane.print());
%>
<view:frame>
  <% if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) { %>
  <div class="inlineMessage">
    <%=resource.getString("JSPP.maintenanceStatus."+maintenanceState)%>
  </div>
  <br clear="all"/>
  <% } %>
  <% if (isComponentSpaceQuotaFull) { %>
  <div class="inlineMessage-nok"><%=space.getComponentSpaceQuotaReachedErrorMessage(resource.getLanguage())%></div>
  <br clear="all"/>
  <% } %>
  <% if (isDataStorageQuotaFull) { %>
  <div class="inlineMessage-nok"><%=space.getDataStorageQuotaReachedErrorMessage(resource.getLanguage())%></div>
  <br clear="all"/>
  <% } %>
  <view:areaOfOperationOfCreation/>
  <div class="rightContent" id="right-content-adminSpace">
    <div id="goToApplication">
      <a class="navigation-button" href="javascript:onclick=window.top.spWindow.leaveAdmin({fromSpaceId:'<%=space.getId()%>'});"><span><%=resource.getString("JSPP.space.go")%></span></a>
    </div>
    <viewTags:displayLastUserCRUD
        permalink="${space.permalink}"
        displayHour="true"
        createDate="${space.creationDate}" createdBy="${space.creator}"
        updateDate="${space.lastUpdateDate}" updatedBy="${space.lastUpdater}"/>
  </div>

  <c:if test="<%=m_SpaceExtraInfos.isAdmin()%>">
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
        <% if (isComponentSpaceQuotaActivated) { %>
        <fmt:message key="JSPP.componentSpaceQuotaUsed" var="tmpText"/>
        <viewTags:displayGauge title="${tmpText}"
                               quotaBean="<%=space.getComponentSpaceQuota()%>"/>
        <% } %>
        <% if (isDataStorageQuotaActivated) { %>
        <fmt:message key="JSPP.dataStorageUsed" var="tmpText"/>
        <viewTags:displayGauge title="${tmpText}"
                               quotaBean="<%=space.getDataStorageQuota()%>"/>
        <% } %>
      </div>
      <h2 id="spaceName" class="principal-content-title"><%=WebEncodeHelper.javaStringToHtmlString(m_SpaceName)%>
      </h2>

      <%if (StringUtil.isDefined(m_Description)) {%>
      <p class="descriptionType" id="description-adminSpace"><%=WebEncodeHelper
          .javaStringToHtmlParagraphe(m_Description)%>
      </p>
      <%}%>
      <table width="98%" cellspacing="0" cellpadding="5" border="0" class="tableBoard">
        <tbody>
        <tr>
          <td>
            <table width="100%" cellspacing="0" cellpadding="5" border="0">
              <tbody>
              <% if (!space.isRoot() && isInHeritanceEnable) { %>
              <tr>
                <td class="txtlibform" valign="top"><%=resource
                    .getString("JSPP.inheritanceBlockedComponent") %> :
                </td>
                <td align="left" valign="baseline" width="100%">
                  <% if (space.isInheritanceBlocked()) { %>
                  <input type="radio" disabled="disabled" checked="checked"/> <%=resource
                    .getString("JSPP.inheritanceSpaceNotUsed")%><br/>
                  <input type="radio" disabled="disabled"/> <%=resource
                    .getString("JSPP.inheritanceSpaceUsed")%>
                  <% } else { %>
                  <input type="radio" disabled="disabled"/> <%=resource
                    .getString("JSPP.inheritanceSpaceNotUsed")%><br/>
                  <input type="radio" disabled="disabled" checked="checked"/> <%=resource
                    .getString("JSPP.inheritanceSpaceUsed")%>
                  <% } %>
                </td>
              </tr>
              <% } %>
              <tr>
                <td class="txtlibform"><%=resource.getString("JSPP.homepageType") %> :</td>
                <td valign="baseline" width="100%"><%=pageType[m_firstPageType.intValue()] %>
                </td>
              </tr>
              <% if (availableLooks.size() >= 2 && StringUtil.isDefined(spaceLook)) { %>
              <tr>
                <td class="txtlibform"><%=resource.getString("JSPP.SpaceLook")%> :</td>
                <td><%=spaceLook%></td>
              </tr>
              <% } %>
              <tr>
                <td class="txtlibform"><%=resource.getString("GML.Id")%> :</td>
                <td>WA<%=m_SpaceId%></td>
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
<%
  out.println(window.printAfter());
%>
<c:if test="<%=m_SpaceExtraInfos.isAdmin()%>">
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
</body>
</html>