/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
  angular.module('silverpeas.directives').directive('silverpeasCrud',
      [function() {
        return {
          template: '<div ng-include src="$ctrl.getTemplateUrl()" onload="$ctrl.performTransclusions()"></div>',
          restrict : 'E',
          scope: {
            createDate: '@',
            createdBy: '@',
            lastUpdateDate: '@',
            lastUpdatedBy: '@',
            permalink: '@',
            permalinkAlt: '@',
            permalinkIconUrl: '@'
          },
          controllerAs: '$ctrl',
          bindToController: true,
          controller : ['$timeout', function($timeout) {

            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config = sp.ajaxConfig(webContext + '/util/javaScript/angularjs/directives/util/silverpeas-crud.jsp');
              config.withParam('createDate', this.createDate);
              config.withParam('createdBy', this.createdBy);
              if (this.createDate !== this.lastUpdateDate) {
                config.withParam('lastUpdateDate', this.lastUpdateDate);
                config.withParam('lastUpdatedBy', this.lastUpdatedBy);
              }
              config.withParam('permalink', this.permalink);
              config.withParam('permalinkAlt', this.permalinkAlt);
              config.withParam('permalinkIconUrl', this.permalinkIconUrl);
              return config.getUrl();
            };

            // function used to perform the before and after transclusions
            this.performTransclusions = function() {
              $timeout(function() {
              }, 0);
            };
          }]
        }
      }]);
})();
