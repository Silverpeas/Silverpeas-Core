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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

var __topbar_updateSelectedItem = function(itemId) {
  var $headerContainer = spLayout.getHeader().getContainer();
  var $currentItem = $headerContainer.querySelector('.activeShortcut');
  if ($currentItem) {
    $currentItem.classList.remove('activeShortcut');
  }
  if (itemId) {
    $currentItem = $headerContainer.querySelector('#item' + itemId);
    if ($currentItem) {
      $currentItem.classList.add('activeShortcut');
    }
  }
};

function goToItem(url, itemId) {
  spWindow.loadLink(url);
  __topbar_updateSelectedItem(itemId);
}

(function() {
  spLayout.getBody().ready(function() {
    var selectedItemListener = function(event) {
      __topbar_updateSelectedItem(event.detail.data.currentComponentId);
    };
    var $navigation = spLayout.getBody().getNavigation();
    $navigation.addEventListener('load', selectedItemListener, '__id__silverpeas-header-part');
    $navigation.addEventListener('changeselected', selectedItemListener, '__id__silverpeas-header-part');
  });
})();