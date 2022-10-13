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

  /*
  SELECTIZE BEHAVIORS
   */

  /**
   * Keeps the query search after the user has leave the selectize input.
   */
  Selectize.define('KeepQueryInputWhenLeaving', function() {
    this.require('$sp$setTextboxValueDoNothing');
    var _self = this;
    this.onBlur = (function() {
      var _originalOnBlur = _self.onBlur;
      return function() {
        __keepQueryInput.call(this, _originalOnBlur, arguments);
      };
    })();
  });

  /**
   * Keeps the query search after the user has leave the selectize input.
   */
  Selectize.define('KeepQueryInputAfterSelect', function() {
    this.require('$sp$setTextboxValueDoNothing');
    var _self = this;
    this.onOptionSelect = (function() {
      var _originalOnOptionSelect = _self.onOptionSelect;
      return function() {
        __keepQueryInput.call(this, _originalOnOptionSelect, arguments);
      };
    })();
    this.hideInput = (function() {
      var _originalHideInput = _self.hideInput;
      return function() {
        __keepQueryInput.call(this, _originalHideInput, arguments);
      };
    })();
  });

  /**
   * Keeps the query search after the user has leave the selectize input.
   */
  Selectize.define('KeepAlwaysQueryInput', function() {
    __require.call(this, 'KeepQueryInputWhenLeaving');
    __require.call(this, 'KeepQueryInputAfterSelect');
  });

  /**
   * Sets the name attribute of the query HTML input.
   * The name set is the one given by attribute 'inputName' of options. If none, the identifier of
   * the HTML input is set as name.
   */
  Selectize.define('QueryInputUsedInForm', function(options) {
    var _self = this;
    options = extendsObject({
      inputName : undefined
    }, options);
    this.setup = (function() {
      var _originalSetup = _self.setup;
      return function() {
        // performing the original behavior
        _originalSetup.apply(this, arguments);
        // computing the name of the input
        var inputName = options.inputName;
        if (!inputName) {
          inputName = this.$control_input.attr('id');
        }
        // Set the value back
        this.$control_input.attr('name', inputName);
      };
    })();
  });

  /**
   * Does not selected automatically the first option into drop down in open.
   */
  Selectize.define('DoNotSelectAutomaticallyOnDropDownOpen', function() {
    var _self = this;
    this.refreshOptions = (function() {
      var _originalRefreshOptions = _self.refreshOptions;
      return function() {
        // performing the original behavior
        _originalRefreshOptions.apply(this, arguments);
        // no option automatically selected
        this.setActiveOption(null);
      };
    })();
    this.onKeyDown = (function() {
      var _originalOnKeyDown = _self.onKeyDown;
      return function(e) {
        // performing the original behavior
        _originalOnKeyDown.apply(this, arguments);
        // On key down, select the first option if no option selected
        if (e.keyCode === 40 && !this.$activeOption) {
          var $firstOption = this.$dropdown_content.find('[data-selectable]:first');
          this.setActiveOption($firstOption);
        }
      };
    })();
  });

  /**
   * Keeps last selected value when leaving the selectize input.
   */
  Selectize.define('KeepLastSelectedValueIfEmptyWhenLeaving', function() {
    var _self = this;
    var _isPlaceholder;
    var _lastValue;
    var _getCurrentFirstValue = function() {
      var _value = _self.getValue();
      return Array.isArray(_value) ? (_value.length ? _value[0] : '') : _value;
    };
    var _performLastValue = function() {
      var lastValue = _getCurrentFirstValue();
      if (lastValue && _lastValue !== lastValue) {
        _lastValue = lastValue;
        if (_isPlaceholder) {
          _self.settings.placeholder = _lastValue;
          _self.updatePlaceholder();
        }
      }
    };
    this.on('initialize', function() {
      _isPlaceholder = _self.settings.placeholder === _getCurrentFirstValue();
      _performLastValue();
    });
    this.on('change', _performLastValue);
    this.onKeyDown = (function() {
      var _originalOnKeyDown = _self.onKeyDown;
      return function(e) {
        _performLastValue();
        _originalOnKeyDown.apply(this, arguments);
      };
    })();
    this.onBlur = (function() {
      var _originalOnBlur = _self.onBlur;
      return function(e) {
        // performing the original behavior
        _originalOnBlur.apply(this, arguments);
        // handling the last removed value
        if (!_getCurrentFirstValue() && _lastValue) {
          this.setValue(_lastValue);
        }
      };
    })();
  });

  /**
   * Selects the active option when tabulation key is pressed.
   */
  Selectize.define('SelectOnTabulationKeyDown', function() {
    var _self = this;
    this.onKeyDown = (function() {
      var _originalOnKeyDown = _self.onKeyDown;
      return function(e) {
        if (e.keyCode === 9 && this.$activeOption) {
          var _value = _self.$activeOption[0].getAttribute('data-value');
          setTimeout(function() {
            _self.setValue(_value);
          });
        }
        _originalOnKeyDown.apply(this, arguments);
      };
    })();
  });

  /**
   * Does not selected automatically the first option into drop down in open.
   */
  Selectize.define('NavigationalBehavior', function() {
    __require.call(this, 'KeepAlwaysQueryInput');
  });

  /**
   * Commons behavior
   * @param _overratedMethod the overrated method.
   * @param arguments the arguments to apply.
   * @private
   */
  var __keepQueryInput = function(_overratedMethod, arguments) {
    this.$sp$setTextboxValueDoNothing.activate();
    try {
      // performing the original behavior
      _overratedMethod.apply(this, arguments);
    } finally {
      this.$sp$setTextboxValueDoNothing.deactivate();
    }
  };

  /**
   * Registering the given plugin/options if it is not yet.
   * @param pluginName the plugin name.
   * @param options the options in cas of first load.
   * @private
   */
  var __require = function(pluginName, options) {
    if (!this.plugins.loaded[pluginName]) {
      if (options) {
        this.plugins.settings[pluginName] = options;
      }
      this.loadPlugin(pluginName);
    }
  };

  /**
   * Technical behavior which must not be used outside the definition of behaviors.
   * When the instance (this or self) has attribute $sp$setTextboxValueDoNothing set at true, an
   * other behavior tells the system to not clear the search input.
   */
  Selectize.define('$sp$setTextboxValueDoNothing', function() {
    var _self = this;
    this.$sp$setTextboxValueDoNothing = {
      __requests : [],
      activate : function() {
        this.__requests.push(true);
      },
      deactivate : function() {
        this.__requests.shift();
      }
    };
    this.setTextboxValue = (function() {
      var _originalSetTextboxValue = _self.setTextboxValue;
      return function() {
        if (!this.$sp$setTextboxValueDoNothing.__requests.length) {
          _originalSetTextboxValue.apply(this, arguments);
        }
      };
    })();
  });

  whenSilverpeasReady(function() {
    jQuery('.silverpeas-selectize').selectize();
  });

})();
