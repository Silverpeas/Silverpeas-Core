
(function() {

  if (typeof window._spWindow_isSilverpeasMainWindow !== 'function') {
    // Module is not displayed
    return;
  }

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/a11y/js/vuejs/components/silverpeas-a11y-templates.jsp');

  /**
   * This mixin allows to have centralized declarations between all a11y components.
   * @type mixin Vuejs Component Mixin
   */
  const A11Y_MIXIN = {
    emits : ['select', 'unselect'],
    props : {
      definitions : {
        'type' : Array,
        'required' : true
      },
      currents : {
        'type' : Array,
        'required' : true
      }
    },
    methods : {
      isEnabled : function(definitionId, valueId) {
        const current = this.currents[definitionId];
        return current && current.values[valueId];
      }
    }
  }

  /**
   * This component is the main one of a11y module.
   * All concerned components MUST be hosted from this one.
   */
  SpVue.component('silverpeas-a11y',
      templateRepository.get('module', {
        mixins : [A11Y_MIXIN]
      }));

  /**
   * This component is dedicated to the a11y menu management.
   */
  SpVue.component('silverpeas-a11y-menu',
      templateRepository.get('menu', {
        mixins : [A11Y_MIXIN],
        data : function() {
          return {
            opened : false,
            closing : false
          }
        },
        methods : {
          onLeave : function() {
            this.closing = false;
          },
          open : function() {
            this.opened = true;
          },
          close : function() {
            this.closing = true;
            this.opened = false;
          }
        },
        computed : {
          isOpen : function() {
            return this.opened;
          },
          isClosed : function() {
            return !this.opened && !this.closing;
          }
        }
      }));

  /**
   * This component is dedicated to the a11y open menu.
   */
  SpVue.component('silverpeas-a11y-menu-open',
      templateRepository.get('menu-open', {
        emits : ['close'],
        mixins : [A11Y_MIXIN]
      }));

  /**
   * This component handled the display of a definition value.
   */
  SpVue.component('silverpeas-a11y-definition-value',
      templateRepository.get('definition-value', {
        mixins : [A11Y_MIXIN],
        props : {
          definition : {
            'type' : Object,
            'required' : true
          },
          value : {
            'type' : Object,
            'required' : true
          }
        },
        methods : {
          selectOrUnselect : function() {
            const eventName = this.selected ? 'unselect' : 'select';
            this.$emit(eventName, {
              'definition' : this.definition,
              'value' : this.value
            });
          }
        },
        computed : {
          id : function() {
            return this.definition.id + '-' + this.value.id;
          },
          selected : function() {
            return this.isEnabled(this.definition.id, this.value.id);
          }
        }
      }));

  /**
   * Installing the module on the main window.
   */
  whenSilverpeasReady(function() {
    if (_spWindow_isSilverpeasMainWindow()) {
      const $module = document.createElement('div');
      $module.classList.add('sp-a11y-module');
      document.body.appendChild($module);
      setTimeout(function() {
        SpVue.createApp({
          template : '<silverpeas-a11y v-bind:definitions="definitions"' +
                                     ' v-bind:currents="currents"' +
                                     ' v-on:select="select"' +
                                     ' v-on:unselect="unselect"></silverpeas-a11y>',
          data : function() {
            return {
              definitions : sp.a11y.getDefinitions(false),
              currents : sp.a11y.getDefinitions(true)
            }
          },
          mounted : function() {
            const handler = function() {
              this.currents = sp.a11y.getDefinitions(true);
            }.bind(this);
            sp.a11y.addEventListener('select', handler);
            sp.a11y.addEventListener('unselect', handler);
            sp.a11y.addEventListener('update-definitions', function() {
              this.definitions = sp.a11y.getDefinitions(false);
            }.bind(this));
          },
          methods : {
            select : function(param) {
              sp.a11y.select(param.definition.id, param.value.id);
            },
            unselect : function(param) {
              sp.a11y.unselect(param.definition.id, param.value.id);
            }
          }
        }).mount($module);
      }, 0);
    }
  })
})();