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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="browseLabel" key="GML.upload.choose.browse"/>
<fmt:message var="chooseFileLabel" key="GML.upload.choose.file"/>
<fmt:message var="chooseFilesLabel" key="GML.upload.choose.files"/>
<fmt:message var="dragAndDropFileLabel" key="GML.upload.dragAndDrop.file"/>
<fmt:message var="dragAndDropFilesLabel" key="GML.upload.dragAndDrop.files"/>
<fmt:message var="sendingFileLabel" key="GML.upload.sending.file"><fmt:param value="@name@"/></fmt:message>
<fmt:message var="sendingFilesLabel" key="GML.upload.sending.files"><fmt:param value="@number@"/></fmt:message>
<fmt:message var="sendingWaitingWarningLabel" key="GML.upload.warning"/>
<fmt:message var="limitFileWarningLabel" key="GML.upload.warning.file.limit"/>
<fmt:message var="limitFilesWarningLabel" key="GML.upload.warning.files.limit"><fmt:param value="@number@"/></fmt:message>
<fmt:message var="limitFileReachedLabel" key="GML.upload.warning.file.limit.reached"/>
<fmt:message var="limitFilesReachedLabel" key="GML.upload.warning.files.limit.reached"><fmt:param value="@number@"/></fmt:message>
<fmt:message var="titleLabel" key="GML.title"/>
<fmt:message var="descriptionLabel" key="GML.description"/>
<fmt:message var="deleteFileLabel" key="GML.delete"/>
<fmt:message var="attachmentsLabel" key="GML.attachments"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.browse = '${silfn:escapeJs(browseLabel)}'"></span>
  <span ng-init="$ctrl.labels.chooseFile = '${silfn:escapeJs(chooseFileLabel)}'"></span>
  <span ng-init="$ctrl.labels.chooseFiles = '${silfn:escapeJs(chooseFilesLabel)}'"></span>
  <span ng-init="$ctrl.labels.dragAndDropFile = '${silfn:escapeJs(dragAndDropFileLabel)}'"></span>
  <span ng-init="$ctrl.labels.dragAndDropFiles = '${silfn:escapeJs(dragAndDropFilesLabel)}'"></span>
  <span ng-init="$ctrl.labels.sendingFile = '${silfn:escapeJs(sendingFileLabel)}'"></span>
  <span ng-init="$ctrl.labels.sendingFiles = '${silfn:escapeJs(sendingFilesLabel)}'"></span>
  <span ng-init="$ctrl.labels.sendingWaitingWarning = '${silfn:escapeJs(sendingWaitingWarningLabel)}'"></span>
  <span ng-init="$ctrl.labels.limitFileWarning = '${silfn:escapeJs(limitFileWarningLabel)}'"></span>
  <span ng-init="$ctrl.labels.limitFilesWarning = '${silfn:escapeJs(limitFilesWarningLabel)}'"></span>
  <span ng-init="$ctrl.labels.limitFileReached = '${silfn:escapeJs(limitFileReachedLabel)}'"></span>
  <span ng-init="$ctrl.labels.limitFilesReached = '${silfn:escapeJs(limitFilesReachedLabel)}'"></span>
  <span ng-init="$ctrl.labels.title = '${silfn:escapeJs(titleLabel)}'"></span>
  <span ng-init="$ctrl.labels.description = '${silfn:escapeJs(descriptionLabel)}'"></span>
  <span ng-init="$ctrl.labels.deleteFile = '${silfn:escapeJs(deleteFileLabel)}'"></span>
  <span ng-init="$ctrl.labels.deleteFile = '${silfn:escapeJs(deleteFileLabel)}'"></span>
  <span ng-init="$ctrl.labels.attachments = '${silfn:escapeJs(attachmentsLabel)}'"></span>
</div>

<fieldset ng-if="$ctrl.displayIntoFieldset" class="fileUpload skinFieldset">
  <legend>{{$ctrl.labels.attachments}}</legend>
</fieldset>
<div ng-if="!$ctrl.displayIntoFieldset" class="fileUpload"></div>