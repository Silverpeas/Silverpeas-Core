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
  angular.module('silverpeas.directives').directive('silverpeasFileUpload',
      [function() {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-file-upload.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            displayIntoFieldset : '=?',
            multiple : '=?',
            infoInputs : '=?',
            dragAndDropDisplay : '=?',
            dragAndDropDisplayIcon : '=?',
            nbFileLimit : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$element', function($element) {
            var _fileUploadApi;

            this.api = {
              /**
               * Resets the plugin of file upload.
               */
              reset : function() {
                _fileUploadApi.reset();
              }.bind(this),
              /**
               * Verifies that it does not exist an upload. Nothing is done if none.
               * An exception is sent otherwise with the appropriate message.
               */
              checkNoFileSending : function() {
                _fileUploadApi.checkNoFileSending();
              }.bind(this),
              /**
               * Encodes a set of form elements as a string for submission.
               */
              serialize : function() {
                return _fileUploadApi.serialize();
              }.bind(this),
              /**
               * Encodes a set of form elements as an array of names and values.
               */
              serializeArray : function() {
                return _fileUploadApi.serializeArray();
              }.bind(this)
            };

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              whenSilverpeasReady(function() {
                if (typeof this.multiple === 'undefined') {
                  this.multiple = true;
                }
                if (typeof this.infoInputs === 'undefined') {
                  this.infoInputs = true;
                }
                if (typeof this.dragAndDropDisplay === 'undefined') {
                  this.dragAndDropDisplay = true;
                }
                if (typeof this.dragAndDropDisplayIcon === 'undefined') {
                  this.dragAndDropDisplayIcon = true;
                }
                if (typeof this.nbFileLimit === 'undefined') {
                  this.nbFileLimit = 0;
                }
                this.$fileUpload = jQuery(angular.element(".fileUpload", $element));
                this.$fileUpload.fileUpload({
                  multiple : this.multiple,
                  infoInputs : this.infoInputs,
                  dragAndDropDisplay : this.dragAndDropDisplay,
                  dragAndDropDisplayIcon : this.dragAndDropDisplayIcon,
                  nbFileLimit : this.nbFileLimit,
                  labels : this.labels
                });
                _fileUploadApi = this.$fileUpload.fileUpload('api');
              }.bind(this));
            }.bind(this);
          }]
        };
      }]);
})();
