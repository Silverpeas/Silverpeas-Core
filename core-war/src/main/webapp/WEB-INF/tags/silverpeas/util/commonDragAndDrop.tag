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

<%@ attribute name="domSelector" required="true"
              type="java.lang.String"
              description="The DOM selector that permits to identify the drop zone" %>
<%@ attribute name="componentInstanceId" required="false"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>
<%@ attribute name="uploadCompletedUrl" required="true"
              type="java.lang.String"
              description="The URL that must be called after all files are uploaded" %>
<%@ attribute name="uploadCompletedUrlSuccess" required="true"
              type="java.lang.String"
              description="The js function to call after the AJAX call of uploadCompletedUrl is peformed successfully" %>

<%@ attribute name="domHelpHighlightSelector" required="false"
              type="java.lang.String"
              description="The DOM selector aimed to apply the help highlight" %>
<%@ attribute name="ignoreFolders" required="false"
              type="java.lang.Boolean"
              description="Indicates if folders will be ignored (after upload)" %>

<%@ attribute name="greatestUserRole" required="false"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The greatest role the user has" %>
<c:if test="${empty greatestUserRole}">
  <c:set var="greatestUserRole" value="${silfn:getGreatestRoleOfCurrentUserOn(componentInstanceId)}"/>
</c:if>

<%@ attribute name="helpContentUrl" required="false"
              type="java.lang.String"
              description="Specify the URL from which the help content is filled" %>
<c:if test="${empty helpContentUrl}">
  <c:url var="helpContentUrl" value="/upload/Help_${userLanguage}.jsp${(ignoreFolders != null and ignoreFolders) ? '?folders=ignored' : ''}"/>
</c:if>

<%@ attribute name="helpCoverClass" required="false"
              type="java.lang.String"
              description="Specify a class to change display of help access icon" %>

<view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.writer"/>
<jsp:useBean id="writerRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:if test="${greatestUserRole.isGreaterThanOrEquals(writerRole)}">

  <view:includePlugin name="dragAndDropUpload"/>

  <script type="text/JavaScript">
    (function() {
      var options = {
        domSelector : '${domSelector}',
        componentInstanceId : "${componentInstanceId}",
        onCompletedUrl : "${uploadCompletedUrl}",
        onCompletedUrlSuccess : ${uploadCompletedUrlSuccess},
        helpContentUrl : "${helpContentUrl}",
        helpCoverClass : "${helpCoverClass}"
      };

      <c:if test="${not empty domHelpHighlightSelector}">
      options.helpHighlightSelector = '${domHelpHighlightSelector}';
      </c:if>

      initDragAndDropUploadAndReload(options);
    })();
  </script>
</c:if>