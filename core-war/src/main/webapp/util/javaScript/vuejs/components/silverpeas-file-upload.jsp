<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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

<div class="silverpeas-file-upload">
  <div v-sp-init>
    {{addMessages({
    browse : '${silfn:escapeJs(browseLabel)}',
    chooseFile : '${silfn:escapeJs(chooseFileLabel)}',
    chooseFiles : '${silfn:escapeJs(chooseFilesLabel)}',
    dragAndDropFile : '${silfn:escapeJs(dragAndDropFileLabel)}',
    dragAndDropFiles : '${silfn:escapeJs(dragAndDropFilesLabel)}',
    sendingFile : '${silfn:escapeJs(sendingFileLabel)}',
    sendingFiles : '${silfn:escapeJs(sendingFilesLabel)}',
    sendingWaitingWarning : "${silfn:escapeJs(fn:replace(sendingWaitingWarningLabel, '<br/>', ''))}",
    limitFileWarning : '${silfn:escapeJs(limitFileWarningLabel)}',
    limitFilesWarning : '${silfn:escapeJs(limitFilesWarningLabel)}',
    limitFileReached : '${silfn:escapeJs(limitFileReachedLabel)}',
    limitFilesReached : '${silfn:escapeJs(limitFilesReachedLabel)}',
    title : '${silfn:escapeJs(titleLabel)}',
    description : '${silfn:escapeJs(descriptionLabel)}',
    deleteFile : '${silfn:escapeJs(deleteFileLabel)}',
    attachments : '${silfn:escapeJs(attachmentsLabel)}'
    })}}
  </div>
  <fieldset v-if="displayIntoFieldset" class="fileUpload skinFieldset">
    <legend>{{messages.attachments}}</legend>
  </fieldset>
  <div v-if="!displayIntoFieldset" class="fileUpload"></div>
</div>
