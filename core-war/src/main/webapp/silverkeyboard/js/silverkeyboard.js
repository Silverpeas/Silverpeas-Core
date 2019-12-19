/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

  /**
   * SilverKeyboard version 1.0.0.
   * It is currently based upon <a href="https://virtual-keyboard.js.org/">Simple Keyboard</a>
   */
  if (window.top.SilverKeyboard) {
    return;
  }

  window.top.SilverKeyboard = new function() {

    var inputTypes = ['date', 'datetime-local', 'email', 'month', 'number', 'password', 'search',
      'tel', 'text', 'time', 'url', 'week'];

    var kbdClassName = 'kbd-textual-input';

    var _keyboard = undefined;

    var _initialized = false;

    var _inputs = [];

    /**
     * Initializes the virtual keyboard with the specified locale and for the specified selectors of
     * input HTML element.
     * @param locale an ISO 639-1 code of a locale (currently only 'en', 'de' and 'fr' are
     * supported)
     * @param selectors a list of selectors of HTML elements for which the keyboard has to be used.
     * If not set, by default selects all the inputs and the textarea: ["input", "textarea"].
     * Nevertheless, among all the scanned HTML inputs, only textual ones are taken in charge by
     * the virtual keyboard.
     * @return {Window.SilverKeyboard} instance.
     */
    this.init = function(locale, selectors = []) {
      if (_initialized) {
        console.log('[Virtual Keyboard] Already initialized!');
        return this;
      }

      if (!('ontouchstart' in window) || (navigator.maxTouchPoints > 0) ||
          (navigator.msMaxTouchPoints > 0)) {
        console.log(
            "[Virtual Keyboard] The screen isn't a touch one. Disable the virtual keyboard!");
        //return;
      }

      console.log("[Virtual Keyboard] The screen is a touch one. Enable the virtual keyboard");

      let kbdWidget = window.top.document.querySelector('#virtual-keyboard');
      if (kbdWidget === null) {
        throw new Error("No virtual keyboard widget's location defined in the DOM!");
      }

      if (selectors === undefined || selectors === null || selectors.length === 0) {
        _inputs = ["input", "textarea"];
      } else {
        _inputs = selectors;
      }

      console.log('[Virtual Keyboard] Inputs to support:', _inputs);

      let layout = '';
      if (locale === 'fr')
        layout = french;
      else if (locale === 'en')
        layout = english;
      else if (locale === 'de')
        layout = german;

      _keyboard = new window.SimpleKeyboard.default({
        onChange : onChange,
        onKeyPress : onKeyPress,
        onInit : function() {
          window.top.document.querySelector('#virtual-keyboard').style.display = 'none';
        },
        newLineOnEnter : true,
        tabCharOnTab : false,
        preventMouseDownDefault : true,
        layout : layout
      });

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
        throw new Error('SilverKeyboard not initialized!');
      }

      if (!node) {
        throw new Error('No HTML node specified for which SilverKeyboard is enabled!');
      }

      if (!window.MutationObserver) {
        console.log(
            "The web browser doesn't support MutationObserver. The virtual keyboard cannot be enabled");
        return this;
      }

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

      return this;
    };

    function isATextualInput(node) {
      let nodeName = node.nodeName.toLowerCase();
      return nodeName === 'textarea' || (nodeName === 'input' && inputTypes.includes(node.type))
    }

    function processTextualInput($input) {
      if (isATextualInput($input)) {
        if (!$input.classList.contains(kbdClassName)) {
          console.log('[Virtual Keyboard] Input found: ', $input);
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

    function attachEventsListening($input) {
      $input.addEventListener("focus", function(event) {
        window.top.document.querySelector('#virtual-keyboard').style.display = '';
        _keyboard.setInput(event.target.value, event.target.name);
        if (event.target.setSelectionRange) {
          event.target.setSelectionRange(event.target.value.length, event.target.value.length);
          _keyboard.caretPosition = event.target.value.length;
        }
        switchToInput(event.target);
      });
      $input.addEventListener("focusout", function(event) {
        event.target.style.border = '';
        window.top.document.querySelector('#virtual-keyboard').style.display = 'none';
      });
      $input.addEventListener("input", function(event) {
        _keyboard.setInput(event.target.value, event.target.name);
      });
    }

    function switchToInput($input) {
      if (_keyboard.options.inputName && _keyboard.options.inputName !== 'default') {
        _keyboard.focusedInput.style.border = '';
      }
      $input.style.border = 'thin solid green';
      _keyboard.setOptions({
        inputName : $input.name
      });
      _keyboard.focusedInput = $input;
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
          $input.focus();
          $input.setSelectionRange(caretPosition, caretPosition);
        }
      }
    }

    function onKeyPress(button) {

      /**
       * Cycle the keyboard focus over the different inputs
       */
      if (button === "{tab}") {
        let $input = _keyboard.focusedInput;
        let doc = $input.ownerDocument;
        if (doc) {
          let $allInputs = doc.querySelectorAll(`.${kbdClassName}`);
          let $nextInput = nextValidInput($allInputs, $input);
          if ($nextInput)
            $nextInput.focus();
        }
      }

      /**
       * If you want to handle the shift and caps lock buttons
       */
      if (button === "{shift}" || button === "{lock}") {
        let currentLayout = _keyboard.options.layoutName;
        let shiftToggle = currentLayout === "default" ? "shift" : "default";

        _keyboard.setOptions({
          layoutName : shiftToggle
        });
      }
    }
  };
})();
