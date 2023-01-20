/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
      '/socialNetwork/jsp/js/vuejs/components/silverpeas-my-profile-templates.jsp');

  SpVue.component('silverpeas-my-profile-management',
      templateRepository.get('my-profile-management', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        emits : ['my-photo-change'],
        props : {
          profile : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            myPhotoCtx : {
              popin : undefined,
              formPane : undefined,
              form : undefined
            }
          };
        },
        created : function() {
          this.extendApiWith({
            openMyPhoto : this.openMyPhoto
          });
        },
        methods : {
          openMyPhoto : function() {
            this.myPhotoCtx.form.initFormData();
            this.myPhotoCtx.popin.open({
              callback : function() {
                return this.myPhotoCtx.formPane.validate().then(function(formPaneData) {
                  return sp
                      .ajaxRequest('UpdatePhoto')
                      .byPostMethod()
                      .send(sp.form.toFormData(formPaneData))
                      .then(function() {
                        this.$emit('my-photo-change');
                      }.bind(this));
                }.bind(this));
              }.bind(this)
            });
          }
        }
      }));

  SpVue.component('silverpeas-my-photo-form',
      templateRepository.get('my-photo-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        props : {
          myPhotoUrl : {
            'type' : String,
            'required' : true
          }
        },
        data : function() {
          return {
            myPhotoInput : undefined,
            myPhotoModel : {}
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData : this.initFormData,
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              if (this.myPhotoModel.file) {
                formPaneData[this.myPhotoModel.fileInputName] = this.myPhotoModel.file;
              } else if (this.myPhotoModel.deleteOriginal) {
                formPaneData.removeImageFile = 'yes';
              }
            }
          });
        },
        methods : {
          initFormData : function(){
            this.myPhotoInput.clear();
            this.myPhotoModel = {};
          }
        }
      }));
})();
