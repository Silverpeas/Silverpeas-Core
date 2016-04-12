/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * silverpeas-rating is an HTML element to render a rating service associated to any Silverpeas resource by using the AngularJS framework.
   *
   * It defines several attributes:
   * @property {string} componentid - the app id of the resource
   * @property {string} resourcetype - the type of the resource
   * @property {string} resourceid - the id of the resource
   * @property {string} readonly - [optional] if true, user is not able to rate associated resource
   * @property {string} starsize - [optional] use 'small' to display smaller stars
   * @property {string} shownbraterratings - [optional] if false, number of ratings is not display (true by default)
   * @property {string} rating - [optional] a JSON representation of an existing rating object (ie RatingEntity.java).
   * If provided, attributes componentid, resourcetype and resourceid are useless.
   *
   */
  angular.module('silverpeas.directives').directive('silverpeasRating',
      ['Rating', 'context', '$timeout', function(Rating, context, $timeout) {
        return {
          templateUrl : webContext + '/util/javaScript/angularjs/directives/silverpeas-rating.jsp',
          restrict : 'AE',
          scope : {
            componentid : '@',
            contributiontype : '@',
            contributionid : '@',
            readonly : '@',
            forcedisplaywhennorating : '@',
            canuserrating : '@',
            starsize : '@',
            shownbraterratings : '@',
            raterrating : '='
          },
          replace: true,
          link : function postLink(scope, element, attrs) {
            $timeout(function() {

              /*
              Labels & common parameters
               */
              var $labels = angular.element('.labels', element);
              var label_RatingTooltips = $labels.attr('label_RatingTooltips').split(', ');
              var label_RatingVote = $labels.attr('label_RatingVote');
              var label_RatingVotes = $labels.attr('label_RatingVotes');
              var label_RatingVoteDelete = $labels.attr('label_RatingVoteDelete');
              var label_RatingVoteYours = $labels.attr('label_RatingVoteYours');
              var label_RatingVoteNone = $labels.attr('label_RatingVoteNone');
              var param_userAnonymous = eval($labels.attr('param_userAnonymous'));

              /*
              Treatments
               */
              if (typeof scope.canuserrating === 'undefined') {
                scope.canuserrating = "true";
              }
              if (scope.raterrating || attrs.raterrating) {
                if (scope.raterrating) {
                  scope.raterrating = Rating.wrap(scope.raterrating);
                } else {
                  scope.raterrating = Rating.wrap(eval(attrs.raterrating));
                }
                scope.contributionid = scope.raterrating.contributionId;
                scope.contributiontype = scope.raterrating.contributionType;
                scope.componentid = scope.raterrating.componentId;
              }

              var starWidth = 16;
              var starHeight = 16;

              var $rating = angular.element(".spRating", element);
              if (scope.starsize === "small") {
                $rating.addClass("smallStars");
                starWidth = 12;
                starHeight = 12;
              }
              var canuserrating = (scope.canuserrating === "true") && !param_userAnonymous;
              if (!canuserrating) {
                scope.readonly = "true";
              }
              var readonly = scope.readonly === "true";
              var forceDisplayWhenNoRating = scope.forcedisplaywhennorating === "true";
              if (scope.shownbraterratings === "false") {
                angular.element(".rating-contribution", element).hide();
              }

              var _rating = function(raterRatingEntity) {
                // rating resetable only if rater has already participated
                var raterRating = raterRatingEntity.raterRatingValue;
                var resetable = raterRatingEntity.isRatingDone;

                if (!readonly || forceDisplayWhenNoRating || raterRatingEntity.numberOfRaterRatings > 0) {
                  // display number of rater ratings
                  displayCounter(raterRatingEntity.numberOfRaterRatings);

                  // setting up and displaying rating plugin
                  $rating.rateit({
                    value : raterRatingEntity.ratingAverage,
                    readonly : readonly,
                    resetable : resetable,
                    ispreset : true,
                    step : 1,
                    starwidth : starWidth,
                    starheight : starHeight
                  });

                  if (readonly) {
                    angular.element(".spOverRating", element).attr('title',
                        displayTitleCounter(raterRatingEntity, canuserrating));
                  }

                  // setting titles over stars and reset buttons
                  $rating.find(".rateit-reset").attr('title',
                          label_RatingVoteDelete + ' : ' + label_RatingTooltips[raterRating - 1]);
                  $rating.bind('over', function(event, value) {
                    $(this).attr('title', label_RatingTooltips[value - 1]);
                  });

                  $rating.bind('rated reset', function(e) {
                    var ri = $(this);

                    // if the use pressed reset, it will get value: 0 (to be compatible with the HTML range control),
                    // we could check if e.type == 'reset', and then set the value to null.
                    var value = ri.rateit('value');
                    if (e.type == 'reset') {
                      value = null;
                    }

                    // save user rate
                    raterRatingEntity.rate(value).then(function(newRaterRatingEntity) {
                      // refreshing global rate
                      ri.rateit('value', newRaterRatingEntity.ratingAverage);
                      ri.rateit('ispreset', true);
                      ri.rateit('resetable', newRaterRatingEntity.raterRatingValue > 0);
                      ri.find(".rateit-reset").attr('title', label_RatingVoteDelete + ' : ' +
                          label_RatingTooltips[newRaterRatingEntity.raterRatingValue - 1]);
                      displayCounter(newRaterRatingEntity.numberOfRaterRatings);
                    });

                    //maybe we want to disable voting?
                    //ri.rateit('readonly', true);
                  });
                } else {
                  angular.element("span", element).remove();
                }
              };

              if (scope.raterrating) {
                _rating(scope.raterrating);
              } else {
                Rating.get(scope).then(_rating);
              }

              function displayCounter(nbRaterRatings) {
                scope.nbRaterRatings = nbRaterRatings;
                scope.ratingLabel = getRatingsLabel(nbRaterRatings);
              }

              function displayTitleCounter(raterRatingEntity, canuserrating) {
                var label = raterRatingEntity.numberOfRaterRatings + ' ' +
                    getRatingsLabel(raterRatingEntity.numberOfRaterRatings) + '. ';
                if (canuserrating) {
                  if (raterRatingEntity.raterRatingValue > 0) {
                    label += label_RatingVoteYours + ' ' +
                        label_RatingTooltips[raterRatingEntity.raterRatingValue - 1];
                  } else {
                    label += label_RatingVoteNone;
                  }
                }
                return label;
              }

              function getRatingsLabel(nbRaterRatings) {
                if (nbRaterRatings > 1) {
                  return label_RatingVotes;
                }
                return label_RatingVote;
              }
            }, 0);
          } //end link
        };
      } ]);
})();