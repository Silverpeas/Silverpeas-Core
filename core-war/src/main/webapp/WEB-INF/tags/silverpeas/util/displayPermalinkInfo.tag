<%--
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<%-- Permalink --%>
<%@ attribute name="link" required="false" type="java.lang.String"
              description="A permalink to display" %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The label to display" %>
<%@ attribute name="help" required="false" type="java.lang.String"
              description="The permalink help." %>
<%@ attribute name="iconUrl" required="false" type="java.lang.String"
              description="The permalink url" %>
<c:if test="${empty label}">
  <fmt:message var="label" key="GML.permalink" bundle="${generalBundle}"/>
</c:if>
<c:if test="${empty iconUrl}">
  <c:url var="iconUrl" value="/util/icons/link.gif"/>
</c:if>


<c:set var="__Permalink_TAG_ID" value="0"/>
<c:if test="${not empty link}">
  <c:set var="__Permalink_TAG_ID" value="${link.hashCode() < 0 ? -link.hashCode() : link.hashCode()}"/>
</c:if>
<c:set var="__lastUserCrud" value="__lastUserCrud${__Permalink_TAG_ID}"/>
<c:set var="__inputId" value="permalink-input-${__Permalink_TAG_ID}"/>

<c:if test="${not empty link}">
  <c:url value="/" var="applicationPrefix"/>
  <c:set value="/${fn:replace(link, applicationPrefix, '')}" var="permalink"/>
  <p id="permalinkInfo">
    <a title="${help}" class="sp-permalink" href="<c:url value="${permalink}"/>">
      <img border="0" alt="${help}" title="${help}" src="${iconUrl}"/>
    </a> ${label}
    <input id="${__inputId}" type="text" value="${silfn:fullApplicationURL(pageContext.request)}${permalink}" onfocus="${__lastUserCrud}.select();" class="inputPermalink"/>
    <fmt:message var="permalinkCopyLabel" key="GML.permalink.copy" bundle="${generalBundle}"/>
    <a class="sp_button copy-to-clipboard" title="${permalinkCopyLabel}" href="javascript:void(0)" onclick="${__lastUserCrud}.copyLink();">
      <span>${permalinkCopyLabel}</span>
    </a>
  </p>
  <fmt:message var="permalinkCopyOkMessage" key="GML.permalink.copy.ok" bundle="${generalBundle}"/>
  <script type="text/javascript">
    var ${__lastUserCrud} = new function() {
      this.select = function() {
        var $input = jQuery('#${__inputId}')[0];
        $input.select();
      };
      this.copyLink = function() {
        this.select();
        document.execCommand('copy');
        notyInfo('${permalinkCopyOkMessage}');
      };
    };
  </script>
</c:if>
