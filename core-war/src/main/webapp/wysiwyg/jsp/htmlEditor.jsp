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

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.silvertrace.SilverTrace" %>
<%@ page import="org.silverpeas.core.util.EncodeHelper" %>
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
  String spaceId = "";
  String componentId = "";
  String spaceName = "";
  String componentName = "";
  String objectId = "";
  String language = "";
  String contentLanguage = "";
  String codeWysiwyg = "";
  String returnUrl = null;
  String browseInformation = null;
  String fileName = "";
  String path = "";
  String[][] collectionPages = null;
  String specificURL = "";    //For Websites only

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
    spaceId = (String) session.getAttribute("WYSIWYG_SpaceId");
    spaceName = (String) session.getAttribute("WYSIWYG_SpaceName");
    componentId = (String) session.getAttribute("WYSIWYG_ComponentId");
    componentName = (String) session.getAttribute("WYSIWYG_ComponentName");
    objectId = (String) session.getAttribute("WYSIWYG_ObjectId");
    browseInformation = (String) session.getAttribute("WYSIWYG_BrowseInfo");
    language = (String) session.getAttribute("WYSIWYG_Language");
    contentLanguage = (String) session.getAttribute("WYSIWYG_ContentLanguage");
    returnUrl = (String) session.getAttribute("WYSIWYG_ReturnUrl");
    userId = (String) session.getAttribute("WYSIWYG_UserId");
    fileName = (String) session.getAttribute("WYSIWYG_FileName");
    path = (String) session.getAttribute("WYSIWYG_Path");
    specificURL = (String) session.getAttribute("WYSIWYG_SpecificURL");
    indexIt = (String) session.getAttribute("WYSIWYG_IndexIt");

    if ("SaveHtmlAndExit".equals(actionWysiwyg) || "SaveHtml".equals(actionWysiwyg)) {
      //For parsing absolute url (Bug FCKEditor)
      String server = request.getRequestURL()
          .substring(0, request.getRequestURL().toString().lastIndexOf(context));
      int serverPort = request.getServerPort();
      if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
        codeWysiwyg = codeWysiwyg.replaceAll("../../../../../", "/");
        codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
        codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
      } else {
        codeWysiwyg = codeWysiwyg.replaceAll("../../../../", context + "/");
        codeWysiwyg = codeWysiwyg.replaceAll(server + ":" + serverPort, "");
        codeWysiwyg = codeWysiwyg.replaceAll(server + "/", "/");
      }

      if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
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
      if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
        collectionPages = WysiwygController.getWebsitePages(path, componentId);
        SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
            "nb collectionPages = " + collectionPages.length);
      }
    }
    if ("SaveHtmlAndExit".equals(actionWysiwyg)) {
      session.removeAttribute("WYSIWYG_ContentLanguage");
    }
  } else if ("Load".equals(actionWysiwyg)) {

    spaceId = request.getParameter("SpaceId");
    if (spaceId == null) {
      spaceId = (String) request.getAttribute("SpaceId");
    }

    spaceName = request.getParameter("SpaceName");
    if (spaceName == null) {
      spaceName = (String) request.getAttribute("SpaceName");
    }

    componentId = request.getParameter("ComponentId");
    if (componentId == null) {
      componentId = (String) request.getAttribute("ComponentId");
    }

    componentName = request.getParameter("ComponentName");
    if (componentName == null) {
      componentName = (String) request.getAttribute("ComponentName");
    }

    objectId = request.getParameter("ObjectId");
    if (objectId == null) {
      objectId = (String) request.getAttribute("ObjectId");
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

    if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
      collectionPages = WysiwygController.getWebsitePages(path, componentId);
      SilverTrace.info("wysiwyg", "Wysiwyg.htmlEditorJSP", "root.MSG_GEN_PARAM_VALUE",
          "nb collectionPages = " + collectionPages.length);
      specificURL = "/website/" + componentId + "/" + objectId + "/";
    } else {
      specificURL = context;
    }
    session.setAttribute("WYSIWYG_SpecificURL", specificURL);

    try {
      if (componentId.startsWith(WysiwygController.WYSIWYG_WEBSITES)) {
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

<c:set var="actionWysiwyg" value="<%=actionWysiwyg%>"/>

<c:set var="handledSubscriptionType" value="${param.handledSubscriptionType}"/>
<c:set var="handledSubscriptionResourceId" value="${param.handledSubscriptionResourceId}"/>
<c:set var="subscriptionManagementContext" value="${requestScope.subscriptionManagementContext}"/>
<c:set var="wysiwygTextValue" value="<%=wysiwygTextValue%>"/>
<c:if test="${not empty subscriptionManagementContext}">
  <jsp:useBean id="subscriptionManagementContext" type="org.silverpeas.core.subscription.util.SubscriptionManagementContext"/>
  <c:if test="${subscriptionManagementContext.entityStatusBeforePersistAction.validated
              and subscriptionManagementContext.entityStatusAfterPersistAction.validated
              and subscriptionManagementContext.entityPersistenceAction.update}">
    <c:set var="handledSubscriptionType" value="${subscriptionManagementContext.linkedSubscriptionResource.type}"/>
    <c:set var="handledSubscriptionResourceId" value="${subscriptionManagementContext.linkedSubscriptionResource.id}"/>
  </c:if>
</c:if>
<c:set var="isHandledSubscriptionConfirmation"
       value="${not empty handledSubscriptionType and not empty handledSubscriptionResourceId}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Silverpeas Wysiwyg Editor</title>
  <c:if test="${actionWysiwyg eq 'Load' or actionWysiwyg eq 'Refresh'}">
    <view:looknfeel/>
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="subscription"/>
  </c:if>
</head>
<body>
<c:if test="${actionWysiwyg eq 'Load' or actionWysiwyg eq 'Refresh'}">
<view:browseBar componentId="<%=componentId%>" extraInformations="<%=browseInformation%>"/>
<view:window>

<script type="text/javascript">

  var $deferred;

  <c:if test="${not empty param.notySuccess}">
  $(document).ready(function(){
    notySuccess('${param.notySuccess}');
  });
  </c:if>

  window.onload = function() {
    <view:wysiwyg replace="editor1" language="<%=language %>"
      spaceId="<%=spaceId%>" spaceName="<%=spaceName%>" componentId="<%=componentId%>" componentName="<%=componentName%>"
      browseInfo="<%=browseInformation%>" objectId="<%=objectId%>" />

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
      location.href = '<%=EncodeHelper.javaStringToJsString(returnUrl)%>';
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
    <textarea id="editor1" name="editor1" cols="10" rows="10"><c:out value="<%=wysiwygTextValue%>" escapeXml="true"/></textarea>
  </div>

  <input name="actionWysiwyg" type="hidden" value="SaveHtml"/>
  <input name="origin" type="hidden" value="<%=componentId%>"/>
  <c:if test="${isHandledSubscriptionConfirmation}">
    <input name="handledSubscriptionType" type="hidden" value="${handledSubscriptionType}"/>
    <input name="handledSubscriptionResourceId" type="hidden" value="${handledSubscriptionResourceId}"/>
  </c:if>
  <c:set var="saveLabel"><%=message.getString("SaveAndExit")%></c:set>
  <c:set var="cancelLabel"><%=message.getString("GML.back")%></c:set>
  <c:set var="cancelAction"><%="javascript:location.href='" + returnUrl + "';"%></c:set>
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
            $deferred.resolve(mustReload);
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
</body>
</html>