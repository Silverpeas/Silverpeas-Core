/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
   * silverpeas-operation-creation-area is an HTML element which is built to provide the equivalent
   * to <view:areaOfOperationOfCreation/>.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-operation-creation-area></silverpeas-operation-creation-area>
   */
  Vue.component('silverpeas-operation-creation-area', {
    template : '<div id="menubar-creation-actions" ref="container"></div>',
    created : function() {
      YAHOO.util.Event.onContentReady("menuwithgroups", function() {
        var $creationArea = $(this.$refs.container);
        if ($creationArea.length > 0) {
          $('.menubar-creation-actions-item').appendTo($creationArea);
          $('a', $creationArea).css({'display' : ''});
          $creationArea.css({'display' : 'block'});
        }
        setTimeout(applyTokenSecurityOnMenu, 0);
      }.bind(this));
    }
  });

  /**
   * silverpeas-inline-message is an HTML element to render a Silverpeas's inline message by VueJs.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-inline-message>
   *            <span>My message</span>
   *          </silverpeas-inline-message>
   */
  Vue.component('silverpeas-inline-message', {
    template : '<div class="inlineMessage"><slot></slot></div>'
  });

  /**
   * silverpeas-button-pane is an HTML element which is built to contain silverpeas-button elements.
   * Nevertheless, all kinds of HTML or VueJS component can be included.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-button-pane>
   *            <silverpeas-button v-bind:click.native="...">OK<silverpeas-button>
   *            <silverpeas-button onclick="...">Cancel<silverpeas-button>
   *          </silverpeas-button-pane>
   */
  Vue.component('silverpeas-button-pane', {
    template : '<div class="silverpeas-button-pane sp_buttonPane"><slot></slot></div>'
  });

  /**
   * silverpeas-button is an HTML element to render a button in a Silverpeas way by using the
   * VueJS framework.
   *
   * Two ways to perform a click event:
   * - one way to execute a method of a VueJS component. In that case 'v-bind' directive and
   * 'native' modifier will be required. The 'native' modifier is mandatory when the native event
   * on a VueJS component is aimed. See example 1.
   * - the second way to execute an external JS code. Just fill 'onclick' attribute. See example 2.
   *
   * @param title (Optional) if filled it adds a title on the button.
   * @param iconUrl (Optional) if filled the button has icon clickable behavior.
   *
   * @example 1 <silverpeas-button v-bind:click.native="methodOfVue()">OK<silverpeas-button>
   * @example 2 <silverpeas-button onclick="methodOfVanillaJs">Cancel<silverpeas-button>
   */
  Vue.component('silverpeas-button', {
    template : '<a class="silverpeas-button" ' +
                  'v-bind:class="{\'sp_button\':!isIconBehavior,\'sp_icon\':isIconBehavior}" ' +
                  'v-bind:title="title" ' +
                  'href="javascript:void(0)">' +
                    '<slot v-if="!isIconBehavior"></slot>' +
                    '<img v-else v-bind:src="iconUrl" alt="" />' +
               '</a>',
    props : {
      title : {
        'type' : String,
        'default' : ''
      },
      iconUrl : {
        'type' : String,
        'default' : undefined
      }
    },
    computed : {
      isIconBehavior : function() {
        return !!this.iconUrl;
      }
    }
  });

  /**
   * silverpeas-list is an HTML element to render a common list in a Silverpeas way by using the
   * VueJS framework.
   *
   * One attribute is handled:
   * @param items permits to handle some UI displays.
   * @param noItemLabel (Optional) permits to override the default label displayed when no item
   *     exists.
   *
   * There is 3 template zones handled:
   * - the "before" one
   * - the "default" one (the body)
   * - the "after" one
   *
   * @see silverpeas-list-item documentation in order to get example about the template zone use.
   * @example <silverpeas-list>
   *            <silverpeas-list-item v-for="item in items">{{item}}</silverpeas-list-item>
   *          <silverpeas-list>
   */
  Vue.component('silverpeas-list', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-list.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsI18nTemplateMixin],
        template: request.responseText,
        props : {
          items : {
            'type' : Array,
            'required' : true
          },
          noItemLabel : {
            'type' : String,
            'default' : 'N/A'
          }
        },
        computed : {
          isData : function() {
            return this.items.length > 0;
          },
          noItemMessage : function() {
            return this.noItemLabel !== 'N/A' ? this.noItemLabel : this.messages.noItemLabel;
          }
        }
      });
    });
  });

  /**
   * silverpeas-list is an HTML element to render a common list in a Silverpeas way by using the
   * VueJS framework.
   *
   * There is 4 template zones handled:
   * - the "header" one
   * - the "default" one (the body)
   * - the "actions" one
   * - the "footer" one
   *
   * @example 1 <silverpeas-list-item>
   *              <div>Content</div>
   *            <silverpeas-list-item>
   *
   * @example 2 <silverpeas-list-item>
   *              <span slot="header">An header</span>
   *              <div>Content</div>
   *              <template slot="actions">
   *                <silverpeas-button>Save</silverpeas-button>
   *                <silverpeas-button>Delete</silverpeas-button>
   *              </template>
   *              <span slot="footer">An footer</span>
   *            <silverpeas-list-item>
   */
  Vue.component('silverpeas-list-item', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-list-item.jsp').send().then(function(request) {
      resolve({
        template: request.responseText,
        computed : {
          isActions : function() {
            return !!this.$slots.actions;
          }
        }
      });
    });
  });

  /**
   */
  Vue.component('silverpeas-popin', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-popin.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        template : request.responseText,
        props : {
          'type' : {
            'type' : String,
            'default' : 'validation'
          },
          'title' : {
            'type' : String,
            'default' : ''
          },
          'minWidth' : {
            'type' : String,
            'default' : '500px'
          },
          'maxWidth' : {
            'type' : String,
            'default' : '800px'
          }
        },
        data : function() {
          return {
            jqDialog : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : function(options) {
              this.open(options);
            },
            close : function() {
              this.close();
            }
          });
        },
        methods : {
          open : function(options) {
            Vue.nextTick(function() {
              this.jqDialog = jQuery(this.$refs.container);
              this.jqDialog.popup(this.type, extendsObject({
                title : this.title,
                minWidth : this.minWidth,
                maxWidth : this.maxWidth
              }, options));
            }.bind(this));
          },
          close : function() {
            this.jqDialog.popup('close');
          }
        }
      });
    });
  });

  /**
   * silverpeas-permalink is an HTML element to render a permalink in a Silverpeas's way by using
   * the VueJS framework.
   *
   * Attributes are listed into 'props' declarations:
   * @param link: the link representing the permalink.
   * @param label: the label displayed (into simple mode).
   * @param help: the help displayed (into simple mode).
   * @param iconUrl: (optional) if filled it replace the default icon url.
   * @param simple: (default true) if true, the VueJS version is displayed, otherwise the server
   *     version is rendered.
   * @param noHrefHook: (default false) if true, the spWindow Silverpeas's plugin does not handle
   *     the permalink.
   *
   * Some examples:
   * @example <silverpeas-permalink link="/Publication/3"></silverpeas-permalink>
   * @example <silverpeas-permalink link="/Publication/3"
   *     v-bind:simple="false"></silverpeas-permalink>
   */
  Vue.component('silverpeas-permalink', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-permalink.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsI18nTemplateMixin],
        template: request.responseText,
        props : {
          link : String,
          label : String,
          help : String,
          iconUrl : String,
          simple : {
            'type' : Boolean,
            'default' : true
          },
          noHrefHook : {
            'type' : Boolean,
            'default' : false
          }
        },
        methods : {
          renderFullTemplateUrl : function() {
            sp.ajaxRequest(webContext +
                '/util/javaScript/vuejs/components/silverpeas-permalink-wrapper.jsp')
                .withParam('link', encodeURIComponent(this.getFormattedPermalinkForWrapper()))
                .withParam('label', this.label ? encodeURIComponent(this.label) : this.label)
                .withParam('help', this.help ? encodeURIComponent(this.help) : this.help)
                .withParam('iconUrl', this.iconUrl)
                .send().then(function(request) {
              sp.updateTargetWithHtmlContent(this.$refs.fullContainer, request.responseText);
            }.bind(this));
          },
          getFormattedPermalinkForWrapper : function() {
            var result = this.link;
            if (this.link.startsWith(silverpeasUrl)) {
              result = this.link.replace(silverpeasUrl, webContext);
            } else if (!this.link.startsWith(webContext)) {
              result = webContext + this.link;
            }
            return result;
          },
          copyLink : function() {
            var $input = this.$el.querySelector('input');
            $input.select();
            document.execCommand('copy');
            notyInfo(this.messages.copyOk);
          }
        },
        computed : {
          isFull : function() {
            if (!this.simple) {
              this.renderFullTemplateUrl();
            }
            return !this.simple;
          }
        }
      });
    });
  });

  /**
   * silverpeas-form-pane is an HTML element to render a form in a Silverpeas's way by using
   * the VueJS framework.
   * The aim is to get a standard for all entity to set by a form that the user fills.
   *
   * This component has to include into its body some components which are using
   *     'VuejsFormApiMixin'. Otherwise it will do nothing.
   *
   * There is 4 template zones handled:
   * - the "header" one
   * - the "default" one (the body)
   * - the "footer" one
   * - the "legend" one
   *
   * @param mandatoryLegend (Optional) if true, displaying the mandatory legend just before
   *     buttons.
   * @param manualActions (Optional) if true, handling manually the actions on the form with the
   *     api.
   *
   * @event data-update when the user has performed validation of data successfully and the new
   *     data are ready.
   * @event validation-fail when validation of data has failed.
   * @event cancel when validation has been cancelled.
   */
  Vue.component('silverpeas-form-pane', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-form-pane.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        provide : function() {
          return {
            rootFormApi : this.api
          };
        },
        template : request.responseText,
        props : {
          mandatoryLegend : {
            "type" : Boolean,
            "default" : true
          },
          manualActions : {
            "type" : Boolean,
            "default" : false
          }
        },
        created : function() {
          function __sortFormsByPriority(formA, formB) {
            return formA.getFormPriority() - formB.getFormPriority();
          }

          var formValidationRegistry = [];

          /**
           * Performs validation on all linked forms
           */
          var _validate = function() {
            var __validationPromises = [];
            for (var i = 0; i < formValidationRegistry.length; i++) {
              var validation = formValidationRegistry[i].validate();
              var validationType = typeof validation;
              var isPromiseValidation = !sp.promise.isOne(validation);
              if (validationType === 'undefined' || !isPromiseValidation) {
                sp.log.error('VuejsFormApiMixin - validate method must return a promise or a boolean value');
                return sp.promise.rejectDirectlyWith();
              }
              __validationPromises.push(isPromiseValidation ? validation : sp.promise.resolveDirectlyWith(validation));
            }
            var existsAtLeastOneError = false;
            return sp.promise.whenAllResolved(__validationPromises).then(function(validationResults) {
              validationResults.forEach(function(validationResult) {
                validationResult = typeof validationResult !== 'undefined' ? validationResult : true;
                existsAtLeastOneError = !validationResult || existsAtLeastOneError;
              });
              return (!SilverpeasError.show() && !existsAtLeastOneError)
                  ? sp.promise.resolveDirectlyWith()
                  : sp.promise.rejectDirectlyWith();
            });
          }.bind(this);

          /**
           * Performs updating of source data from internal one.
           */
          var _updateData = function() {
            var data = {};
            formValidationRegistry.forEach(function(form) {
              var result = form.updateData(data);
              if (result) {
                data = result;
              }
            });
            return data;
          }.bind(this);

          this.extendApiWith({
            handleFormApi : function(formApi) {
              formValidationRegistry.push(formApi);
              formValidationRegistry.sort(__sortFormsByPriority);
            },
            validate : function() {
              notyReset();
              return _validate().then(function() {
                var data = _updateData();
                if (data) {
                  this.$emit('data-update', data);
                  return data;
                } else {
                  sp.log.error("silverpeas-form - no data updated...");
                  return sp.promise.rejectDirectlyWith();
                }
              }.bind(this))['catch'](function() {
                sp.log.debug("silverpeas-form - validation failed...");
                this.$emit('validation-fail');
                return sp.promise.rejectDirectlyWith();
              }.bind(this));
            },
            cancel : function() {
              notyReset();
              this.$emit('cancel');
            }
          });
        },
        computed : {
          isHeader : function() {
            return !!this.$slots.header;
          },
          isBody : function() {
            return !!this.$slots.default;
          },
          isFooter : function() {
            return !!this.$slots.footer;
          },
          isLegend : function() {
            return !!this.$slots.legend || this.mandatoryLegend;
          },
          isManualActions : function() {
            return this.manualActions;
          }
        }
      });
    });
  });

  /**
   * silverpeas-mandatory-indicator is an HTML element which display an indicator of mandatory form
   * input.
   *
   * @param iconUrl (Optional) can be used to override the default icon url.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-mandatory-indicator></silverpeas-mandatory-indicator>
   */
  Vue.component('silverpeas-mandatory-indicator', {
    template : '<span>&#160;<img v-bind:src="iconUrl" height="5" width="5" alt=""/></span>', props : {
      iconUrl : {
        'type' : String,
        'default' : webContext + '/util/icons/mandatoryField.gif'
      }
    }
  });
})();
