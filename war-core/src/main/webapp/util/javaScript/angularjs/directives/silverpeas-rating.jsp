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
<fmt:message var="ratingVoteOK" key="notation.vote.ok"></fmt:message>
<fmt:message var="ratingVoteYours" key="notation.vote.yours"></fmt:message>
<fmt:message var="ratingVoteNone" key="notation.vote.yours.none"></fmt:message>

<script type="text/javascript">
 var label_RatingTooltips = [${ratingTooltips}];
 var label_RatingVote = '${ratingVote}';
 var label_RatingVotes = '${ratingVotes}';
 var label_RatingVoteDelete = '${ratingVoteDelete}';
 var label_RatingVoteOK = '${ratingVoteOK}';
 var label_RatingVoteYours = '${ratingVoteYours}';
 var label_RatingVoteNone = '${silfn:escapeJs(ratingVoteNone)}';
 var param_userAnonymous = ${user.anonymous};
</script>

<span class="spOverRating">
<span class="spRating"></span> <span class="rating-votes"><span class="rating-votes-number">{{nbRates}}</span> {{voteLabel}}</span>
</span>