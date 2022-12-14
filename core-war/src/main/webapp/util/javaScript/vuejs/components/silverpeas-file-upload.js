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
  /**
   * silverpeas-file-upload is the VueJS implementation of plugin silverpeas-fileUpload.
   */
  const asyncTemplate = new VueJsAsyncComponentTemplateRepository(
      webContext + '/util/javaScript/vuejs/components/silverpeas-file-upload.jsp');
  SpVue.component('silverpeas-file-upload',
      asyncTemplate.getSingle({
        mixins : [VuejsFormInputMixin],
        props : {
          componentInstanceId : {
            'type': String,
            'default': undefined
          },
          displayIntoFieldset : {
            'type': Boolean,
            'default': true
          },
          multiple : {
            'type': Boolean,
            'default': true
          },
          infoInputs : {
            'type': Boolean,
            'default': true
          },
          dragAndDropDisplay : {
            'type': Boolean,
            'default': true
          },
          dragAndDropDisplayIcon : {
            'type': Boolean,
            'default': true
          },
          nbFileLimit : {
            'type': Number,
            'default': 0
          }
        },
        data : function() {
          return {
            fileUploadApi : undefined
          };
        },
        mounted : function() {
          const $fileUpload = jQuery(this.$el.querySelector(".fileUpload"));
          $fileUpload.fileUpload({
            componentInstanceId : this.componentInstanceId,
            multiple : this.multiple,
            infoInputs : this.infoInputs,
            dragAndDropDisplay : this.dragAndDropDisplay,
            dragAndDropDisplayIcon : this.dragAndDropDisplayIcon,
            nbFileLimit : this.nbFileLimit,
            labels : {
              browse : this.messages.browse,
              chooseFile : this.messages.chooseFile,
              chooseFiles : this.messages.chooseFiles,
              dragAndDropFile : this.messages.dragAndDropFile,
              dragAndDropFiles : this.messages.dragAndDropFiles,
              sendingFile : this.messages.sendingFile,
              sendingFiles : this.messages.sendingFiles,
              sendingWaitingWarning : this.messages.sendingWaitingWarning.convertNewLineAsHtml(),
              limitFileWarning : this.messages.limitFileWarning,
              limitFilesWarning : this.messages.limitFilesWarning,
              limitFileReached : this.messages.limitFileReached,
              limitFilesReached : this.messages.limitFilesReached,
              title : this.messages.title,
              description : this.messages.description,
              deleteFile : this.messages.deleteFile,
              attachments : this.messages.attachments
            }
          });
          this.fileUploadApi = $fileUpload.fileUpload('api');
          this.extendApiWith({
            /**
             * Resets the plugin of file upload.
             */
            reset : this.reset,
            /**
             * Verifies that it does not exist an upload. Nothing is done if none.
             * An exception is sent otherwise with the appropriate message.
             */
            checkNoFileSending : this.checkNoFileSending,
            /**
             * Encodes a set of form elements as a string for submission.
             */
            serialize : this.serialize,
            /**
             * Encodes a set of form elements as an array of names and values.
             */
            serializeArray : this.serializeArray,
            /**
             * Gets an array of all current uploaded file data.
             * For each uploaded file, there is the attribute 'input' which is an array of all form
             * inputs.
             * @returns {*[]}
             */
            getUploadedFiles : this.getUploadedFiles,
            /**
             * Mandatory implementation needed by {@link VuejsFormInputMixin}.
             * @returns {boolean}
             */
            validateFormInput : function() {
              let isError = false;
              if (this.fileUploadApi) {
                try {
                  this.checkNoFileSending();
                } catch (errorMsg) {
                  isError = true;
                  this.rootFormApi.errorMessage().add(errorMsg);
                }
                if (this.isMandatory && !this.serializeArray().length) {
                  isError = true;
                  this.rootFormApi.errorMessage().add(
                      this.formatMessage(this.rootFormMessages.mandatory,
                          this.getLabelByForAttribute(this.id)));
                }
              }
              return !isError;
            }
          });
        },
        methods : {
          getInputElementName : function() {
            // this method is used by silverpeas-form-pane APIs
          },
          reset : function() {
            this.fileUploadApi.reset();
          },
          checkNoFileSending : function() {
            this.fileUploadApi.checkNoFileSending();
          },
          serialize : function() {
            return this.fileUploadApi.serialize();
          },
          serializeArray : function() {
            return this.fileUploadApi.serializeArray();
          },
          getUploadedFiles : function() {
            return this.fileUploadApi.getUploadedFiles();
          }
        }
      }));
})();
