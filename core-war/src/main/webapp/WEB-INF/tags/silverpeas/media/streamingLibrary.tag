<%@ tag import="org.silverpeas.core.media.streaming.StreamingProvidersRegistry" %><%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Constants --%>
<c:set var="supportedStreamingProviders" value="<%=StreamingProvidersRegistry.get().getAll()%>"/>

<script type="text/javascript">

  /**
   * Returns {
   *   title,
   *   author,
   *   embedHtml,
   *   formattedDurationHMS,
   *   definition : {
   *     width,
   *     height
   *   }
   * }
   */
  function getPromiseOfStreamingProviderData(streamingUrl) {
    return sp.ajaxRequest(webContext + '/services/media/streaming/providerData')
        .withParam('url', streamingUrl)
        .sendAndPromiseJsonResponse();
  }

  /**
   * Indicates if a supported streaming provider is detected from the specified streaming URL.
   * @param streamingUrl
   * @returns {boolean}
   */
  function detectSupportedStreamingProvider(streamingUrl) {
    <c:if test="${not empty supportedStreamingProviders}">
    <c:set var="supportedStreamingProviderRegExpr" value=""/>
    <c:forEach var="supportedStreamingProvider" items="${supportedStreamingProviders}">
    <c:forEach var="regexpDetectionPart" items="${supportedStreamingProvider.regexpDetectionParts}">
    <c:if test="${not empty supportedStreamingProviderRegExpr}">
    <c:set var="supportedStreamingProviderRegExpr" value="${supportedStreamingProviderRegExpr}|"/>
    </c:if>
    <c:set var="supportedStreamingProviderRegExpr" value="${supportedStreamingProviderRegExpr}${regexpDetectionPart}"/>
    </c:forEach>
    </c:forEach>
    if (streamingUrl && streamingUrl.length > 0) {
      const urlRegExprCheck = /http.?:\/\/.*(${supportedStreamingProviderRegExpr})/;
      if (urlRegExprCheck.exec(streamingUrl.toLowerCase()) != null) {
        return true;
      }
    }
    </c:if>
    return false;
  }
</script>