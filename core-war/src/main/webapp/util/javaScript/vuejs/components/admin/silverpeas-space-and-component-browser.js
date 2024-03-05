/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

//# sourceURL=/silverpeas/util/javaScript/vuejs/components/admin/silverpeas-space-and-component-browser.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/admin/silverpeas-space-and-component-browser-templates.jsp');

  const SpaceAndComponentManager = function(service) {
    let roots;
    this.getRoots = function() {
      if (!roots) {
        return service.listChildrenByIdOrUri('').then(function(spaces) {
          roots = spaces;
          return spaces;
        });
      }
      return sp.promise.resolveDirectlyWith(roots);
    }
  }

  window.SCB_MAIN_MIXIN = {
    mixins : [VuejsI18nTemplateMixin, VuejsAdminServicesMixin],
    props : {
      'spaceSelectable' : {
        'type' : Boolean,
        'default' : false
      },
      'spaceContentEnabled' : {
        'type' : Boolean,
        'default' : true
      },
      'componentSelectable' : {
        'type' : Boolean,
        'default' : false
      },
      'componentContentEnabled' : {
        'type' : Boolean,
        'default' : false
      },
      'spaceFilter' : {
        'type' : Function,
        'default' : function(space) {
          return true;
        }
      },
      'componentFilter' : {
        'type' : Function,
        'default' : function(component) {
          return true;
        }
      }
    },
    methods : {
      isSpaceSelectable : function() {
        return this.spaceSelectable;
      },
      isSpaceContentEnabled : function() {
        return this.spaceContentEnabled;
      },
      isComponentSelectable : function() {
        return this.isSpaceContentEnabled() && this.componentSelectable;
      },
      isComponentContentEnabled : function() {
        return this.isSpaceContentEnabled() && this.componentContentEnabled;
      },
      componentIconUrl : function(component) {
        return webContext + "/util/icons/component/" + component.name + "Small.gif"
      }
    }
  }

  const SPACES_MIXIN = {
    emits : ['enter-root', 'enter-space', 'select-space'],
    mixins : [SCB_MAIN_MIXIN],
    props : {
      'spaces' : {
        'type' : Array,
        'default' : []
      },
      'isBrowserExtended' : {
        'type' : Boolean,
        'required' : true
      }
    }
  }

  const COMPONENTS_MIXIN = {
    emits : ['enter-component', 'select-component'],
    mixins : [SCB_MAIN_MIXIN],
    props : {
      'components' : {
        'type' : Array,
        'default' : []
      }
    }
  }

  /**
   * silverpeas-space-and-component-browser is an HTML element to render a browser of spaces and
   * components by using the VueJS framework.
   *
   * This component gives the possibility to extends the browsing experience by the management of
   * two slots.
   * The named 'extend-browser-breadcrumb' allows to add additional elements into breadcrumb
   * browsing area. The named 'extend-browser' allows to add additional browsing elements. The
   * named 'extend-browser-selection' allows to add elements about a selection.
   *
   * Managed space and component instances are provided both by {@link AdminSpaceService} and
   * {@link AdminComponentInstanceService} services.
   *
   * @example
   *  <div id="example">
   *    <silverpeas-space-and-component-browser
   *        v-bind:component-content-enabled="true"
   *        v-bind:component-filter="componentFilter"
   *        v-on:enter-root="reset"
   *        v-on:enter-space="reset"
   *        v-on:enter-component="loadLocations">
   *      <template v-if="!!$slots['extend-browser-breadcrumb']" v-slot:extend-browser-breadcrumb>
   *        ...
   *      </template>
   *      <template v-if="!!$slots['extend-browser']" v-slot:extend-browser>
   *        ...
   *      </template>
   *      <template v-if="!!$slots['extend-browser-selection']" v-slot:extend-browser-selection>
   *        ...
   *      </template>
   *    </silverpeas-space-and-component-browser>
   *  </div>
   *  <script type="text/javascript">
   *    SpVue.createApp({
   *      methods : {
   *        componentFilter : function(component) {
   *          return component.name === 'kmelia';
   *        },
   *        reset : function() {
   *          this.currentComponent = undefined;
   *          ...
   *        },
   *        loadLocations : function(component) {
   *          ...
   *        }
   *      }
   *    }).mount('#example');
   *  </script>
   *
   * @property space-selectable (optional boolean) - true enables the 'select-space' event, false
   *     otherwise (default)
   * @property space-content-enabled (optional boolean) - true means that the user can access the
   *     content of a space (components of the space in other terms), false means that the user can
   *     only navigate between spaces.
   * @property component-selectable (optional boolean) - true enables the 'select-component' event,
   *     false otherwise (default)
   * @property component-content-enabled (optional) - true means that the user can access the
   *     content of a component (resources of the component in other terms), false means that the
   *     user can only select components if 'componentSelectable' is true, or consult components of
   *     a space if 'componentSelectable' is false.
   * @property space-filter (optional function) - the function takes in parameter a space instance
   *     and MUST return a boolean value. True means that the space MUST be taken into account.
   *     Specifying an undefined function is forbidden
   * @property component-filter (optional function) - the function takes in parameter a component
   *     instance and MUST return a boolean value. True means that the component instance  MUST be
   *     taken into account. Specifying an undefined function is forbidden
   * @property admin-access (optional boolean) - true indicates that the component is used in an
   *     administration context and that the spaces and components MUST be provided without taking
   *     care about the current user access rights. If current user is not a user having full
   *     administration access, access rights are verified whatever the value of this parameter.
   *     False is the default value for standard behavior.
   *
   * @event enter-root when the user clicks on the root element.
   * @event enter-space when the user clicks on a space navigation element.
   * @event select-space when the user clicks on a space select element.
   * @event enter-component when the user clicks on a component navigation element.
   * @event select-component when the user clicks on a component select element.
   */
  SpVue.component('silverpeas-space-and-component-browser',
      templateRepository.get('space-and-component-browser', {
        emits : ['enter-root', 'enter-space', 'select-space', 'enter-component', 'select-component'],
        mixins : [VuejsApiMixin, SCB_MAIN_MIXIN],
        data : function() {
          return {
            currentSpaces : undefined,
            currentComponents : [],
            currentSpace : undefined,
            currentComponent : undefined,
          };
        },
        created : function() {
          this.extendApiWith({
            /**
             * Loading the space given as parameter and emits 'enter-space' event.
             * @param space a space instance.
             */
            enterSpace : this.loadSpace,
            /**
             * Loading the component given as parameter and emits 'enter-component' event.
             * @param component a component instance.
             */
            enterComponent : this.loadComponent
          });
          this.loadRoot();
        },
        methods : {
          loadRoot : function() {
            this.currentSpace = undefined;
            this.currentComponent = undefined;
            this.currentComponents = [];
            this.spaceAndComponentManager.getRoots().then(function(spaces) {
              this.currentSpaces = spaces.filter(this.spaceFilter);
              this.$emit('enter-root');
            }.bind(this));
          },
          loadSpace : function(space) {
            // forces breadcrumb refresh
            this.currentSpace = undefined;
            this.currentComponent = undefined;
            // promise + Vue.nextTick to perform successfully the breadcrumb refresh
            return new Promise(function(resolve, reject) {
              Vue.nextTick(function() {
                this.currentSpace = space;
                space.getContent().then(function(content) {
                  const filteredSpaces = content.spaces.filter(this.spaceFilter);
                  if (filteredSpaces.length > 0) {
                    this.currentSpaces = filteredSpaces;
                  } else {
                    space.getParent().then(function(parent) {
                      if (parent) {
                        parent.listChildren().then(function(children) {
                          this.currentSpaces = children.filter(this.spaceFilter);
                        }.bind(this));
                      } else {
                        this.spaceAndComponentManager.getRoots().then(function(spaces) {
                          this.currentSpaces = spaces.filter(this.spaceFilter);
                        }.bind(this));
                      }
                    }.bind(this));
                  }
                  this.currentComponents = content.components.filter(this.componentFilter);
                  this.$emit('enter-space', space);
                  resolve();
                }.bind(this), reject);
              }.bind(this));
            }.bind(this));
          },
          selectSpace : function(space) {
            this.$emit('select-space', space);
          },
          loadComponent : function(component) {
            this.currentComponent = component;
            this.$emit('enter-component', component);
            return sp.promise.resolveDirectlyWith(component);
          },
          selectComponent : function(component) {
            this.$emit('select-component', component);
          },
          isBrowserExtensionSlot : function() {
            return !!this.$slots['extend-browser'];
          }
        },
        computed : {
          spaceAndComponentManager : function() {
            return new SpaceAndComponentManager(this.adminSpaceService);
          },
          displayComponents : function() {
            if (!this.isSpaceContentEnabled()) {
              this.currentComponent = undefined;
              this.currentComponents = [];
            }
            return this.currentSpaces && this.isSpaceContentEnabled();
          }
        }
      }));

  SpVue.component('private-space-and-component-breadcrumb',
      templateRepository.get('breadcrumb', {
        mixins : [SCB_MAIN_MIXIN],
        props : {
          currentSpace : {
            'type' : Object,
            'default' : undefined
          },
          currentComponent : {
            'type' : Object,
            'default' : undefined
          }
        },
        data : function() {
          return {
            path : undefined
          }
        },
        methods : {
          gotoTitle : function(item) {
            return item.instanceId
                ? this.messages['gotoComponentLabel'] + ' ' + item.label
                : this.messages['gotoSpaceLabel'] + ' ' + item.label;
          },
          getCssClass:function(item) {
            return item.instanceId ? 'component' : 'space';
          },
          getCssStyle:function(item) {
            if (item.instanceId) {
              return {
                '--breadcrumb-component-icon' : 'url(' + this.componentIconUrl(item) + ')'
              };
            }
          },
          onItem : function(item) {
            if (item.instanceId) {
              this.$emit('enter-component', item)
            } else {
              this.$emit('enter-space', item)
            }
          }
        },
        watch : {
          'currentSpace' : function() {
            if (this.currentSpace) {
              this.currentSpace.getFullPath().then(function(spacePath) {
                this.path = spacePath;
              }.bind(this));
            } else {
              this.path = undefined;
            }
          },
          'currentComponent' : function() {
            if (this.currentComponent) {
              this.currentComponent.getFullPath().then(function(componentPath) {
                this.path = componentPath;
              }.bind(this));
            }
          },
        }
      }));

  SpVue.component('private-space-and-component-spaces',
      templateRepository.get('spaces', {
        mixins : [SPACES_MIXIN]
      }));

  SpVue.component('private-space-and-component-spaces-space',
      templateRepository.get('spaces-space', {
        mixins : [SPACES_MIXIN],
        props : {
          space : {
            'type' : Object,
            'required' : true
          }
        },
        created : function() {
          this.space.listChildren().then(function(children) {
            this.space.hasChildren = children.filter(this.spaceFilter).length > 0;
          }.bind(this));
        },
        methods : {
          onSpaceSelect : function() {
            this.$emit('select-space', this.space);
          },
          onSpaceNavigation : function() {
            if (this.isGotoEnabled()) {
              this.$emit('enter-space', this.space);
            }
          },
          domId : function() {
            return "space-" + this.space.id;
          },
          selectTitle : function() {
            return this.messages['selectSpaceLabel'] + ' ' + this.space.label;
          },
          gotoTitle : function() {
            if (this.isGotoEnabled()) {
              return this.messages['gotoSpaceLabel'] + ' ' + this.space.label;
            }
          },
          isGotoEnabled : function() {
            return this.space.hasChildren || this.isSpaceContentEnabled() || this.isBrowserExtended;
          }
        }
      }));

  SpVue.component('private-space-and-component-space-components',
      templateRepository.get('components', {
        mixins : [COMPONENTS_MIXIN],
        methods : {
          onComponentSelect : function(component) {
            this.$emit('select-component', component);
          },
          onComponentNavigation : function(component) {
            this.$emit('enter-component', component);
          },
          domId : function(component) {
            return component.instanceId;
          },
          selectTitle : function(component) {
            return this.messages['selectComponentLabel'] + ' ' + component.label;
          },
          gotoTitle : function(component) {
            if (this.isComponentContentEnabled()) {
              return this.messages['gotoComponentLabel'] + ' ' + component.label;
            }
          }
        }
      }));
})();
