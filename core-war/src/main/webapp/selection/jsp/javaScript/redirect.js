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

/*
 * Gives back the selection of the user panel opened in a popup to the window that opened it: the
 * window is redirected to the URL given by the data-redirection-url attribute of the body, and
 * then the popup is closed.
 * The URL can target a protected resource (an action of saving, updating, ...): it is stamped
 * with the security tokens of the session, as the links of the pages are, otherwise the request
 * is rejected as a CSRF attack.
 */
(function() {
  const url = document.body.dataset.redirectionUrl;
  window.opener.location.href = typeof stampURL === 'function' ? stampURL(url) : url;
  window.close();
})();
