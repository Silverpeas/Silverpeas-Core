/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
  angular.module('silverpeas.directives').directive('silverpeasFileUpload',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-file-upload.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            displayIntoFieldset : '='
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {

            this.api = {
              /**
               * Verifies that it does not exist an upload. Nothing is done if none.
               * An exception is sent otherwise with the appropriate message.
               */
              checkNoFileSending : function() {
                this.fileUploadApi.checkNoFileSending();
              }.bind(this),
              /**
               * Encodes a set of form elements as a string for submission.
               */
              serialize : function() {
                return this.fileUploadApi.serialize();
              }.bind(this),
              /**
               * Encodes a set of form elements as an array of names and values.
               */
              serializeArray : function() {
                return this.fileUploadApi.serializeArray();
              }.bind(this)
            }

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              whenSilverpeasReady(function() {
                this.$fileUpload = jQuery(angular.element(".fileUpload", $element));
                this.$fileUpload.fileUpload({
                  multiple: true,
                  dragAndDropDisplay: true,
                  nbFileLimit: 0,
                  labels: this.labels
                });
                this.fileUploadApi = this.$fileUpload.fileUpload('api');
              }.bind(this));
            }.bind(this);
          }
        };
      }]);
})();
