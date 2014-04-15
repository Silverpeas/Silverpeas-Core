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
   * @property {string} shownbratings - [optional] if false, number of ratings is not display (true by default)
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
            type : '@resourcetype',
            id : '@resourceid',
            readonly : '@',
            starsize : '@',
            shownbratings : '@',
            rating : '='
          },
          link : function postLink(scope, element, attrs) {
            $timeout(function() {
              if (attrs.rating) {
                scope.rating = Rating.wrap(eval(attrs.rating));
                scope.id = scope.rating.resourceId;
                scope.componentid = scope.rating.componentId;
              }

              var starWidth = 16;
              var starHeight = 16;

              var $rating = angular.element(".spRating", element);
              if (scope.starsize === "small") {
                $rating.addClass("smallStars");
                starWidth = 12;
                starHeight = 12;
              }
              var readonly = scope.readonly === "true" || param_userAnonymous;
              if (scope.shownbratings === "false") {
                angular.element(".rating-votes", element).hide();
              }

              var _rating = function(rating) {
                // rating resetable only if user has already participated
                var userRating = rating.userRating;
                var resetable = userRating > 0;

                if (!readonly || rating.notesCount > 0) {
                  // display number of votes
                  displayCounter(rating.notesCount);

                  // setting up and displaying rating plugin
                  $rating.rateit({
                    value : rating.globalRating,
                    readonly : readonly,
                    resetable : resetable,
                    ispreset : true,
                    step : 1,
                    starwidth : starWidth,
                    starheight : starHeight
                  });

                  if (readonly) {
                    angular.element(".spOverRating", element).attr('title',
                        displayTitleCounter(rating, param_userAnonymous));
                  }

                  // setting titles over stars and reset buttons
                  $rating.find(".rateit-reset").attr('title',
                          label_RatingVoteDelete + ' : ' + label_RatingTooltips[userRating - 1]);
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
                    rating.rate(value).then(function(newRating) {
                      // refreshing global rate
                      ri.rateit('value', newRating.globalRating);
                      ri.rateit('ispreset', true);
                      ri.rateit('resetable', newRating.userRating > 0);
                      ri.find(".rateit-reset").attr('title',
                              label_RatingVoteDelete + ' : ' + label_RatingTooltips[newRating.userRating - 1]);
                      displayCounter(newRating.notesCount);
                    });

                    //maybe we want to disable voting?
                    //ri.rateit('readonly', true);
                  });
                } else {
                  angular.element("span", element).remove();
                }
              };

              if (scope.rating) {
                _rating(scope.rating);
              } else {
                Rating.get(scope).then(_rating);
              }

              function displayCounter(nb) {
                scope.nbRates = nb;
                scope.voteLabel = getRatingsLabel(nb);
              }

              function displayTitleCounter(rating, anonymous) {
                var label = rating.notesCount + ' ' + getRatingsLabel(rating.notesCount) + '. ';
                if (!anonymous) {
                  if (rating.userRating > 0) {
                    label += label_RatingVoteYours + ' ' + label_RatingTooltips[rating.userRating - 1];
                  } else {
                    label += label_RatingVoteNone;
                  }
                }
                return label;
              }

              function getRatingsLabel(nb) {
                if (nb > 1) {
                  return label_RatingVotes;
                }
                return label_RatingVote;
              }
            }, 0);
          } //end link
        };
      } ]);
})();