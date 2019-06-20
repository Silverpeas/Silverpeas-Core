/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
            showBefore : '=?',
            showAfter : '=?',
            createDate: '@',
            createdBy: '@',
            lastUpdateDate: '@',
            lastUpdatedBy: '@',
            permalink: '@',
            permalinkLabel: '@',
            permalinkHelp: '@',
            permalinkIconUrl: '@'
          },
          transclude : {
            before : '?beforeSlot',
            after : '?afterSlot'
          },
          controllerAs: '$ctrl',
          bindToController: true,
          controller : ['$timeout', '$transclude', '$element',
            function($timeout, $transclude, $element) {

            this.getFormattedPermalink = function() {
              var result = this.permalink;
              if (this.permalink.startsWith(silverpeasUrl)) {
                result = this.permalink.replace(silverpeasUrl, webContext);
              } else if (!this.permalink.startsWith(webContext)) {
                result = webContext + this.permalink;
              }
              return result;
            };

            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config = sp.ajaxConfig(webContext + '/util/javaScript/angularjs/directives/util/silverpeas-crud.jsp');
              config.withParam('createDate', this.createDate);
              config.withParam('createdBy', this.createdBy);
              if (this.createDate !== this.lastUpdateDate) {
                config.withParam('lastUpdateDate', this.lastUpdateDate);
                config.withParam('lastUpdatedBy', this.lastUpdatedBy);
              }
              config.withParam('permalink', this.getFormattedPermalink());
              config.withParam('permalinkLabel', this.permalinkLabel);
              config.withParam('permalinkHelp', this.permalinkHelp);
              config.withParam('permalinkIconUrl', this.permalinkIconUrl);
              return config.getUrl();
            };

            // function used to perform the before and after transclusions
            this.performTransclusions = function() {
              var $beforeSlot = angular.element('.beforeCommonContentBloc', $element);
              if (!this.showBefore) {
                $beforeSlot.remove();
              }
              var $afterSlot = angular.element('.afterCommonContentBloc', $element);
              if (!this.showAfter) {
                $afterSlot.remove();
              }
              $timeout(function() {
                if (this.showBefore && $transclude.isSlotFilled('before')) {
                  $transclude(function($before) {
                    $beforeSlot.append($before);
                  }.bind(this), null, 'before');
                } else {
                  $beforeSlot.remove();
                }
                if (this.showAfter && $transclude.isSlotFilled('after')) {
                  $transclude(function($after) {
                    $afterSlot.append($after);
                  }.bind(this), null, 'after');
                } else {
                  $afterSlot.remove();
                }
              }.bind(this), 0);
            };
          }]
        }
      }]);
})();
