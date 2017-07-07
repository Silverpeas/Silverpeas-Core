/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/*
Silverpeas plugin which handles the behaviour about the connected users information.
 */

(function() {

  var $window = top.spLayout ? top.window : window;

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.spUserNavigation) {
    if (!window.spUserNavigation) {
      window.spUserNavigation = $window.spUserNavigation;
    }
    return;
  }

  /**
   * Handling the rendering of the Silverpeas's connected users.
   * @constructor
   */
  $window.spUserNavigation = new function() {

    /**
     * Go to space administration (back office side)
     */
    this.setupSpace = function(spaceId) {
      window.top.location = webContext+"/RjobManagerPeas/jsp/Main?SpaceId=" + spaceId;
    };

    this.setupComponent = function(componentId) {
      spLayout.getBody().getContent().load(webContext+"/RjobStartPagePeas/jsp/SetupComponent?ComponentId=" + componentId);
    };

  };
})();