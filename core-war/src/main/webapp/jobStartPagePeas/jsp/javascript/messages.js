/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

$(document).ready(function() {
  messageTriggers.attach();
});

messageTriggers = {
  initialized : false,
  doInitialize : function() {
    if (!messageTriggers.initialized) {
      messageTriggers.initialized = true;
      $.i18n.properties({
        name : 'jobStartPagePeasBundle',
        path : webContext  + '/services/bundles/org/silverpeas/jobStartPagePeas/multilang/',
        language : '$$', // by default the language of the user in the current session
        mode : 'map'
      });
    }
  },
  attach : function() {
    messageTriggers.doInitialize();
    $warnings = $('div[id^="warning"]');
    $warnings
        .on(
            'addConfirmationMessageEnds',
            function() {
              var $this = $(this);
              var html = $this.html()
                  + '<br/><br/>'
                  + $.i18n.prop('Warning.dialog.confirmation.message.end');
              $this.html(html);
            }).prev().change(function(event) {
              if (event.target.type == "checkbox") {
                if (event.target.checked) {
                  event.stopPropagation();
                  event.target.checked = false;
                  $('#warning-' + event.target.name).popup('confirmation', {
                    callback : function() {
                      event.target.checked = true;
                      return true;
                    }
                  });
                  return false;
                }
              }
              return true;
            });
    $warnings.trigger('addConfirmationMessageEnds');
  }
}
