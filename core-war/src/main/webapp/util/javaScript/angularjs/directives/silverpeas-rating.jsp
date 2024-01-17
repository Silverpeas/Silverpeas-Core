<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.notation.multilang.notation"/>

<c:set var="user" value="${sessionScope['SilverSessionController'].currentUserDetail}"/>

<fmt:message var="ratingTooltips" key="notation.labels"></fmt:message>
<fmt:message var="ratingVote" key="notation.vote"></fmt:message>
<fmt:message var="ratingVotes" key="notation.votes"></fmt:message>
<fmt:message var="ratingVoteDelete" key="notation.vote.delete"></fmt:message>
<fmt:message var="ratingVoteYours" key="notation.vote.yours"></fmt:message>
<fmt:message var="ratingVoteNone" key="notation.vote.yours.none"></fmt:message>

<span class="silverpeas-rating">
  <span class="labels" style="display: none"
       label_RatingTooltips="${ratingTooltips}"
       label_RatingVote="${ratingVote}"
       label_RatingVotes="${ratingVotes}"
       label_RatingVoteDelete="${ratingVoteDelete}"
       label_RatingVoteYours="${ratingVoteYours}"
       label_RatingVoteNone="${ratingVoteNone}"
       param_userAnonymous="${user.anonymous}"></span>
  <span class="spOverRating">
    <span class="spRating"></span> <span class="rating-contribution"><span class="rating-contribution-number">{{nbRaterRatings}}</span> {{ratingLabel}}</span>
  </span>
</span>