/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

function goToItem(spaceId, subSpaceId, componentId, url, itemId, reloadPage) {

  top.reloadBodyMenuPart({
    "privateDomain" : spaceId,
    "privateSubDomain" : subSpaceId,
    "component_id" : componentId,
    "FromTopBar" : '1'
  });
  top.reloadBodyContentPart(url);

  if (reloadPage) {
    top.reloadHeaderPart({
      "ComponentId" : componentId,
      "SpaceId" : spaceId
    });
  } else {
    //unactivate all items
    var tr = document.getElementById('item' + itemId).parentNode;
    if (tr.hasChildNodes()) {
      var children = tr.childNodes;
      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        if (child.id != null && (child.id.substring(0, 4) == "item")) {
          child.className = "";
        }
      }
    }

    //activate item
    document.getElementById('item' + itemId).className = "activeShortcut";
  }
}