/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL,
 * you may redistribute this Program in connection with Free/Libre Open Source
 * Software ("FLOSS") applications as described in Silverpeas's FLOSS exception.
 * You should have received a copy of the text describing the FLOSS exception,
 * and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

const messageTriggers = {
  initialized : false,
  doInitialize : function() {
    if (!messageTriggers.initialized) {
      messageTriggers.initialized = true;
      sp.i18n.load('org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle');
    }
  },
  attach : function() {
    messageTriggers.doInitialize();
    const $warnings = $('div[id^="warning"]');
    function __alwaysWarning(event) {
      const always = $('#warning-' + event.target.name).attr('always');
      return (always && always === "true") || false;
    }
    function __getDefaultSelectValueAsString(event) {
      return $('select[name="' + event.target.name + '"] option:first').val();
    }
    function __getInitialValueAsString(event, defaultValue) {
      return $('#warning-' + event.target.name).attr('initialParamValue') || defaultValue;
    }
    function __displayConfirmation(event, rollbackCallback) {
      event.stopPropagation();
      let __rollback = rollbackCallback;
      $('#warning-' + event.target.name).popup('confirmation', {
        callback : function() {
          __rollback = undefined;
          return true;
        },
        callbackOnClose : function() {
          if (typeof __rollback === 'function') {
            __rollback();
          }
        }
      });
      return false;
    }
    $warnings
        .on(
            'addConfirmationMessageEnds',
            function() {
              const $this = $(this);
              const html = $this.html()
                  + '<p>'
                  + sp.i18n.get('Warning.dialog.confirmation.message.end')
                  + '</p>';
              $this.html(html);
            }).prev().change(function(event) {
              const always = __alwaysWarning(event);
              if (event.target.type === "checkbox") {
                const newChecked = event.target.checked;
                const previousChecked = __getInitialValueAsString(event, "false") === "true";
                if (newChecked !== previousChecked && (always || newChecked)) {
                  return __displayConfirmation(event, function() {
                    event.target.checked = previousChecked;
                  });
                }
              } else if (event.target.type === "select-one") {
                const newValue = event.target.value;
                const defaultValue = __getDefaultSelectValueAsString(event);
                const previousValue = __getInitialValueAsString(event, defaultValue);
                if (newValue !== previousValue && (always || newValue !== defaultValue)) {
                  return __displayConfirmation(event, function() {
                    event.target.value = previousValue;
                  });
                }
              }
              return true;
            });
    $warnings.trigger('addConfirmationMessageEnds');
  }
};

$(document).ready(function() {
  messageTriggers.attach();
});