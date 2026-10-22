/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function($) {

  /**
   * When some HTML is inserted into the DOM by jQuery, each external script it contains is fetched
   * by a synchronous XMLHttpRequest and then evaluated, even if the script is already loaded into
   * the document (the templates of the AngularJS directives for example). A script already present
   * into the document isn't fetched nor evaluated again.
   */
  const evalUrl = $._evalUrl;
  $._evalUrl = function(url, options, doc) {
    const key = new URL(url, document.baseURI).href;
    const alreadyLoaded = Array.from((doc || document).scripts).some(function(script) {
      return script.src === key;
    }) || !!(window.__sp_loadedScripts && window.__sp_loadedScripts[key]);
    if (alreadyLoaded) {
      return;
    }
    return evalUrl.apply(this, arguments);
  };

  $.fn.serializeFormJSON = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
      if (o[this.name]) {
        if (!o[this.name].push) {
          o[this.name] = [o[this.name]];
        }
        o[this.name].push(this.value || '');
      } else {
        o[this.name] = this.value || '';
      }
    });
    return o;
  };
})(jQuery);
