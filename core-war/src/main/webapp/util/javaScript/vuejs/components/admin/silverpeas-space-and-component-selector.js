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

//# sourceURL=/silverpeas/util/javaScript/vuejs/components/admin/silverpeas-space-and-component-selector.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/admin/silverpeas-space-and-component-selector-templates.jsp');

  const Selectable = {
    NO : 'no',
    ONE : 'one',
    SEVERAL : 'several'
  };

  const SCS_MAIN_MIXIN = {
    mixins : [SCB_MAIN_MIXIN],
    emits : [
      'enter-root',
      'enter-space', 'select-space', 'unselect-space', 'space-selection',
      'enter-component', 'select-component', 'unselect-component', 'component-selection'],
    props : {
      spaceSelectable : {
        'type' : String,
        'default' : Selectable.SEVERAL
      },
      spaceSelection : {
        'type' : Array,
        'default' : []
      },
      componentSelectable : {
        'type' : String,
        'default' : Selectable.SEVERAL
      },
      componentSelection : {
        'type' : Array,
        'default' : []
      }
    },
    methods : {
      isSingleSpaceSelectable : function() {
        return this.spaceSelectable === Selectable.ONE;
      },
      isAtLeastOneSpaceSelectable : function() {
        return this.spaceSelectable !== Selectable.NO;
      },
      isSingleComponentSelectable : function() {
        return this.isSpaceContentEnabled() && this.componentSelectable === Selectable.ONE;
      },
      isAtLeastOneComponentSelectable : function() {
        return this.isSpaceContentEnabled() && this.componentSelectable !== Selectable.NO;
      }
    }
  };

  /**
   * silverpeas-space-and-component-selector-popin is an HTML element to render a
   * {@link silverpeas-space-and-component-selector} component into a {@link silverpeas-popin}
   * component.
   *
   * By default, it is a validation popin and a link is displayed.
   *
   * @see this component provides the same props and events as
   *     {@link silverpeas-space-and-component-selector} and {@link silverpeas-popin} ones. Please
   *     consult the linked component definition for more details. It provides also additional
   *     props and events.
   *
   * @property max-width (optional number) - redefine default value of {@link silverpeas-popin}.
   *     1250 by default.
   * @property select-link (optional boolean) - true to display the link to open the popin, false
   *     otherwise. true by default.
   * @property select-label (optional string) - allows to specify a custom label for the link
   *     opening the popin.
   *
   * @event on the selector popin API providing open method.
   * @event validated-space-selection when type of popin is 'validation' and the user validates.
   * @event validated-component-selection when type of popin is 'validation' and the user
   *     validates.
   */
  SpVue.component('silverpeas-space-and-component-selector-popin',
      templateRepository.get('space-and-component-selector-popin', {
        emits : ['validated-space-selection', 'validated-component-selection'],
        mixins : [VuejsPopinMixin, VuejsApiMixin, SCS_MAIN_MIXIN],
        props : {
          maxWidth : {
            'type' : Number,
            'default' : 1000
          },
          selectLink : {
            'type' : Boolean,
            'default' : true
          },
          selectLabel : {
            'type' : String,
            'default' : ''
          },
          popinTitle : {
            'type' : String,
            'default' : ''
          }
        },
        data : function() {
          return {
            popinApi : undefined,
            selectedSpaces : undefined,
            selectedComponents : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : this.openSelector,
          });
        },
        methods : {
          openSelector : function() {
            this.popinApi.open({
              callback : function() {
                if (this.isSpaceSelectable()) {
                  this.$emit('validated-space-selection', this.selectedSpaces);
                }
                if (this.isComponentSelectable()) {
                  this.$emit('validated-component-selection', this.selectedComponents);
                }
                return true;
              }.bind(this)
            });
          },
          onOpen : function() {
            this.selectedSpaces = this.spaceSelection.copy();
            this.selectedComponents = this.componentSelection.copy();
            this.$emit('open');
          },
          onClose : function() {
            this.selectedSpaces = undefined;
            this.selectedComponents = undefined;
            this.$emit('close');
          },
          onSpaceSelection : function(selection) {
            this.selectedSpaces = selection;
            this.$emit('space-selection', selection);
          },
          onComponentSelection : function(selection) {
            this.selectedComponents = selection;
            this.$emit('component-selection', selection);
          }
        },
        computed : {
          selectLinkLabel : function() {
            let label = this.selectLabel;
            if (!label) {
              label = this.selectPopinTitle + '...'
            }
            return label;
          },
          selectPopinTitle : function() {
            let title = this.popinTitle;
            if (!title) {
              const no = Selectable.NO;
              const one = Selectable.ONE;
              const several = Selectable.SEVERAL;
              const allTitleBundleKeys = {};
              allTitleBundleKeys[no + '_' + no] = '';
              allTitleBundleKeys[no + '_' + one] = 'selectComponentLabel';
              allTitleBundleKeys[no + '_' + several] = 'selectComponentsLabel';
              allTitleBundleKeys[one + '_' + no] = 'selectSpaceLabel';
              allTitleBundleKeys[one + '_' + one] = 'selectSpaceAndComponentLabel';
              allTitleBundleKeys[one + '_' + several] = 'selectSpaceAndComponentsLabel';
              allTitleBundleKeys[several + '_' + no] = 'selectSpacesLabel';
              allTitleBundleKeys[several + '_' + one] = 'selectSpacesAndComponentLabel';
              allTitleBundleKeys[several + '_' + several] = 'selectSpacesAndComponentsLabel';
              const spaceSelectable = this.isAtLeastOneSpaceSelectable()
                  ? this.spaceSelectable
                  : Selectable.NO;
              const componentSelectable = this.isAtLeastOneComponentSelectable()
                  ? this.componentSelectable
                  : Selectable.NO;
              const bundleKey = allTitleBundleKeys[spaceSelectable + '_' + componentSelectable];
              title = this.messages[bundleKey];
            }
            return title;
          }
        }
      }));

  /**
   * silverpeas-space-and-component-selector is an HTML element to render a selector of spaces and
   * components by using the VueJS framework.
   *
   * This component is using the silverpeas-space-and-component-selector component and exposes its
   * named slots, 'extend-browser-breadcrumb' and 'extend-browser'.
   *
   * This component gives also the possibility to extends the selecting experience by the
   * management of 'extend-browser-selection' slot.
   *
   * @example
   *  <div id="example">
   *    <silverpeas-space-and-component-selector
   *        v-bind:space-selection="selectedSpaces"
   *        v-bind:component-selection="selectedComponents"
   *        v-bind:component-content-enabled="true"
   *        v-bind:component-filter="componentFilter"
   *        v-on:enter-root="reset"
   *        v-on:enter-space="reset"
   *        v-on:enter-component="loadLocations"
   *        v-on:space-selection="newSpaceSelection"
   *        v-on:component-selection="newComponentSelection">
   *      <template v-if="!!$slots['extend-browser-breadcrumb']" v-slot:extend-browser-breadcrumb>
   *        ...
   *      </template>
   *      <template v-if="!!$slots['extend-browser']" v-slot:extend-browser>
   *        ...
   *      </template>
   *      <template v-if="!!$slots['extend-browser-selection']" v-slot:extend-browser-selection>
   *        ...
   *      </template>
   *    </silverpeas-space-and-component-selector>
   *  </div>
   *  <script type="text/javascript">
   *    SpVue.createApp({
   *      date : function() {
   *        return {
   *          selectedSpaces : [],
   *          selectedComponents : []
   *        };
   *      },
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
   *        },
   *        newSpaceSelection : function(spaceSelection) {
   *          ...
   *        },
   *        newComponentSelection : function(componentSelection) {
   *          ...
   *        }
   *      }
   *    }).mount('#example');
   *  </script>
   *
   * @see this component provides the same props and events as
   *     {@link silverpeas-space-and-component-browser} one. Please consult the linked component
   *     definition for more details. It provides also additional props and events.
   *
   * @property space-selectable (optional string) - this prop is overridden to handle
   *     {@link Selectable} values. {@link Selectable#SEVERAL} by default.
   * @property space-selection (optional Array) - the initial space selection which MUST be an
   *     array of space instance managed by {@link AdminSpaceService}. [] by default.
   * @property component-selectable (optional string) - this prop is overridden to handle
   *     {@link Selectable} values. {@link Selectable#SEVERAL} by default.
   * @property component-selection (optional Array) - the initial component selection which MUST be
   *     an array of component instance managed by {@link AdminComponentInstanceService}. [] by
   *     default.
   *
   * @event unselect-space when the user remove a space from selected container.
   * @event space-selection when the user clicks on a space select element.
   * @event unselect-component when the user clicks on a component navigation element.
   * @event component-selection when the user clicks on a component select element.
   */
  SpVue.component('silverpeas-space-and-component-selector',
      templateRepository.get('space-and-component-selector', {
        mixins : [SCS_MAIN_MIXIN],
        data : function() {
          return {
            browserApi : undefined,
            selectedSpaces : [],
            selectedComponents : [],
            selectedItemObserver : undefined,
            applyCssClassTimer : undefined,
          };
        },
        created : function() {
          this.selectedItemObserver = new MutationObserver(function(mutationsList) {
            const distincts = new Set();
            mutationsList
                .filter(function(mutation) {
                  return mutation.target && mutation.target.id && mutation.target.tagName === 'LI';
                })
                .forEach(function(mutation) {
                  if (mutation.type === 'attributes') {
                    const nbOldValues = mutation.oldValue.replace('selected','').trim();
                    const nbNewValues = mutation.target.classList.toString().replace('selected','').trim();
                    if (nbOldValues !== nbNewValues) {
                      distincts.add(mutation.target);
                    }
                  } else {
                    distincts.add(mutation.target);
                  }
                });
            if (distincts.size > 0) {
              this.applySelectedCssClasses();
            }
          }.bind(this));
          this.updateSelectedSpaces();
          this.updateSelectedComponents();
        },
        mounted : function() {
          const __initObserve = function() {
            const $target = this.$el.querySelector(".browser");
            if ($target) {
              this.selectedItemObserver.observe($target, {
                childList : true,
                subtree : true,
                attributeFilter : ['class'],
                attributeOldValue : true
              });
            } else {
              setTimeout(__initObserve, 0);
            }
          }.bind(this);
          __initObserve();
        },
        updated : function() {
          this.applySelectedCssClasses();
        },
        beforeUnmount : function() {
          this.selectedItemObserver.disconnect();
        },
        methods : {
          enterRoot : function() {
            this.$emit('enter-root');
            this.applySelectedCssClasses();
          },
          enterSpace : function(space, fromBrowser) {
            if (!fromBrowser) {
              this.browserApi.enterSpace(space);
            } else {
              this.$emit('enter-space', space);
              this.applySelectedCssClasses();
            }
          },
          enterComponent : function(component, fromBrowser) {
            if (!fromBrowser) {
              component.getParent().then(function(space) {
                this.browserApi.enterSpace(space).then(function() {
                  this.browserApi.enterComponent(component);
                }.bind(this));
              }.bind(this));
            } else {
              this.$emit('enter-component', component);
              this.applySelectedCssClasses();
            }
          },
          selectSpace : function(space) {
            __chooseSelectedMergeStrategy(this.selectedSpaces, space,
                this.isSingleSpaceSelectable(),
                function(selectedSpace) {
                  this.selectedSpaces.addElement(selectedSpace);
                  this.$emit('select-space', selectedSpace);
                }.bind(this),
                function(unselectedSpace) {
                  this.selectedSpaces.removeElement(unselectedSpace, 'id');
                  this.$emit('unselect-space', unselectedSpace);
                }.bind(this));
            this.$emit('space-selection', this.selectedSpaces);
            this.applySelectedCssClasses();
          },
          unselectSpace : function(space) {
            this.selectSpace(space);
          },
          selectComponent : function(component) {
            __chooseSelectedMergeStrategy(this.selectedComponents, component,
                this.isSingleComponentSelectable(),
                function(selectedComponent) {
                  this.selectedComponents.addElement(selectedComponent);
                  this.$emit('select-component', selectedComponent);
                }.bind(this),
                function(unselectedComponent) {
                  this.selectedComponents.removeElement(unselectedComponent, 'id');
                  this.$emit('unselect-component', unselectedComponent);
                }.bind(this));
            this.$emit('component-selection', this.selectedComponents);
            this.applySelectedCssClasses();
          },
          unselectComponent : function(component) {
            this.selectComponent(component);
          },
          updateSelectedSpaces : function() {
            if (this.isSingleSpaceSelectable() && this.spaceSelection.length) {
              this.selectedSpaces = [this.spaceSelection[0]];
            } else {
              this.selectedSpaces = this.spaceSelection.copy();
            }
          },
          updateSelectedComponents : function() {
            if (this.isSingleComponentSelectable() && this.componentSelection.length) {
              this.selectedComponents = [this.componentSelection[0]];
            } else {
              this.selectedComponents = this.componentSelection.copy();
            }
          },
          applySelectedCssClasses : function() {
            clearTimeout(this.applyCssClassTimer);
            this.applyCssClassTimer = setTimeout(function() {
              this.$el.querySelectorAll('.browser li.selectable').forEach(function($el) {
                const classList = $el.classList;
                if (this.indexedSelectedDomIds.has($el.id)) {
                  classList.add('selected');
                } else {
                  classList.remove('selected');
                }
              }.bind(this));
            }.bind(this), 0);
          },
          isBrowserSelectionExtensionSlot : function() {
            return !!this.$slots['extend-browser-selection'];
          }
        },
        watch : {
          spaceSelection : function() {
            this.updateSelectedSpaces();
          },
          componentSelection : function() {
            this.updateSelectedComponents();
          }
        },
        computed : {
          indexedSelectedDomIds : function() {
            const indexedSelectedDomIds = new Set();
            this.selectedSpaces.forEach(function(space) {
              indexedSelectedDomIds.add('space-' + space.id);
            });
            this.selectedComponents.forEach(function(component) {
              indexedSelectedDomIds.add(component.instanceId);
            });
            return indexedSelectedDomIds;
          }
        }
      }));

  function __chooseSelectedMergeStrategy(selected, item, isSingleSelection, selectCallback,
      unselectCallback) {
    const isAlreadySelected = selected.indexOfElement(item, 'id') >= 0;
    if (isSingleSelection) {
      selected.forEach(unselectCallback);
    } else if (isAlreadySelected) {
      unselectCallback(item);
    }
    if (!isAlreadySelected) {
      selectCallback(item);
    }
  }

  SpVue.component('private-space-and-component-selector-selected-spaces',
      templateRepository.get('selected-spaces', {
        mixins : [SCS_MAIN_MIXIN],
        props : {
          displayTitle : {
            'type' : Boolean,
            'required' : true
          }
        }
      }));

  SpVue.component('private-space-and-component-selector-selected-spaces-space',
      templateRepository.get('selected-spaces-space', {
        mixins : [SCS_MAIN_MIXIN],
        props : {
          space : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            gotoTitle : undefined
          }
        },
        created : function() {
          this.space.getFullPath().then(function(path) {
            this.gotoTitle = this.messages['gotoSpaceLabel'] + ' ' + path.format();
          }.bind(this));
        }
      }));

  SpVue.component('private-space-and-component-selector-selected-components',
      templateRepository.get('selected-components', {
        mixins : [SCS_MAIN_MIXIN],
        props : {
          displayTitle : {
            'type' : Boolean,
            'required' : true
          }
        }
      }));

  SpVue.component('private-component-and-component-selector-selected-components-component',
      templateRepository.get('selected-components-component', {
        mixins : [SCS_MAIN_MIXIN],
        props : {
          component : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            gotoTitle : undefined
          }
        },
        created : function() {
          this.component.getFullPath().then(function(path) {
            this.gotoTitle = this.messages['gotoComponentLabel'] + ' ' + path.format();
          }.bind(this));
        }
      }));
})();
