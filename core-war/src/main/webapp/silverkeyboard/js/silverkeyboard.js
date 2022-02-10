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
 * FLOSS exception. You should have received a copy of the text describing
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

  let silverKeyboardDebug = false;

  /**
   * SilverKeyboard version 1.0.0.
   * It is currently based upon <a href="https://virtual-keyboard.js.org/">Simple Keyboard</a>
   */
  if (window.top.SilverKeyboard) {
    whenSilverpeasReady(function() {
      if (!window.SilverKeyboard) {
        window.SilverKeyboard = window.top.SilverKeyboard;
      }
      __logDebug("Enabling the virtual keyboard on " + window.name);
      window.SilverKeyboard.enableFor(window.document);
    });
    return;
  }

  /**
   * @param locale an ISO 639-1 code of a locale (currently only 'en', 'de' and 'fr' are
   * supported)
   * @param silverKeyboardLayoutAdapter the adapter to deals with for UI.
   * @constructor
   */
  let SilverKeyboard = function(locale, silverKeyboardLayoutAdapter) {

    if (typeof silverKeyboardLayoutAdapter === 'undefined') {
      throw new Error("No virtual keyboard widget's location defined in the DOM!");
    }

    const inputTypes = ['date', 'datetime-local', 'email', 'month', 'number', 'password', 'search',
      'tel', 'text', 'time', 'url', 'week'];

    const kbdClassName = 'kbd-textual-input';

    var _webBrowserCompatible = true;

    var _keyboard = undefined;

    var _initialized = false;

    var _inputs = [];

    let __cache = new SilverpeasCache("SilverpeasVirtualKeyboard");

    /**
     * Indicates if the keyboard is activated.
     * @returns {*}
     */
    this.isActivated = function() {
      return __cache.get('activated');
    };

    /**
     * Updates the activation state of the keyboard.
     * @returns {boolean}
     */
    this.switchActivation = function() {
      let newActivationState = !this.isActivated();
      __cache.put("activated", newActivationState);
      silverKeyboardLayoutAdapter.refreshActivationDock(this);
      if (newActivationState) {
        if (_initialized) {
          __logInfo("Enable the virtual keyboard");
        }
        this.init().enableFor(window.document);
      } else {
        __logInfo("Disable the virtual keyboard");
      }
      return newActivationState;
    };

    /**
     * Initializes the virtual keyboard with the specified locale and for the specified selectors of
     * input HTML element.
     * @param selectors a list of selectors of HTML elements for which the keyboard has to be used.
     * If not set, by default selects all the inputs and the textarea: ["input", "textarea"].
     * Nevertheless, among all the scanned HTML inputs, only textual ones are taken in charge by
     * the virtual keyboard.
     * @return {Window.SilverKeyboard} instance.
     */
    this.init = function(selectors) {
      if (_initialized) {
        __logDebug("Already initialized!");
        return this;
      }

      if (!this.isActivated()) {
        __logDebug("It is not activated");
        return this;
      }

      __logInfo("Enable the virtual keyboard");

      if (selectors === undefined || selectors === null || selectors.length === 0) {
        _inputs = ['input', 'textarea'];
      } else {
        _inputs = selectors;
      }

      __logDebug("Inputs to support:", _inputs);

      let layout = '';
      let display;
      if (locale === 'fr') {
        layout = french;
        display = {
          '{bksp}': 'Suppr arri&egrave;re',
          '{enter}': 'Entrer',
          '{shift}': 'Maj',
          '{lock}': 'Verr. Maj'
        };
      } else if (locale === 'en') {
        layout = english;
      } else if (locale === 'de') {
        layout = german;
      }

      try {
        let Keyboard = window.SimpleKeyboard['default'];
        _keyboard = new Keyboard({
          onChange : onChange,
          onKeyPress : onKeyPress,
          newLineOnEnter : true,
          tabCharOnTab : false,
          preventMouseDownDefault : true,
          layout : layout,
          maxLength : {},
          mergeDisplay : true,
          display : display
        });
      } catch (e) {
        __logError("The WEB browser is not compatible!");
        _webBrowserCompatible = false;
      }

      _initialized = true;
      return this;
    };

    /**
     * Enables the Silverpeas virtual keyboard for the specified node. It will look for the all the
     * supported textual inputs to interact with them.
     * @param node an HTML node. By default the current document.
     * @return {Window.top.SilverKeyboard} itself.
     */
    this.enableFor = function(node) {
      if (!_initialized) {
        __logDebug("Cannot enable the keyboard as it has not been initialized");
        return this;
      }

      if (!node) {
        throw new Error("No HTML node specified for which SilverKeyboard is enabled!");
      }

      if (!_webBrowserCompatible) {
        __logDebug("Not enabling keyboard for current window as the WEB browser is not compatible");
        return this;
      }

      if (!window.MutationObserver) {
        __logDebug("The web browser doesn't support MutationObserver. The virtual keyboard cannot be enabled");
        return this;
      }

      if (window.__silverpeas_virtualkeyboard_observer_initialized) {
        __logDebug("Observers already initialized");
        return this;
      }

      scanInputs(node.body);

      let observer = new MutationObserver(function(mutations) {
        mutations.filter(function(aMutation) {
          return aMutation.target.querySelectorAll;
        }).forEach(function(aMutation) {
          scanInputs(aMutation.target);
        })
      }.bind(this));
      observer.observe(node.body, {
        childList : true, subtree : true
      });

      focusCurrentElementIfHandled(node);

      window.__silverpeas_virtualkeyboard_observer_initialized = true;

      return this;
    };

    function focusCurrentElementIfHandled(node) {
      let activeElement = node.activeElement;
      if (activeElement) {
        _inputs.filter(function(inputType) {
          return inputType.toLowerCase() === activeElement.tagName.toLowerCase()
        }).forEach(function() {
          activeElement.blur();
          activeElement.focus();
        })
      }
    }

    function isATextualInput(node) {
      let nodeName = node.nodeName.toLowerCase();
      return nodeName === 'textarea' || (nodeName === 'input' && inputTypes.includes(node.type))
    }

    function processTextualInput($input) {
      if (isATextualInput($input)) {
        if (!$input.classList.contains(kbdClassName)) {
          __logDebug("Input found: ", $input);
          $input.classList.add(kbdClassName);
          attachEventsListening($input);
        }
      }
    }

    function scanInputs(doc) {
      doc.querySelectorAll(_inputs.join(',')).forEach(function($input) {
        processTextualInput($input);
      });
    }

    let attachEventsListening = function($input) {
      $input.addEventListener('click', function() {
        _keyboard.caretPosition = getCaretPosition($input);
      });
      $input.addEventListener('focus', function() {
        if (!this.isActivated()) {
          __logDebug("It is no more activated, so not displayed");
          return;
        }
        silverKeyboardLayoutAdapter.show();
        _keyboard.setInput($input.value, $input.name);
        if ($input.setSelectionRange) {
          $input.setSelectionRange($input.value.length, $input.value.length);
          _keyboard.caretPosition = $input.value.length;
        }
        switchToInput($input);
      }.bind(this));
      $input.addEventListener('focusout', function() {
        silverKeyboardLayoutAdapter.hide();
      });
      $input.addEventListener('input', function() {
        _keyboard.setInput($input.value, $input.name);
      });
    }.bind(this);

    function switchToInput($input) {
      let maxLength = _keyboard.options.maxLength;
      if ($input.maxLength > 0) {
        maxLength[$input.name] = $input.maxLength;
      }
      _keyboard.setOptions({
        maxLength : maxLength,
        inputName : $input.name
      });
      _keyboard.focusedInput = $input;
    }

    function getCaretPosition($input) {
      if ($input.createTextRange) {
        var range = document.selection.createRange().duplicate();
        range.moveEnd('character', $input.value.length);
        if (range.text === '') {
          return $input.value.length;
        }
        return $input.value.lastIndexOf(range.text);
      } else {
        return $input.selectionStart;
      }
    }

    function isVisible($input) {
      return $input.type.toLowerCase() !== 'hidden' && $input.offsetHeight > 0 &&
          $input.offsetWidth > 0;
    }

    function nextValidInput($allInputs, $currentInput) {
      let $nextInput;
      for (let i = 0; i < $allInputs.length; i++) {
        if ($allInputs[i] === $currentInput) {
          let next = i;
          do {
            next = (next + 1 < $allInputs.length) ? next + 1 : 0;
          } while (!isVisible($allInputs[next]));
          $nextInput = $allInputs[next];
          break;
        }
      }
      return $nextInput;
    }

    function onChange(input) {
      let $input = _keyboard.focusedInput;
      $input.value = input;
      let caretPosition = _keyboard.caretPosition;
      if (caretPosition !== null) {
        if ($input.setSelectionRange) {
          $input.setSelectionRange(caretPosition, caretPosition);
        }
      }
    }

    function onKeyPress(button) {

      /**
       * Cycle the keyboard focus over the different inputs
       */
      if (button === '{tab}') {
        let $input = _keyboard.focusedInput;
        let doc = $input.ownerDocument;
        if (doc) {
          let $allInputs = doc.querySelectorAll('.' + kbdClassName);
          let $nextInput = nextValidInput($allInputs, $input);
          if ($nextInput) {
            $nextInput.focus();
          }
        }
      }

      /**
       * If you want to handle the shift and caps lock buttons
       */
      else if (button === '{shift}' || button === '{lock}') {
        let currentLayout = _keyboard.options.layoutName;
        let shiftToggle = currentLayout === 'default' ? 'shift' : 'default';

        _keyboard.setOptions({
          layoutName : shiftToggle
        });
      }
    }

    // UI activation initialization
    silverKeyboardLayoutAdapter.refreshActivationDock(this);
  };

  let SilverKeyboardLayoutAdapter = function() {
    let keyBoardAnchor = document.createElement('div');
    keyBoardAnchor.id = 'virtual-keyboard';
    keyBoardAnchor.classList.add('simple-keyboard');
    if (typeof spLayout === 'undefined') {
      __logDebug("Installing DOM keyboard container");
      let keyBoardContainer = document.createElement('div');
      keyBoardContainer.classList.add('virtual-keyboard-without-silverpeas-layout');
      keyBoardContainer.style.display = 'none';
      keyBoardContainer.appendChild(keyBoardAnchor);
      document.body.appendChild(keyBoardContainer);
      this.show = function() {
        keyBoardContainer.style.display = '';
      };
      this.hide = function() {
        keyBoardContainer.style.display = 'none';
      };
    } else {
      __logDebug("Installing DOM keyboard container into Silverpeas's Layout");
      let keyboardLayoutPart = spLayout.getCustomFooter().newCustomPart('virtual-keyboard');
      keyboardLayoutPart.getContainer().appendChild(keyBoardAnchor);
      this.show = function() {
        keyboardLayoutPart.show();
      };
      this.hide = function() {
        keyboardLayoutPart.hide();
      };
    }
    this.refreshActivationDock = function(spKeyboard) {
      let message = VirtualKeyboardBundle.get(spKeyboard.isActivated() ? 'vk.d' : 'vk.a');
      document.querySelectorAll(".silverpeas-keyboard-activation-dock").forEach(function(dock) {
        dock.innerHTML = message;
      })
    };
  };

  /**
   * Logs error messages.
   * @private
   */
  function __logError() {
    let messages = [];
    Array.prototype.push.apply(messages, arguments);
    messages.splice(0, 0, "[Virtual Keyboard]");
    sp.log.error.apply(this, messages);
  }

  /**
   * Logs info messages.
   * @private
   */
  function __logInfo() {
    let messages = [];
    Array.prototype.push.apply(messages, arguments);
    messages.splice(0, 0, "[Virtual Keyboard]");
    sp.log.info.apply(this, messages);
  }

  /**
   * Logs debug messages.
   * @private
   */
  function __logDebug() {
    if (silverKeyboardDebug) {
      let mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      let messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "[Virtual Keyboard]");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }

  function __checkIsDefined(objName, obj) {
    if (!obj) {
      throw new Error(objName + " should be defined to use the virtual keyboard!");
    }
  }

  window.top.SilverKeyboard = true;
  whenSilverpeasReady(function() {
    __logDebug("Initialize the virtual keyboard");
    __checkIsDefined('Virtual keyboard setting', VirtualKeyboardSettings);
    window.top.SilverKeyboard = new SilverKeyboard(VirtualKeyboardSettings.get('u.l'), new SilverKeyboardLayoutAdapter())
        .init()
        .enableFor(window.document);
  });
})();
