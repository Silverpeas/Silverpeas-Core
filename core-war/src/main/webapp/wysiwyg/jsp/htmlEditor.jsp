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

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.silvertrace.SilverTrace" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.contribution.content.wysiwyg.WysiwygException" %>
<%@ page import="org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%
  String componentId = "";
  String spaceLabel = "";
  String componentLabel = "";
  String objectId = "";
  String objectType = "";
  String language = "";
  String contentLanguage = "";
  String codeWysiwyg = "";
  String returnUrl = null;
  String browseInformation = null;
  String fileName = "";
  String path = "";
  String[][] collectionPages = null;
  String specificURL = "";    //For Websites only
  boolean isWebSiteCase = false;    //For Websites only

  String wysiwygTextValue = "";
  String context = URLUtil.getApplicationURL();
  String userId = "";
  String indexIt = "";

  String actionWysiwyg = request.getParameter("actionWysiwyg");
  if (actionWysiwyg == null) {
    actionWysiwyg = "Load";
  }

  UserSubscriptionNotificationSendingHandler.verifyRequest(request);

  if ("SaveHtmlAndExit".equals(actionWysiwyg) || "Refresh".equals(actionWysiwyg) ||
      "SaveHtml".equals(actionWysiwyg)) {
    codeWysiwyg = request.getParameter("editor1");
    spaceLabel = (String) session.getAttribute("WYSIWYG_SpaceLabel");
    componentId = (String) session.getAttribute("WYSIWYG_ComponentId");
    componentLabel = (String) session.getAttribute("WYSIWYG_ComponentLabel");
    objectId = (String) session.getAttribute("WYSIWYG_ObjectId");
    objectType = (String) session.getAttribute("WYSIWYG_ObjectType");
    browseInformation = (String) session.getAttribute("WYSIWYG_BrowseInfo");
    language = (String) session.getAttribute("WYSIWYG_Language");
    contentLanguage = (String) session.getAttribute("WYSIWYG_ContentLanguage");
    returnUrl = (String) session.getAttribute("WYSIWYG_ReturnUrl");
    userId = (String) session.getAttribute("WYSIWYG_UserId");
    fileName = (String) session.getAttribute("WYSIWYG_FileName");
    path = (String) session.getAttribute("WYSIWYG_Path");
    specificURL = (String) session.getAttribute("WYSIWYG_SpecificURL");
    indexIt = (String) session.getAttribute("WYSIWYG_IndexIt");
    isWebSiteCase = StringUtil.defaultStringIfNotDefined(componentId).startsWith(WysiwygController.WYSIWYG_WEBSITES) && StringUtil.isLong(objectId);

    if ("SaveHtmlAndExit".equals(actionWysiwyg) || "SaveHtml".equals(actionWysiwyg)) {
      //For parsing absolute url (Bug FCKEditor)
      String server = request.getRequestURL()
          .substring(0, request.getRequestURL().toString().lastIndexOf(context));
      int serverPort = request.getServerPort();
      if (isWebSiteCase) {
        codeWysiwyg = codeWysiwyg.replaceAll("../../../../../", "/");
        codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
        codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
      } else {
        codeWysiwyg = codeWysiwyg.replaceAll("../../../../", context + "/");
        codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
        codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
      }

      if (isWebSiteCase) {
        WysiwygController.updateWebsite(path, fileName, codeWysiwyg);
      } else {
        boolean bIndexIt = (!StringUtil.isDefined(indexIt) || !"false".equalsIgnoreCase(indexIt));
        if (StringUtil.isDefined(contentLanguage)) {
          WysiwygController
              .save(codeWysiwyg, componentId, objectId, userId, contentLanguage, bIndexIt);
        } else {
          WysiwygController
              .updateFileAndAttachment(codeWysiwyg, componentId, objectId, userId, contentLanguage,
                  bIndexIt);
        }
      }
    }
    if ("Refresh".equals(actionWysiwyg)) {
      wysiwygTextValue = codeWysiwyg;
      if (isWebSiteCase) {
        collectionPages = WysiwygController.getWebsitePages(path, componentId);
        SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
            "nb collectionPages = " + collectionPages.length);
      }
    }
    if ("SaveHtmlAndExit".equals(actionWysiwyg)) {
      session.removeAttribute("WYSIWYG_ContentLanguage");
    }
  } else if ("Load".equals(actionWysiwyg)) {

    spaceLabel = request.getParameter("SpaceLabel");
    if (spaceLabel == null) {
      spaceLabel = (String) request.getAttribute("SpaceLabel");
    }

    componentId = request.getParameter("ComponentId");
    if (componentId == null) {
      componentId = (String) request.getAttribute("ComponentId");
    }

    componentLabel = request.getParameter("ComponentLabel");
    if (componentLabel == null) {
      componentLabel = (String) request.getAttribute("ComponentLabel");
    }

    objectId = request.getParameter("ObjectId");
    if (objectId == null) {
      objectId = (String) request.getAttribute("ObjectId");
    }

    objectType = request.getParameter("ObjectType");
    if (objectType == null) {
      objectType = (String) request.getAttribute("ObjectType");
    }

    returnUrl = request.getParameter("ReturnUrl");
    if (returnUrl == null) {
      returnUrl = (String) request.getAttribute("ReturnUrl");
    }
    session.setAttribute("WYSIWYG_ReturnUrl", returnUrl);

    browseInformation = request.getParameter("BrowseInfo");
    if (browseInformation == null) {
      browseInformation = (String) request.getAttribute("BrowseInfo");
    }

    isWebSiteCase = StringUtil.defaultStringIfNotDefined(componentId).startsWith(WysiwygController.WYSIWYG_WEBSITES) && StringUtil.isLong(objectId);

    userId = ((MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).getUserId();
    session.setAttribute("WYSIWYG_UserId", userId);

    fileName = request.getParameter("FileName");
    if (fileName == null) {
      fileName = (String) request.getAttribute("FileName");
    }
    session.setAttribute("WYSIWYG_FileName", fileName);

    path = request.getParameter("Path");
    if (path == null) {
      path = (String) request.getAttribute("Path");
    }
    if (componentId.startsWith("webSites")) {
      path = WysiwygController.getWebsiteRepository() + path;
    }
    session.setAttribute("WYSIWYG_Path", path);

    language = request.getParameter("Language");
    if (language == null) {
      language = (String) request.getAttribute("Language");
    }
    if (language == null) {
      language = "en";
    }

    contentLanguage = request.getParameter("ContentLanguage");
    if (contentLanguage == null) {
      contentLanguage = (String) request.getAttribute("ContentLanguage");
    }
    session.setAttribute("WYSIWYG_ContentLanguage", contentLanguage);

    indexIt = request.getParameter("IndexIt");
    if (indexIt == null) {
      indexIt = (String) request.getAttribute("IndexIt");
    }
    session.setAttribute("WYSIWYG_IndexIt", indexIt);

    if (isWebSiteCase) {
      collectionPages = WysiwygController.getWebsitePages(path, componentId);
      SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
          "nb collectionPages = " + collectionPages.length);
      specificURL = "/website/" + componentId + "/" + objectId + "/";
    } else {
      specificURL = context;
    }
    session.setAttribute("WYSIWYG_SpecificURL", specificURL);

    try {
      if (isWebSiteCase) {
        wysiwygTextValue = WysiwygController.loadFileWebsite(path, fileName);
      } else {
        wysiwygTextValue = WysiwygController.load(componentId, objectId, contentLanguage);
      }
      if (wysiwygTextValue == null) {
        wysiwygTextValue = "";
      }
    } catch (WysiwygException exc) {
      // no file
    }
  }

  LocalizationBundle message =
      ResourceLocator.getLocalizationBundle("org.silverpeas.wysiwyg.multilang.wysiwygBundle", language);
%>

<fmt:setLocale value="<%=language%>"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<view:componentParam var="commentActivated" componentId="<%=componentId%>" parameter="tabComments"/>
<c:if test="${not silfn:booleanValue(commentActivated)}">
  <view:componentParam var="commentActivated" componentId="<%=componentId%>" parameter="comments"/>
</c:if>
<c:set var="actionWysiwyg" value="<%=actionWysiwyg%>"/>

<c:set var="isHtmlLoadingContext" value="${actionWysiwyg eq 'Load' or actionWysiwyg eq 'Refresh'}"/>
<c:set var="handledSubscriptionType" value="${param.handledSubscriptionType}"/>
<c:set var="handledSubscriptionResourceId" value="${param.handledSubscriptionResourceId}"/>
<c:set var="subscriptionManagementContext" value="${requestScope.subscriptionManagementContext}"/>
<c:set var="wysiwygTextValue" value="<%=wysiwygTextValue%>"/>
<c:if test="${not empty subscriptionManagementContext}">
  <jsp:useBean id="subscriptionManagementContext" type="org.silverpeas.core.subscription.util.SubscriptionManagementContext"/>
  <c:if test="${subscriptionManagementContext.entityStatusBeforePersistAction.validated
              and subscriptionManagementContext.entityStatusAfterPersistAction.validated
              and subscriptionManagementContext.entityPersistenceAction.update}">
    <c:set var="handledSubscriptionType" value="${subscriptionManagementContext.linkedSubscriptionResource.type.name}"/>
    <c:set var="handledSubscriptionResourceId" value="${subscriptionManagementContext.linkedSubscriptionResource.id}"/>
  </c:if>
</c:if>
<c:set var="isHandledSubscriptionConfirmation"
       value="${not empty handledSubscriptionType and not empty handledSubscriptionResourceId}"/>

<view:sp-page>
<view:sp-head-part noLookAndFeel="${not isHtmlLoadingContext}">
  <c:if test="${isHtmlLoadingContext}">
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="subscription"/>
  </c:if>
</view:sp-head-part>
<view:sp-body-part>
<c:if test="${isHtmlLoadingContext}">
<view:browseBar componentId="<%=componentId%>" extraInformations="<%=browseInformation%>"/>
<view:window>

<script type="text/javascript">

  var $deferred;

  <c:if test="${not empty param.notySuccess}">
  $(document).ready(function(){
    notySuccess('${silfn:escapeJs(param.notySuccess)}');
  });
  </c:if>

  window.onload = function() {
    <view:wysiwyg replace="editor1" language="<%=language %>"
      spaceLabel="<%=spaceLabel%>" componentId="<%=componentId%>" componentLabel="<%=componentLabel%>"
      browseInfo="<%=browseInformation%>" objectId="<%=objectId%>" objectType="<%=objectType%>"
      activateWysiwygBackupManager="true" />

    if ($.trim($(".wysiwyg-fileStorage").text()).length == 0) {
      $(".wysiwyg-fileStorage").css("display", "none");
    }
  };

  function saveCalledByFormSubmit() {
    $deferred = $.Deferred();
    document.recupHtml.actionWysiwyg.value = "SaveHtml";
    jQuery(document.recupHtml).submit();
    $deferred.then(function(mustReload) {
      if (!mustReload) {
        notySuccess('<fmt:message key="GML.validation.update"/>');
        $.closeProgressMessage();
      } else {
        reload.call(this);
      }
    });
  }

  function saveAndExit() {
    $deferred = $.Deferred();
    document.recupHtml.actionWysiwyg.value = "SaveHtmlAndExit";
    CKEDITOR.instances.editor1.updateElement();
    jQuery(document.recupHtml).submit();
    $deferred.then(function() {
      location.href = '<%=WebEncodeHelper.javaStringToJsString(returnUrl)%>';
    });
  }

  function choixLien() {
    var index = document.recupHtml.liens.selectedIndex;
    var str = document.recupHtml.liens.options[index].value;
    if (index != 0 && str != null) {
      CKEDITOR.instances.editor1.insertHtml('<a href="' + str + '">' +
      str.substring(str.lastIndexOf("/") + 1) + "</a>");
    }
  }

</script>

<form method="post" name="recupHtml" action="javascript:saveCalledByFormSubmit();">

  <% if (I18NHelper.isI18nContentActivated && StringUtil.isDefined(contentLanguage)) { %>
  <div class="container-wysiwyg wysiwyg-language"><%=I18NHelper
      .getLanguageLabel(contentLanguage, language)%>
  </div>
  <% } %>

  <div class="container-wysiwyg wysiwyg-fileStorage">

    <viewTags:displayToolBarWysiwyg
        editorName="editor1"
        componentId="<%=componentId%>"
        objectId="<%=objectId%>"
        path="<%=path%>"/>

    <%
      // Only for WebSites
      if (collectionPages != null) { %>
    <select name="liens" onchange="choixLien(); this.selectedIndex=0">
      <option selected="selected"><%=message.getString("Links")%>
      </option>
      <% for (int i = 0; i < collectionPages.length; i++) { %>
      <option value="<%=specificURL+collectionPages[i][0] %>"><%=collectionPages[i][1] %>
      </option>
      <% } %>
    </select>
    <% } %>
  </div>

  <div class="container-wysiwyg wysiwyg-area">
    <textarea id="editor1" name="editor1" cols="10" rows="10" style="display: none"><c:out value="<%=wysiwygTextValue%>" escapeXml="true"/></textarea>
  </div>

  <input name="actionWysiwyg" type="hidden" value="SaveHtml"/>
  <input name="origin" type="hidden" value="<%=componentId%>"/>
  <c:if test="${isHandledSubscriptionConfirmation}">
    <input name="handledSubscriptionType" type="hidden" value="${handledSubscriptionType}"/>
    <input name="handledSubscriptionResourceId" type="hidden" value="${handledSubscriptionResourceId}"/>
  </c:if>
  <c:set var="saveLabel"><%=message.getString("SaveAndExit")%></c:set>
  <c:set var="cancelLabel"><%=message.getString("GML.back")%></c:set>
  <c:set var="cancelAction"><%="javascript:sp.editor.wysiwyg.lastBackupManager.clear();sp.navRequest('" + returnUrl + "').go();"%></c:set>
  <br/>
  <view:buttonPane>
    <view:button label="${saveLabel}" action="javascript:onclick=saveAndExit();"/>
    <view:button label="${cancelLabel}" action="${cancelAction}"/>
  </view:buttonPane>
  <script type="text/javascript">

    function commit(mustReload) {
      $.progressMessage();
      $.post('<c:url value="/wysiwyg/jsp/htmlEditor.jsp"/>', $(document.recupHtml).serialize(),
          function() {
            sp.editor.wysiwyg.lastBackupManager.clear();
            $deferred.resolve(mustReload);
          })
          .fail(function(request) {
            $deferred.reject(request);
            notyError(request.responseText);
            $.closeProgressMessage();
          });
    }

    function reload() {
      $(document.recupHtml).append($('<input>', {'type': 'hidden', 'name':'notySuccess'}).val('<fmt:message key="GML.validation.update"/>'));
      document.recupHtml.actionWysiwyg.value = 'Refresh';
      document.recupHtml.action = '<c:url value="/wysiwyg/jsp/htmlEditor.jsp"/>';
      document.recupHtml.submit();
    }

    jQuery(document).ready(function() {
      jQuery(document.recupHtml).submit(function() {
        <c:choose>
          <c:when test="${silfn:isDefined(wysiwygTextValue) and isHandledSubscriptionConfirmation}">
          jQuery.subscription.confirmNotificationSendingOnUpdate({
            comment : {
              saveNote : ${silfn:booleanValue(commentActivated)},
              contributionLocalId : '<%=objectId%>',
              contributionType : '<%=objectType%>',
              contributionIndexable : <%=indexIt%>
            },
            subscription : {
              componentInstanceId : '<%=componentId%>',
              type : '${handledSubscriptionType}',
              resourceId : '${handledSubscriptionResourceId}'
            }, callback : function() {
              commit.call(this, false);
            }
          });
          </c:when>
          <c:otherwise>
            commit.call(this, ${silfn:isNotDefined(wysiwygTextValue) ? 'true' : 'false'});
          </c:otherwise>
        </c:choose>
        return false;
      });
    });
  </script>
</form>
<view:progressMessage/>
</view:window>
</c:if>
</view:sp-body-part>
</view:sp-page>
