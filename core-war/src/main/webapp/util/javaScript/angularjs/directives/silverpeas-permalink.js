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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * silverpeas-ts handles the display and management of UI tabs by using the AngularJS framework.
   */
  angular.module('silverpeas.directives').directive('silverpeasPermalink', function() {
    return {
      templateUrl : webContext + '/util/javaScript/angularjs/directives/silverpeas-permalink.jsp',
      restrict : 'E',
      transclude : true,
      scope : {
        link : '=',
        label : '@',
        help : '@',
        iconUrl : '@',
        simple : '=?',
        noHrefHook : '=?'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : ['$element', function($element) {
        //function used on the ng-include to resolve the template
        this.getFullTemplateUrl = function() {
          var config = sp.ajaxConfig(webContext + '/util/javaScript/angularjs/directives/silverpeas-permalink-wrapper.jsp');
          config.withParam('link', encodeURIComponent(this.getFormattedPermalinkForWrapper()));
          config.withParam('label', this.label ? encodeURIComponent(this.label) : this.label);
          config.withParam('help', this.help ? encodeURIComponent(this.help) : this.help);
          config.withParam('iconUrl', this.iconUrl);
          return config.getUrl();
        }
        this.getTemplate = function() {
          return this.simple ? '###silverpeas.permalink.simple' : '###silverpeas.permalink.full';
        };
        this.copyLink = function() {
          var $input = angular.element('input', $element)[0];
          $input.select();
          document.execCommand('copy');
          notyInfo(this.messages.copyOk);
        };
        this.$postLink = function() {
          if (typeof this.simple === 'undefined') {
            this.simple = true;
          }
        };

        this.getFormattedPermalinkForWrapper = function() {
          var result = this.link;
          if (this.link.startsWith(silverpeasUrl)) {
            result = this.link.replace(silverpeasUrl, webContext);
          } else if (!this.link.startsWith(webContext)) {
            result = webContext + this.link;
          }
          return result;
        };
      }]
    };
  });
})();
