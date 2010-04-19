/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.treeMenu.process;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silverpeas.treeMenu.model.MenuItem;

/**
 * Allows transforming menuItem to Json objects
 */
public class TreeMenuJason {

  /**
   * 
   */
  private TreeMenuJason() {
  }



  /**
   * @param item
   * @return
   */
  public static JSONObject getMenuItemAsJSONObject(MenuItem item) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", item.getKey());
    jsonObject.put("label", item.getLabel());

    jsonObject.put("level", item.getLevel());

    jsonObject.put("isLeaf", item.isLeaf());
    jsonObject.put("componentId", item.getComponentId());

    jsonObject.put("nbObjects", item.getNbObjects());
    jsonObject.put("nodeType", item.getType().toString());
    jsonObject.put("level", item.getLevel());
    jsonObject.put("componentName", item.getComponentName());
    jsonObject.put("labelStyle", item.getLabelStyle());

    return jsonObject;
  }

  /**
   * @param items
   * @return
   */
  public static JSONArray getListAsJSONArray(List<MenuItem> items) {
    JSONArray jsonArray = new JSONArray();
    for (MenuItem menuItem : items) {
      JSONObject jsonObject = getMenuItemAsJSONObject(menuItem);
      jsonArray.put(jsonObject);
    }
    return jsonArray;
  }

}
