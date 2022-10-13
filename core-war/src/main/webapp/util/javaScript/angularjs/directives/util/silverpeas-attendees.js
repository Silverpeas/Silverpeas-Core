/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function() {
  angular.module('silverpeas.directives').directive('silverpeasAttendees',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-attendees.jsp',
          restrict : 'E',
          scope : {
            label : '@',
            userPanelPrefix : '@id',
            initUserPanelUrl : '@',
            attendees : '=',
            isWriteMode : '=?',
            isViewMode : '=?',
            isSimpleMode : '=?',
            onAttendeeAnswer : '&'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {
            var _afterInitialization = false;

            this.getTemplate = function() {
              return this.isSimpleMode ? '###silverpeas.user.attendees.simple.content' :
                  '###silverpeas.user.attendees.content'
            }.bind(this);

            this.refreshInternalContext = function() {
              $timeout(function() {
                var attendees = [];
                Array.prototype.push.apply(attendees, this.userAttendees);
                Array.prototype.push.apply(attendees, this.externalAttendees);
                this.attendees = attendees;
                $timeout(function() {
                  activateUserZoom();
                } ,200);
              }.bind(this), 0);
            }.bind(this);

            this.removeUserAttendee = function(attendee) {
              this.userPanelIds.removeElement(attendee.id);
              this.userAttendees.removeElement(attendee);
            }.bind(this);

            this.api = {
              select : function() {
                SP_openUserPanel({
                  url : this.initUserPanelUrl,
                  params : {
                    "UserPanelCurrentUserIds" : this.userPanelIds,
                    "UserPanelCurrentGroupIds" : []
                  }
                }, "userPanel");
              }.bind(this)
            }

            $scope.$watchCollection('$ctrl.userPanelIds', function() {
              if (_afterInitialization) {
                _afterInitialization = false;
                return;
              }
              if (this.isWriteMode && this.userPanelIds) {
                if (this.userPanelIds.length) {
                  User.get({
                    id : this.userPanelIds
                  }).then(function(users) {
                    var userAttendees = [];
                    for (var i = 0; i < users.length; i++) {
                      var user = users[i];
                      var attendee = this.initialAttendeesById[user.id];
                      if (!attendee) {
                        attendee = {
                          id : user.id,
                          fullName : user.fullName,
                          participationStatus: 'AWAITING',
                          presenceStatus : 'REQUIRED'
                        }
                      }
                      userAttendees.push(attendee);
                    }
                    this.userAttendees = userAttendees;
                    this.refreshInternalContext();
                  }.bind(this));
                } else {
                  this.userAttendees = [];
                  this.refreshInternalContext();
                }
              }
            }.bind(this));

            var _attendeeProcessor = function() {
              this.attendees = this.attendees ? this.attendees : [];
              var initialAttendeesById = [];
              var userPanelIds = [];
              var userAttendees = [];
              var externalAttendees = [];
              for (var i = 0; i < this.attendees.length; i++) {
                var attendee = this.attendees[i];
                var id = attendee.id;
                initialAttendeesById[id] = attendee;
                if (__isUserAttendee(id)) {
                  userPanelIds.push(id);
                  userAttendees.push(attendee);
                } else {
                  externalAttendees.push(attendee);
                }
              }
              this.initialAttendeesById = initialAttendeesById;
              this.userPanelIds = userPanelIds;
              this.userAttendees = userAttendees;
              this.externalAttendees = externalAttendees;
              $timeout(function() {
                activateUserZoom();
              } ,200);
            }.bind(this);

            $scope.$watch('$ctrl.attendees', function() {
              _attendeeProcessor();
            }.bind(this));

            $scope.$watchCollection('$ctrl.attendees', function() {
              if (!this.isWriteMode) {
                _attendeeProcessor();
              }
            }.bind(this));

            this.$onInit = function() {
              _attendeeProcessor();
              _afterInitialization = true;
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasAttendee',
      ['context', '$timeout', function(context, $timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-attendee.jsp',
          restrict : 'E',
          scope : {
            attendee : '=',
            onRemove : '&',
            isWriteMode : '=',
            isViewMode : '=',
            isSimpleMode : '=',
            onAnswer : '&'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function() {
            this.isUserAttendee = function() {
              return __isUserAttendee(this.attendee);
            }.bind(this);
            this.isUserZoom = function() {
              return __isUserAttendee(this.attendee) && this.attendee.id !== context.currentUserId;
            }.bind(this);
            this.getParticipationStatusLabel = function() {
              return this.participationStatuses.getElement(this.attendee,
                  'name=participationStatus').label;
            }.bind(this);
            this.getParticipationStatusDefinition = function(participationStatus) {
              return this.participationStatuses.getElement(
                  {participationStatus : participationStatus}, 'name=participationStatus');
            }.bind(this);
            this.getParticipationStatusIcon = function(participationStatus) {
              var participation = this.participationStatuses.getElement(
                  {participationStatus : participationStatus}, 'name=participationStatus');
              return participation ?  participation.label : '';
            }.bind(this);

            this.answer = function(participationStatus) {
              var attendeeAnswer = angular.copy(this.attendee);
              attendeeAnswer.participationStatus = participationStatus;
              this.onAnswer({attendee : attendeeAnswer});
            }.bind(this);

            this.$postLink = function() {
              this.participationStatuses = [
                {name : 'AWAITING', label : this.labels.awaiting, icon : 'help.png'},
                {name : 'DECLINED', label : this.labels.declined, icon : 'wrong.gif'},
                {name : 'TENTATIVE', label : this.labels.tentative, icon : 'question.gif'},
                {name : 'ACCEPTED', label : this.labels.accepted, icon : 'ok.gif'}
              ];
              this.presenceStatuses = [
                {name : 'REQUIRED', label : this.labels.required},
                {name : 'OPTIONAL', label : this.labels.optional},
                {name : 'INFORMATIVE', label : this.labels.informative}
              ];
            }.bind(this);
          }
        };
      }]);

  function __isUserAttendee(attendee) {
    var id = typeof attendee === 'string' ? attendee : attendee.id;
    return /[0-9]+/.test(id);
  }
})();
