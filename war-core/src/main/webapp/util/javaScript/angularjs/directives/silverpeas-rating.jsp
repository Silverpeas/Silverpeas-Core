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