<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<c:set var="resources" value="${requestScope.resources}"/>
<jsp:useBean id="resources" type="org.silverpeas.core.util.MultiSilverpeasBundle"/>
<view:setBundle bundle="${resources.multilangBundle}"/>

<c:set var="defaultEditorImage" value="${resources.getSetting('ddwe.editor.img.src.default')}" />
<c:set var="specificJsPath" value="${resources.getSetting('ddwe.editor.specific.js.path', '')}" />
<c:set var="specificCssPath" value="${resources.getSetting('ddwe.editor.specific.css.path', '')}" />
<c:set var="componentCssUrl" value='<%=JavascriptPluginInclusion.normalizeWebResourceUrl(URLUtil.getApplicationURL() + "/ddwe/jsp/styleSheets/silverpeas-grapes-canvas.css")%>'/>
<c:set var="componentAddonCssUrl" value='<%=JavascriptPluginInclusion.normalizeWebResourceUrl(URLUtil.getApplicationURL() + "/ddwe/jsp/styleSheets/silverpeas-ddwe-canvas-addon.css")%>'/>

<fmt:message key="ddwe.webBrowser.seeInto" var="seeIntoWebBrowserLabel" />
<fmt:message key="ddwe.mail.sendToMe" var="sendToMeLabel" />
<fmt:message key="ddwe.grapes.cmtTglImagesLabel" var="cmtTglImagesLabel" />
<fmt:message key="ddwe.grapes.cmdBtnUndoLabel" var="cmdBtnUndoLabel" />
<fmt:message key="ddwe.grapes.cmdBtnRedoLabel" var="cmdBtnRedoLabel" />
<fmt:message key="ddwe.grapes.clearCanvas" var="clearCanvas" />
<fmt:message key="ddwe.grapes.clearCanvasConfirm" var="clearCanvasConfirm" />
<fmt:message key="GML.validate" var="validate" />
<fmt:message key="GML.cancel" var="cancel" />
<fmt:message key="ddwe.grapes.finalHtml" var="finalHtml" />
<fmt:message key="ddwe.grapes.htmlSource" var="htmlSource" />
<fmt:message key="ddwe.grapes.editHtml" var="editHtml" />
<fmt:message key="ddwe.editor.component.categories.sp" var="silverpeasCategoryLabel" />
<fmt:message key="ddwe.editor.component.simpleBlock.title" var="simpleBlockTitle" />
<fmt:message key="ddwe.editor.component.contribution.title" var="contributionBlockTitle" />
<fmt:message key="ddwe.editor.component.contribution.content.title" var="contributionBlockContentTitle" />
<fmt:message key="ddwe.editor.component.contribution.content" var="contributionBlockContent" />
<fmt:message key="ddwe.editor.component.contribution.content.readMore" var="contributionBlockContentReadMore" />
<fmt:message key="ddwe.editor.component.event.title" var="eventBlockTitle" />
<fmt:message key="ddwe.editor.component.event.content.title" var="eventBlockContentTitle" />
<fmt:message key="GML.date.the" var="eventBlockContentAt" />
<fmt:message key="GML.date.from" var="eventBlockContentFrom" />
<fmt:message key="GML.date.to" var="eventBlockContentTo" />
<fmt:message key="ddwe.editor.component.event.content" var="eventBlockDescription" />
<fmt:message key="ddwe.editor.component.event.content.open" var="eventBlockOpen" />
<fmt:message key="ddwe.editor.component.imageWithLink.title" var="imageWithLinkBlockTitle" />
<fmt:message key="ddwe.editor.component.toolbar.fa-arrow-up" var="fa_arrow_up_Label" />
<fmt:message key="ddwe.editor.component.toolbar.fa-arrows" var="fa_arrows_Label" />
<fmt:message key="ddwe.editor.component.toolbar.fa-clone" var="fa_clone_Label" />
<fmt:message key="ddwe.editor.component.toolbar.fa-trash-o" var="fa_trash_o_Label" />
<fmt:message key="ddwe.editor.component.toolbar.fa-sp-basket-selector" var="fa_sp_basket_selector_Label" />

<fmt:message key="ddwe.menu.action" var="editionLabel" />

<c:set var="mailMode" value="${silfn:booleanValue(requestScope.mode.mail)}"/>
<c:set var="validateUrl" value="${requestScope.validateUrl}"/>
<c:set var="cancelUrl" value="${requestScope.cancelUrl}"/>
<c:set var="browseBarPath" value="${requestScope.browseBarPath}"/>
<c:if test="${browseBarPath != null}">
  <jsp:useBean id="browseBarPath" type="java.util.List<org.silverpeas.core.util.Pair<java.lang.String, java.lang.String>>"/>
</c:if>
<c:set var="user" value="${requestScope.DdweUser}"/>
<jsp:useBean id="user" type="org.silverpeas.core.wbe.WbeUser"/>
<c:set var="file" value="${requestScope.DdweFile}"/>
<jsp:useBean id="file" type="org.silverpeas.core.contribution.content.ddwe.DragAndDropWbeFile"/>
<c:set var="currentFileContent" value="${requestScope.currentFileContent}"/>

<c:set var="foreignContributionId" value="${file.linkedToContribution().get()}"/>
<jsp:useBean id="foreignContributionId" type="org.silverpeas.core.contribution.model.ContributionIdentifier"/>
<c:set var="componentId" value="${foreignContributionId.componentInstanceId}"/>

<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="imageselector"/>
    <view:includePlugin name="basketselection"/>
    <c:if test="${not empty specificCssPath}">
      <script type="text/css" src="${specificCssPath}"></script>
    </c:if>
    <c:if test="${not empty specificJsPath}">
      <script type="text/javascript" src="${specificJsPath}"></script>
    </c:if>
    <view:script src="/ddwe/jsp/javaScript/silverpeas-ddwe-components.js"/>
    <view:script src="/ddwe/jsp/javaScript/silverpeas-ddwe-addon.js"/>
    <view:link href="/ddwe/jsp/grapesjs/css/grapes.min.css"/>
    <view:link href="/ddwe/jsp/grapesjs/css/grapesjs-preset-newsletter.min.css"/>
    <view:link href="/ddwe/jsp/styleSheets/silverpeas-grapes.css"/>
    <view:link href="/ddwe/jsp/styleSheets/silverpeas-ddwe-addon.css"/>
    <view:script src="/ddwe/jsp/grapesjs/i18n/locale/${currentUserLanguage}.js"/>
    <view:script src="/ddwe/jsp/grapesjs/grapes.min.js"/>
    <view:script src="/ddwe/jsp/grapesjs/grapesjs-preset-newsletter.min.js"/>
    <view:script src="/ddwe/jsp/grapesjs/grapesjs-plugin-ckeditor.min.js"/>
    <view:script src="/ddwe/jsp/javaScript/silverpeas-ddwe.js"/>
    <script type="text/javascript">
      window.DdweBundle = new SilverpeasPluginBundle({
        'cmtTglImagesLabel' : '${silfn:escapeJs(cmtTglImagesLabel)}',
        'cmdBtnUndoLabel' : '${silfn:escapeJs(cmdBtnUndoLabel)}',
        'cmdBtnRedoLabel' : '${silfn:escapeJs(cmdBtnRedoLabel)}',
        'clearCanvas' : '${silfn:escapeJs(clearCanvas)}',
        'clearCanvasConfirm' : '${silfn:escapeJs(clearCanvasConfirm)}',
        'validate' : '${silfn:escapeJs(validate)}',
        'cancel' : '${silfn:escapeJs(cancel)}',
        'finalHtml' : '${silfn:escapeJs(finalHtml)}',
        'htmlSource' : '${silfn:escapeJs(htmlSource)}',
        'editHtml' : '${silfn:escapeJs(editHtml)}',
        'silverpeasCategoryLabel' : '${silfn:escapeJs(silverpeasCategoryLabel)}',
        'simpleBlockTitle' : '${silfn:escapeJs(simpleBlockTitle)}',
        'contributionBlockTitle' : '${silfn:escapeJs(contributionBlockTitle)}',
        'contributionBlockContentTitle' : '${silfn:escapeJs(contributionBlockContentTitle)}',
        'contributionBlockContent' : '${silfn:escapeJs(contributionBlockContent)}',
        'contributionBlockContentReadMore' : '${silfn:escapeJs(contributionBlockContentReadMore)}',
        'eventBlockTitle' : '${silfn:escapeJs(eventBlockTitle)}',
        'eventBlockContentTitle' : '${silfn:escapeJs(eventBlockContentTitle)}',
        'eventBlockContentAt' : '${silfn:escapeJs(eventBlockContentAt)}',
        'eventBlockContentFrom' : '${silfn:escapeJs(eventBlockContentFrom)}',
        'eventBlockContentTo' : '${silfn:escapeJs(eventBlockContentTo)}',
        'eventBlockDescription' : '${silfn:escapeJs(eventBlockDescription)}',
        'eventBlockOpen' : '${silfn:escapeJs(eventBlockOpen)}',
        'imageWithLinkBlockTitle' : '${silfn:escapeJs(imageWithLinkBlockTitle)}',
        'fa_arrow_up_Label' : '${silfn:escapeJs(fa_arrow_up_Label)}',
        'fa_arrows_Label' : '${silfn:escapeJs(fa_arrows_Label)}',
        'fa_clone_Label' : '${silfn:escapeJs(fa_clone_Label)}',
        'fa_trash_o_Label' : '${silfn:escapeJs(fa_trash_o_Label)}',
        'fa_sp_basket_selector_Label' : '${silfn:escapeJs(fa_sp_basket_selector_Label)}'
      });
      function __applyRequestCommonParams(request) {
        return request.withParam("access_token", '${user.accessToken}')
                      .withParam("file_id", '${file.id()}');
      }
      <c:if test="${mailMode}">
      function sendToMe() {
        __applyRequestCommonParams(sp.ajaxRequest(webContext + '/Rddwe/jsp/sendToMe')).send();
      }
      </c:if>
      function renderIntoBrowser() {
        __applyRequestCommonParams(sp.navRequest(webContext + '/Rddwe/jsp/result'))
            .toTarget("_blank")
            .go();
      }
      function validate() {
        performUrl('${validateUrl}');
      }
      function cancel() {
        __applyRequestCommonParams(sp.ajaxRequest(webContext + '/Rddwe/jsp/rstTmpContent'))
          .byPostMethod()
          .send()
          .then(function() {
            performUrl('${cancelUrl}');
          });
      }
      function goBack(url) {
        performUrl(url);
      }
      function performUrl(url) {
        if (url) {
          if (spWindow) {
            if (url.startsWith(webContext)) {
              spWindow.loadLink(url);
              return;
            }
          }
          sp.navRequest(url).go();
        }
      }
      whenSilverpeasReady().then(function() {
        sp.element.querySelectorAll('#browseBar a').forEach(function($el) {
          $el.addEventListener('click', function(e) {
            const href = e.target.getAttribute('href');
            if (href.startsWith('javascript')) {
              cancel();
            } else {
              e.stopPropagation();
              e.preventDefault();
              goBack(href);
            }
          });
        });
      });
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${componentId}">
      <c:if test="${browseBarPath != null}">
        <c:forEach var="broweBarPathPart" items="${browseBarPath}">
          <view:browseBarElt label="${broweBarPathPart.first}" link="${broweBarPathPart.second}"/>
        </c:forEach>
      </c:if>
      <view:browseBarElt label="${editionLabel}"/>
    </view:browseBar>
    <view:operationPane>
      <c:if test="${mailMode}">
        <view:operation action="javascript:sendToMe()" altText="${sendToMeLabel}"/>
      </c:if>
      <view:operation action="javascript:renderIntoBrowser()" altText="${seeIntoWebBrowserLabel}"/>
    </view:operationPane>
    <view:window>
      <view:frame>
        <div id="gjs">${currentFileContent}</div>
        <div id="plugin-manager">
          <silverpeas-image-selector v-on:api="setImageSelectorApi"></silverpeas-image-selector>
          <silverpeas-publication-basket-selector v-on:api="setBasketSelectionApi"></silverpeas-publication-basket-selector>
        </div>
      </view:frame>
    </view:window>
    <script type="text/javascript">
      function choixImageInGallery(url) {
        window.vm.imageSelectorApi.updateSelectedImageMedia(url);
      }
      window.vm = new Vue({
        el : '#plugin-manager',
        provide : function() {
          return {
            context: this.context
          }
        },
        data : function() {
          return {
            imageSelectorApi : undefined,
            basketSelectionApi : undefined,
            context : {
              currentUser : currentUser,
              contributionId : sp.contribution.id.fromString('${foreignContributionId.asString()}')
            }
          }
        },
        methods : {
          setBasketSelectionApi : function(api) {
            this.basketSelectionApi = api;
            this.startEditor();
          },
          setImageSelectorApi : function(api) {
            this.imageSelectorApi = api;
            this.startEditor();
          },
          startEditor : function() {
            if (!this.basketSelectionApi || !this.imageSelectorApi) {
              return;
            }
            whenSilverpeasReady(function() {
              const editorManager = new DragAndDropWebEditorManager({
                componentCssUrl : ['${componentCssUrl}', '${componentAddonCssUrl}'],
                defaultEditorImageSrc : '${defaultEditorImage}',
                imageSelectorApi : this.imageSelectorApi,
                basketSelectionApi : this.basketSelectionApi,
                foreignContributionId : this.context.contributionId,
                userToken : '${user.accessToken}',
                fileId : '${file.id()}',
                connectors : {
                  validate : validate,
                  cancel : cancel
                }
              });
              editorManager.ready(function() {
                document.dispatchEvent(new CustomEvent('ddwe-editor-manager-loaded', {
                  detail : editorManager,
                  bubbles : true,
                  cancelable : true
                }));
              }.bind(this));
            }.bind(this));
          }
        }
      });
    </script>
  </view:sp-body-part>
</view:sp-page>