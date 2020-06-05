<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
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

<%@ tag import="org.silverpeas.core.contribution.attachment.util.AttachmentSettings" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>
<%@ attribute name="resourceId" required="true"
              type="java.lang.String"
              description="The identifier of the resource the uploaded document must be attached to" %>
<%@ attribute name="resourceType" required="true"
              type="java.lang.String"
              description="The type of the resource the uploaded document must be attached to" %>
<%@ attribute name="documentType" required="false"
              type="java.lang.String"
              description="The type of the document attachment" %>
<%@ attribute name="contentLanguage" required="false"
              type="java.lang.String"
              description="The content language to retrieve as a first priority" %>
<%@ attribute name="highestUserRole" required="false"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The highest role the user has" %>

<c:set var="__viewAsContentActivated" value="<%=AttachmentSettings.isDisplayableAsContentForComponentInstanceId(componentInstanceId)%>"/>
<c:if test="${__viewAsContentActivated}">
  <c:set var="domIdSuffix" value="${fn:replace(fn:replace(resourceId, '=', '_'), '-', '_')}"/>
  <div class="attachments-as-content" id="attachments-as-content-${domIdSuffix}"></div>
  <view:includePlugin name="preview" />
  <script type="text/JavaScript">
    (function() {
      new AttachmentsAsContentViewer({
        domSelector : '#attachments-as-content-${domIdSuffix}',
        highestUserRole : ${highestUserRole != null ? '\''.concat(highestUserRole.name).concat('\''): 'undefined'},
        componentInstanceId : '${componentInstanceId}',
        resourceId : '${resourceId}',
        resourceType : '${resourceType}',
        documentType : ${not empty documentType ? '\''.concat(documentType).concat('\''): 'undefined'}
      });
    })();
  </script>
</c:if>