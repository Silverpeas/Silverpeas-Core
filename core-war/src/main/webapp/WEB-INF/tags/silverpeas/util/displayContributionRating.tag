<%--
  Copyright (C) 2000 - 2014 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%-- This TAG handle an angular directive --%>
<%-- Please you ensure the following points: --%>
<%-- - the angular application must be defined by "ng-app" angular directive and an id on HTML tag--%>
<%-- - starting the angular app (exemple: angular.module('silverpeas.suggestionBox', ['silverpeas.services', 'silverpeas.directives']);) --%>
<%-- - in table cell, don't forget to include manually the rating angular plugin : <view:inludePlugin name="rating"/> --%>

<plugins:includeContributionRating/>

<%-- Default values --%>
<c:set var="_readOnly" value="false"/>
<c:set var="_forceDisplayWhenNoRating" value="false"/>
<c:set var="_showNbRaterRatings" value="true"/>
<c:set var="_starsize" value="normal"/>
<c:set var="_canuserrating" value="${true}"/>

<%-- TAG attributes --%>
<%@ attribute name="readOnly" required="false" description="Must the rater rating be displayed in a read only mode?" type="java.lang.Boolean" %>
<%@ attribute name="forceDisplayWhenNoRating" required="false" description="Must the rating by displayed in any case?" type="java.lang.Boolean" %>
<%@ attribute name="showNbRaterRatings" required="false" description="Must the user see the number of rater ratings on the contribution?" type="java.lang.Boolean" %>
<%@ attribute name="starSize" required="false" description="normal = normal displaying (default), small = smaller displaying" type="java.lang.String" %>
<%@ attribute name="canUserRating" required="false" description="Can the user perform a rating on the contribution? (default true)" type="java.lang.Boolean" %>

<%-- A rater rating WEB entity --%>
<%@ attribute name="raterRating" required="true" type="org.silverpeas.core.webapi.rating.RaterRatingEntity"
              description="A rater rating WEB entity." %>

<c:if test="${readOnly != null}">
  <c:set var="_readOnly" value="${readOnly}"/>
</c:if>
<c:if test="${forceDisplayWhenNoRating != null}">
  <c:set var="_forceDisplayWhenNoRating" value="${forceDisplayWhenNoRating}"/>
</c:if>
<c:if test="${showNbRaterRatings != null}">
  <c:set var="_showNbRaterRatings" value="${showNbRaterRatings}"/>
</c:if>
<c:if test="${starSize != null}">
  <c:set var="_starsize" value="${starSize}"/>
</c:if>
<c:if test="${canUserRating != null}">
  <c:set var="_canuserrating" value="${canUserRating}"/>
</c:if>
<c:choose>
  <c:when test="${empty __rating_count}">
    <c:set var="__rating_count" value="${0}" scope="request"/>
  </c:when>
  <c:otherwise>
    <c:set var="__rating_count" value="${__rating_count + 1}" scope="request"/>
  </c:otherwise>
</c:choose>
<c:set var="__raterRatingJsVarName" value="_raterRating_${__rating_count}"/>
<!-- ${raterRating.ratingAverage}: a little trick for table sorting -->
<script type="text/javascript">var ${__raterRatingJsVarName} = ${raterRating.asJSonString};</script>

<span silverpeas-rating readonly="${_readOnly}"
                   forcedisplaywhennorating="${_forceDisplayWhenNoRating}"
                   shownbraterratings="${_showNbRaterRatings}"
                   starsize="${_starsize}"
                   canuserrating="${_canuserrating}"
                   raterrating="${__raterRatingJsVarName}"></span>