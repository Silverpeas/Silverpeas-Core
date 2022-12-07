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
      '/util/javaScript/vuejs/components/content/silverpeas-attachment-templates.jsp');

  Vue.component('silverpeas-attachment-management',
      templateRepository.get('silverpeas-attachment-management', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        data : function() {
          return {
            context : {},
            addPopinApi : undefined,
            addFormApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            addToResource : this.addToResource
          });
        },
        methods : {
          addToResource : function(context, callback) {
            this.context = context;
            this.addPopinApi.open({
              callback : function(formPaneData) {
                return callback(formPaneData).then(function() {
                  this.initAddToResourceFormData();
                }.bind(this));
              }.bind(this)
            });
          },
          initAddToResourceFormData : function() {
            this.addPopinApi.formApi.initFormData();
            this.addFormApi.initFormData();
          }
        }
      }));

  Vue.component('silverpeas-attachment-form',
      templateRepository.get('silverpeas-attachment-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        props : {
          context : {
            'type' : Object,
            'mandatory' : true
          }
        },
        data : function() {
          return {
            foreignId : undefined,
            indexIt : undefined,
            fileTitle : undefined,
            fileDescription : undefined,
            versionType : undefined,
            commentMessage : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData: this.initFormData,
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              formPaneData.foreignId = this.foreignId;
              formPaneData.fileTitle = this.fileTitle;
              formPaneData.fileDescription = this.fileDescription;
              formPaneData.indexIt = this.indexIt;
              if (this.context.isVersionActive) {
                formPaneData.versionType = this.versionType;
                formPaneData.commentMessage = this.commentMessage;
              }
            }
          });
          this.initFormData();
        },
        methods : {
          initFormData : function() {
            this.foreignId = undefined;
            this.indexIt = '' + this.context.indexIt;
            this.fileTitle = '';
            this.fileDescription = '';
            this.versionType = '0';
            this.commentMessage = '';
          }
        },
        watch : {
          'context' : function() {
            if (this.foreignId !== this.context.foreignId) {
              this.$emit('foreign-id-change');
            }
            this.foreignId = this.context.foreignId;
          }
        }
      }));
})();
