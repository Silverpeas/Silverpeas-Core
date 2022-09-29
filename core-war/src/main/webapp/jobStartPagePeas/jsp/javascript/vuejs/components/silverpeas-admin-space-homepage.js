/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

//# sourceURL=/admin/jsp/javaScript/services/silverpeas-admin-space-homepage.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/jobStartPagePeas/jsp/javascript/vuejs/components/silverpeas-admin-space-homepage-templates.jsp');

  const SpaceHomePageType = sp.object.asEnum([
    /**
     * Standard home page. The default one.
     */
    'STANDARD',
    /**
     * The home page is the main page of a component instance, meaning a redirection to the
     * application instance's main page.
     */
    'COMPONENT_INST',
    /**
     * The home page is an HTML page.
     */
    'PORTLET',
    /**
     * The home page is an HTML page.
     */
    'HTML_PAGE']);

  /**
   * This component handles a POPIN tha allows to specify the space homepage content.
   * No save will be performed by this popin, but on validation 'validated' event is emitted with
   * the following object as payload :
   * <pre> {
   *   deferredSave : a deferred promise which MUST be used when the caller performs the save of
   *                  space homepage content choice (deferredSave.resolve() or deferredSave.reject()).
   *   type : string representing the type of homepage. It can be one of these :
   *        * SpaceHomePageType.STANDARD, default homepage,
   *        * SpaceHomePageType.COMPONENT_INST, application instance,
   *        * SpaceHomePageType.PORTLET, portlet,
   *        * SpaceHomePageType.HTML_PAGE, an url which allows to get the homepage content.
   *   value : a value linked to the type data.
   * }
   * </pre>
   */
  Vue.component('silverpeas-admin-space-homepage-popin',
      templateRepository.get('space-homepage-popin', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        props : {
          title : {
            'type' : String,
            'required' : true
          },
          spaceId : {
            'type' : String,
            'required' : true
          },
          homepage : {
            'type' : Object,
            'default' : {
              'type' : SpaceHomePageType.STANDARD.name(),
              'value' : undefined
            }
          }
        },
        data : function() {
          return {
            homepagePopinApi : undefined,
            homepageFormApi : undefined,
            homepageFormMandatoryLegend : false,
            spacePath : undefined,
            spaceApps : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : function() {
              this.open();
            }
          });
          AdminSpaceService.getFullPath(this.spaceId).then(function(spacePath) {
            this.spacePath = spacePath;
            // This is inspired from spAdminWindow.loadSpace()
            return sp.ajaxRequest(webContext + '/RjobStartPagePeas/jsp/GoToSpace').withParam('Espace', this.spaceId).send().then(function() {
              return sp.ajaxRequest(webContext + '/RjobStartPagePeas/jsp/jobStartPageNavAsJson').sendAndPromiseJsonResponse().then(function(data) {
                this.spaceApps = data.applications;
              }.bind(this));
            }.bind(this));
          }.bind(this));
        },
        methods : {
          open : function() {
            this.homepagePopinApi.open({
              callback : this.homepageFormApi.validate
            });
          },
          validate : function(formPaneData) {
            const deferredSave = sp.promise.deferred();
            deferredSave.promise.then(function() {
              notySuccess(this.messages.successSaveMessage);
              const spaceHomePageType = formPaneData.spaceHomePageType;
              if (spaceHomePageType === SpaceHomePageType.PORTLET) {
                SP_openWindow(webContext + '/dt?dt.SpaceId=' + this.spaceId + '&dt.Role=Admin',
                    "spaceHomePageWindow",
                    740,
                    600,
                    "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable",
                    false);
              }
            }.bind(this));
            this.$emit('validated', {
              deferredSave : deferredSave,
              'type' : formPaneData.type,
              'value' : formPaneData.value
            });
            return deferredSave.promise;
          }
        }
      }));

  Vue.component('silverpeas-admin-space-homepage-form',
      templateRepository.get('space-homepage-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        props : {
          spacePath : {
            'type' : Array,
            'required' : true
          },
          spaceApps : {
            'type' : Array,
            'required' : true
          },
          homepage : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            choice : undefined,
            appId : undefined,
            url : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              const choice = SpaceHomePageType.valueOf(this.choice);
              formPaneData.spaceHomePageType = choice;
              formPaneData.type = this.isTypeAsNumber ? choice.ordinal() : choice.name();
              if (choice === SpaceHomePageType.COMPONENT_INST) {
                formPaneData.value = this.appId;
              } else if (choice === SpaceHomePageType.HTML_PAGE) {
                formPaneData.value = this.url;
              }
            }
          });
          this.initFormData();
        },
        watch : {
          'homepage' : function() {
            this.initFormData();
          },
          'choice' : function(value) {
            if (value) {
              this.$emit('choice', value);
            }
          }
        },
        methods : {
          initFormData : function() {
            let choice;
            if (this.isTypeAsNumber) {
              choice = SpaceHomePageType.valueAt(this.homepage.type);
            } else {
              choice = SpaceHomePageType.valueOf(this.homepage.type);
            }
            let appId = undefined;
            let url = undefined;
            if (choice === SpaceHomePageType.COMPONENT_INST) {
              appId = this.homepage.value;
            }
            if (!appId && this.spaceApps.length) {
              appId = this.spaceApps[0].id;
            }
            if (choice === SpaceHomePageType.HTML_PAGE) {
              url = this.homepage.value;
            }
            this.choice = choice.name();
            this.appId = appId;
            this.url = url;
          }
        },
        computed : {
          isTypeAsNumber : function() {
            return typeof this.homepage.type === 'number';
          },
          urlIsMandatory : function() {
            return this.choice === SpaceHomePageType.HTML_PAGE.name();
          }
        }
      }));
})();
