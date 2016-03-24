<%--
  Copyright (C) 2000 - 2015 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment"/>

<%@ attribute name="domSelector" required="true"
              type="java.lang.String"
              description="The DOM selector that permits to identify the drop zone" %>
<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>
<%@ attribute name="resourceId" required="true"
              type="java.lang.String"
              description="The identifier of the resource the uploaded document must be attached to" %>
<%@ attribute name="contentLanguage" required="true"
              type="java.lang.String"
              description="The content language in which the attachment is uploaded" %>
<%@ attribute name="hasToBeIndexed" required="true"
              type="java.lang.Boolean"
              description="Indicates if the attachment must be indexed" %>
<%@ attribute name="documentType" required="true"
              type="java.lang.String"
              description="Indicates the type of attachment (attachment, wysiwyg, image, ...)" %>

<%@ attribute name="greatestUserRole" required="false"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The greatest role the user has" %>
<c:if test="${empty greatestUserRole}">
  <c:set var="greatestUserRole" value="${silfn:getGreatestRoleOfCurrentUserOn(componentInstanceId)}"/>
</c:if>
<%@ attribute name="helpCoverClass" required="false"
              type="java.lang.String"
              description="Specify a class to change display of help access icon" %>
<c:if test="${empty helpCoverClass}">
  <c:set var="helpCoverClass" value="droparea-cover-help-attachment"/>
</c:if>
<%@ attribute name="handledSubscriptionType" required="false"
              type="java.lang.String"
              description="The the subscription notification type to manage, if any." %>
<%@ attribute name="handledSubscriptionResourceId" required="false"
              type="java.lang.String"
              description="The the resource id of subscription notification to manage, if any." %>
<c:set var="isHandledSubscriptionConfirmation"
       value="${not empty handledSubscriptionType and not empty handledSubscriptionResourceId}"/>

<view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.writer"/>
<jsp:useBean id="writerRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:if test="${greatestUserRole.isGreaterThanOrEquals(writerRole)}">

  <c:set var="domIdSuffix" value="${fn:replace(resourceId, '-', '_')}"/>

  <c:set var="_ddIsI18n" value="${silfn:isI18n() && silfn:isDefined(contentLanguage)}"/>

  <view:componentParam var="publicationAlwaysVisiblePramValue" componentId="${componentInstanceId}" parameter="publicationAlwaysVisible"/>
  <view:componentParam var="isComponentVersioned" componentId="${componentInstanceId}" parameter="versionControl"/>
  <c:set var="isPublicationAlwaysVisible" value="${silfn:booleanValue(publicationAlwaysVisiblePramValue)}"/>
  <c:set var="isVersionActive" value="${not isPublicationAlwaysVisible and silfn:booleanValue(isComponentVersioned)}"/>

  <view:includePlugin name="dragAndDropUpload"/>

  <c:url var="uploadCompletedUrl" value="/DragAndDrop/drop">
    <c:param name="ComponentId" value="${componentInstanceId}"/>
    <c:param name="ResourceId" value="${resourceId}"/>
    <c:param name="IndexIt" value="${hasToBeIndexed}"/>
    <c:param name="DocumentType" value="${documentType}"/>
  </c:url>

  <c:url var="helpUrl" value="/upload/Attachment_${userLanguage}.jsp">
    <c:if test="${isVersionActive}">
      <c:param name="mode" value="version"/>
    </c:if>
  </c:url>

  <c:if test="${_ddIsI18n or isVersionActive}">
    <div id="validationDialog${domIdSuffix}" class="form-container" style="display: none;">
      <c:if test="${_ddIsI18n}">
        <br/>

        <div>
          <label for="ddLangCreateId${domIdSuffix}" class="label"><fmt:message key="GML.language"/></label>
          <span class="champ-ui-dialog">
            <view:langSelect elementName="ddLangCreate${domIdSuffix}"
                             elementId="ddLangCreateId${domIdSuffix}"
                             langCode="${contentLanguage}"
                             includeLabel="false"/>
          </span>

          <div style="height: 2px"></div>
        </div>
      </c:if>
      <c:if test="${isVersionActive}">
        <br/>

        <div>
          <span class="label"><fmt:message key="attachment.dragAndDrop.question"/></span>

          <div>
            <input value="0" type="radio" name="versionType${domIdSuffix}" id="publicVersion${domIdSuffix}" checked="checked"/>
            <label for="publicVersion${domIdSuffix}"><fmt:message key="attachment.version_public.label"/></label><br/>
            <input value="1" type="radio" name="versionType${domIdSuffix}" id="workVersion${domIdSuffix}"/>
            <label for="workVersion${domIdSuffix}"><fmt:message key="attachment.version_wip.label"/></label>
          </div>
        </div>
      </c:if>
    </div>
  </c:if>

  <script type="text/JavaScript">
    (function() {
      var options = {
        domSelector : '${domSelector}',
        componentInstanceId : "${componentInstanceId}",
        onCompletedUrl : "${uploadCompletedUrl}",
        onCompletedUrlSuccess : uploadCompleted,
        helpContentUrl : "${helpUrl}",
        helpCoverClass : "${helpCoverClass}"
      };

      var _performDdWithPotentialNotification = function (fileUpload, resolve, reject) {
        <c:choose>
        <c:when test="${isHandledSubscriptionConfirmation}">
        var rejectOnClose = true;
        $.subscription.confirmNotificationSendingOnUpdate({
          subscription : {
            componentInstanceId : '${componentInstanceId}',
            type : '${handledSubscriptionType}',
            resourceId : '${handledSubscriptionResourceId}'
          },
          callback : function(userResponse) {
            rejectOnClose = false;
            var ajaxOptions = userResponse.applyOnAjaxOptions();
            fileUpload.uploadSession.onCompleted.urlHeaders = ajaxOptions.headers;
            resolve();
          },
          callbackOnClose : function() {
            if (rejectOnClose) {
              reject();
            }
          }
        });
        </c:when>
        <c:otherwise>
        resolve();
        </c:otherwise>
        </c:choose>
      };

      <c:choose>
      <c:when test="${_ddIsI18n or isVersionActive}">
      options.beforeSend = function(fileUpload) {
        if (fileUpload.uploadSession.id) {
          return Promise.resolve();
        }
        return new Promise(function(resolve, reject) {
          var rejectOnClose = true;
          jQuery('#validationDialog${domIdSuffix}').popup('validation', {
            title : '<fmt:message key="attachment.dragAndDrop.title" />',
            buttonDisplayed : true,
            isMaxWidth : true,
            callback : function() {
              rejectOnClose = false;
              var uploadCompletedUrl = '${uploadCompletedUrl}';
              <c:if test="${_ddIsI18n}">
              var contentLanguage = jQuery('select[name=ddLangCreate${domIdSuffix}]', this).val();
              uploadCompletedUrl += '&ContentLanguage=' + contentLanguage;
              </c:if>
              <c:if test="${isVersionActive}">
              var version = jQuery('input[name=versionType${domIdSuffix}]:checked', this).val();
              uploadCompletedUrl += '&Type=' + version;
              </c:if>
              fileUpload.uploadSession.onCompleted.url = uploadCompletedUrl;
              _performDdWithPotentialNotification.call(this, fileUpload, resolve, reject);
              return true;
            },
            callbackOnClose : function() {
              if (rejectOnClose) {
                reject();
              }
            }
          });
        });
      };
      </c:when>
      <c:otherwise>
      options.beforeSend = function(fileUpload) {
        if (fileUpload.uploadSession.id) {
          return Promise.resolve();
        }
        return new Promise(function(resolve, reject) {
          _performDdWithPotentialNotification.call(this, fileUpload, resolve, reject);
        });
      };
      </c:otherwise>
      </c:choose>

      initDragAndDropUploadAndReload(options);
    })();
  </script>
</c:if>