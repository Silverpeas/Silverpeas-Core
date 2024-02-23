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

(function($window) {

  /**
   * sp.a11y MUST be same instance shared between different parts of the Silverpeas's layout.
   */

  setTimeout(function() {
    const selectHandler = function(e) {
      const selected = e.detail.data;
      sp.a11y.applyOn(selected.definitionId, selected.valueId, $window);
    };
    const unselectHandler = function(e) {
      const selected = e.detail.data;
      sp.a11y.unapplyOn(selected.definitionId, selected.valueId, $window);
    };
    sp.a11y.addEventListener('select', selectHandler);
    sp.a11y.addEventListener('unselect', unselectHandler);
  }, 0);

  if (typeof window._spWindow_isSilverpeasMainWindow === 'function' &&
      !_spWindow_isSilverpeasMainWindow()) {
    sp.a11y = _spWindow_getSilverpeasMainWindow().sp.a11y;
    if (sp.a11y) {
      sp.a11y.initializeOn($window);
      return;
    }
  }

  const CONTRAST_LABEL = sp.i18n.get("a.p.c");
  const CONTRAST_GREY_LEVEL_OPTION_LABEL = sp.i18n.get("a.p.c.gl");
  const CONTRAST_HIGH_CONTRAST_OPTION_LABEL = sp.i18n.get("a.p.c.hc");
  const CONTRAST_NEGATIVE_HIGH_CONTRAST_OPTION_LABEL = sp.i18n.get("a.p.c.nhc");

  const FONT_LABEL = sp.i18n.get("a.p.f");
  const FONT_BIG_OPTION_LABEL = sp.i18n.get("a.p.f.b");

  const CLICK_ZONE_LABEL = sp.i18n.get("a.p.cz");
  const CLICK_ZONE_ENLARGED_OPTION_LABEL = sp.i18n.get("a.p.cz.e");

  const LINKS_LABEL = sp.i18n.get("a.p.l");
  const LINKS_UNDERLINED_OPTION_LABEL = sp.i18n.get("a.p.l.u");

  /**
   * Definitions of the different accessibility possibilities.
   * Each attribute name is the identifier of a definition.
   * A definition is composed of an i18n label and a list of values.
   * The list of values is represented by an object where each attribute represents the identifier
   * of a value.
   * A value is composed of an i18n label and an array of CSS classes to apply on HTML bodies of
   * the different window managed by the Silverpeas's layout.
   */
  const ACCESSIBILITY_DEFINITIONS = {
    'contrast' : {
      label : CONTRAST_LABEL,
      values : {
        'gl' : {
          label : CONTRAST_GREY_LEVEL_OPTION_LABEL,
          cssClasses : ['a11y-contrast-grey-level']
        },
        'hc' : {
          label : CONTRAST_HIGH_CONTRAST_OPTION_LABEL,
          cssClasses : ['a11y-high-contrast']
        },
        'nhc' : {
          label : CONTRAST_NEGATIVE_HIGH_CONTRAST_OPTION_LABEL,
          cssClasses : ['a11y-negative-high-contrast']
        }
      }
    },
    'font' : {
      label : FONT_LABEL,
      values : {
        'b' : {
          label : FONT_BIG_OPTION_LABEL,
          cssClasses : ['a11y-big-font']
        }
      }
    },
    'click-zone' : {
      label : CLICK_ZONE_LABEL,
      values : {
        'e' : {
          label : CLICK_ZONE_ENLARGED_OPTION_LABEL,
          cssClasses : ['a11y-enlarged-click-zone']
        }
      }
    },
    'links' : {
      label : LINKS_LABEL,
      values : {
        'u' : {
          label : LINKS_UNDERLINED_OPTION_LABEL,
          cssClasses : ['a11y-underlined-links']
        }
      }
    }
  }

  const __cache = new SilverpeasCache('sp-a11y');

  const a11yRepository = new function() {
    const enabled = __cache.get('enabled') || {};
    const __sync = function() {
      __cache.put('enabled', enabled);
    };
    this.get = function(definitionId) {
      return enabled[definitionId];
    };
    this.exists = function(definitionId, valueId) {
      return enabled[definitionId] === valueId;
    };
    this.save = function(definitionId, valueId) {
      enabled[definitionId] = valueId;
      __sync();
    };
    this['delete'] = function(definitionId) {
      delete enabled[definitionId];
      __sync();
    }
  }

  /**
   * a11y core manager provided directly under 'sp' entry.
   * Silverpeas's event dispatching is applied on. Current handled events are:
   * - 'update-definitions' without any data, when an a11y definitions are updated
   * - 'select' with data {definitionId, valueId}, when an a11y is selected (or enabled)
   * - 'unselect' with data {definitionId}, when an a11y is unselected (or disabled)
   */
  sp.a11y = new function() {
    applyEventDispatchingBehaviorOn(this);

    /**
     * Removes the definition from its identifier.
     * 'update-definitions' is dispatch after remove.
     * @param definitionIds identifiers of a definition
     */
    this.removeDefinitions = function(definitionIds) {
      const ids = typeof definitionIds === "string" ? [definitionIds] : definitionIds;
      ids.forEach(function(id) {
        delete ACCESSIBILITY_DEFINITIONS[id];
      })
      this.dispatchEvent('update-definitions');
    }

    /**
     * Merges given definitions with existing ones.
     * 'update-definitions' is dispatch after merge.
     * @param newDefinitions definition object observing same structure than
     *     ACCESSIBILITY_DEFINITIONS.
     */
    this.mergeDefinitionsWith = function(newDefinitions) {
      extendsObject(ACCESSIBILITY_DEFINITIONS, newDefinitions);
      this.dispatchEvent('update-definitions');
    }

    /**
     * Gets all possible definitions.
     * @param filterOnEnabledIds boolean, true to filter on enabled ids
     * @returns {*[]}
     */
    this.getDefinitions = function(filterOnEnabledIds) {
      const definitions = [];
      for (let dKey in ACCESSIBILITY_DEFINITIONS) {
        const reg = ACCESSIBILITY_DEFINITIONS[dKey];
        const current = {id : dKey, label : reg.label, values : []};
        for (let vKey in reg.values) {
          if (!filterOnEnabledIds || a11yRepository.exists(dKey, vKey)) {
            const value = reg.values[vKey];
            const copy = {
              id : vKey,
              label : value.label,
              cssClasses : value.cssClasses};
            current.values.push(copy);
            current.values[vKey] = copy;
          }
        }
        if (current.values.length > 0) {
          definitions.push(current);
          definitions[dKey] = current
        }
      }
      return definitions;
    };

    /**
     * Selects (or enables) an a11y parameter.
     * @param definitionId the identifier of the parameter.
     * @param valueId the identifier of a possible value of the parameter.
     */
    this.select = function(definitionId, valueId) {
      const currentValue = __getDefinitionEnabledValue(definitionId);
      if (currentValue !== valueId) {
        if (currentValue) {
          this.unselect(definitionId, currentValue);
        }
        a11yRepository.save(definitionId, valueId);
        this.dispatchEvent('select', {
          definitionId : definitionId,
          valueId : valueId
        });
      }
    }

    /**
     * Unselects (or disables) an a11y parameter.
     * @param definitionId the identifier of the parameter.
     * @param valueId the identifier of a possible value of the parameter.
     */
    this.unselect = function(definitionId, valueId) {
      const currentValue = __getDefinitionEnabledValue(definitionId);
      if (currentValue === valueId) {
        a11yRepository['delete'](definitionId);
        this.dispatchEvent('unselect', {
          definitionId : definitionId,
          valueId : valueId
        });
      }
    }

    /**
     * Applies the specified definition value.
     * @param definitionId the identifier of the parameter.
     * @param valueId the identifier of a possible value of the parameter.
     * @param $w the window to manage.
     * @returns {*}
     */
    this.applyOn = function(definitionId, valueId, $w) {
      __getDefinitionValue(definitionId, valueId).cssClasses.forEach(function(cssClass) {
        $w.document.querySelector('body').classList.add(cssClass);
      });
    }

    /**
     * Un applies the specified definition value.
     * @param definitionId the identifier of the parameter.
     * @param valueId the identifier of a possible value of the parameter.
     * @param $w the window to manage.
     * @returns {*}
     */
    this.unapplyOn = function(definitionId, valueId, $w) {
      __getDefinitionValue(definitionId, valueId).cssClasses.forEach(function(cssClass) {
        $w.document.querySelector('body').classList.remove(cssClass);
      });
    }

    /**
     * Initializes the accessibility configuration on given window instance.
     * @param $w window instance.
     */
    this.initializeOn = function($w) {
      $w.whenSilverpeasReady(function() {
        this.getDefinitions(true).forEach(function(definition) {
          definition.values.forEach(function(value) {
            this.applyOn(definition.id, value.id, $w);
          }.bind(this));
        }.bind(this));
      }.bind(this));
    }

    const __getDefinitionValue = function(definitionId, valueId) {
      return ACCESSIBILITY_DEFINITIONS[definitionId].values[valueId];
    }

    const __getDefinitionEnabledValue = function(definitionId) {
      return a11yRepository.get(definitionId);
    }
  };
  sp.a11y.initializeOn($window);
})(window);
