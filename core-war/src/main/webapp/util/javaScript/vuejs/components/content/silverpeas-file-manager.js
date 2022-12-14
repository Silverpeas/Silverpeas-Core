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

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/content/silverpeas-file-manager-templates.jsp');

  SpVue.component('silverpeas-add-files-popin',
      templateRepository.get('silverpeas-add-files-popin', {
        mixins : [VuejsDefaultFormPanePopinApiMixin, VuejsI18nTemplateMixin, VuejsI18nContentMixin],
        emits : ['validate'],
        props : {
          componentInstanceId : {
            'type': String,
            'default': undefined
          }
        },
        data : function() {
          return {
            title : ''
          };
        },
        mounted : function() {
          this.title = this.messages.title || '';
        },
        methods : {
          validate : function(formPaneData) {
            this.$emit('validate', formPaneData);
          }
        }
      }));

  SpVue.component('silverpeas-add-files-form',
      templateRepository.get('silverpeas-add-files-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin, VuejsI18nContentMixin],
        props : {
          componentInstanceId : {
            'type': String,
            'default': undefined
          },
          isDocumentTemplateEnabled : {
            'type' : Boolean,
            'required' : true
          },
          multiple : {
            'type': Boolean,
            'default': true
          }
        },
        data : function() {
          return {
            choice : 'upload',
            fileUploadApi : undefined,
            documentTemplateApi : undefined,
            fileName : '',
            contentLanguage : ''
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData: this.initFormData,
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              formPaneData.fileLang = this.contentLanguage;
              if (this.choice === 'upload') {
                const uploadedFile = this.fileUploadApi.getUploadedFiles()[0];
                formPaneData.uploadSessionId = uploadedFile.uploadSessionId;
                formPaneData.fileName = uploadedFile.name;
                uploadedFile.inputs.forEach(function(data) {
                  formPaneData[data.name] = data.value;
                })
              } else {
                formPaneData.documentTemplateId = this.documentTemplateApi.getSelectedDocumentTemplate().id;
                formPaneData.fileName = this.fileName;
              }
            }
          });
        },
        methods : {
          setFileUploadApi : function(api) {
            this.fileUploadApi = api;
            this.initFormData();
          },
          setDocumentTemplateApi : function(api) {
            this.documentTemplateApi = api;
            this.initFormData();
          },
          initFormData : function() {
            this.choice = 'upload';
            this.contentLanguage = this.i18nContentLanguage;
            this.resetApis();
          },
          resetApis : function() {
            if (this.fileUploadApi && this.fileUploadApi.reset) {
              this.fileUploadApi.reset();
            }
            if (this.documentTemplateApi && this.documentTemplateApi.clear) {
              this.fileName = '';
              this.documentTemplateApi.clear();
            }
          }
        },
        computed : {
          displayUploadPart : function() {
            this.resetApis();
            return this.choice === 'upload';
          }
        },
        watch : {
          'i18nContentLanguage' : function() {
            this.contentLanguage = this.i18nContentLanguage;
          }
        }
      }));
})();
