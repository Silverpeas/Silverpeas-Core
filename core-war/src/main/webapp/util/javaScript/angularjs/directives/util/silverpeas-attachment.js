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
  angular.module('silverpeas.directives').directive('silverpeasAttachment',
      [function() {
        return {
          template: '<div ng-include src="$ctrl.getTemplateUrl()"></div>',
          restrict : 'E',
          scope: {
            componentId: '@',
            resourceId: '@',
            readOnly: '@'
          },
          controllerAs: '$ctrl',
          bindToController: true,
          controller : [function() {
            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config = sp.ajaxConfig(webContext + '/util/javaScript/angularjs/directives/util/silverpeas-attachment-wrapper.jsp');
              config.withParam('componentId', this.componentId);
              config.withParam('resourceId', encodeURIComponent(this.resourceId));
              config.withParam('readOnly', this.readOnly);
              return config.getUrl();
            }
          }]
        }
      }]);
})();
