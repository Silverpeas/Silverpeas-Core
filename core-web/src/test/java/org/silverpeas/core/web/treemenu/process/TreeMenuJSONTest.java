/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.treemenu.process;

import org.silverpeas.core.web.treemenu.model.MenuItem;
import org.silverpeas.core.web.treemenu.model.NodeType;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the decoding and encoding of menu items in JSON.
 */
public class TreeMenuJSONTest {

  @Test
  public void testGetMenuItemAsJSONObject() throws Exception {
    MenuItem item =
        new MenuItem("Add item", "itemAdd", 0, NodeType.COMPONENT, true, null, "Todo12");
    String json = TreeMenuJSON.getMenuItemAsJSONObject(item);
    assertThat(json.contains("\"id\":\"itemAdd\""), is(true));
    assertThat(json.contains("\"label\":\"Add item\""), is(true));
    assertThat(json.contains("\"level\":0"), is(true));
    assertThat(json.contains("\"nodeType\":\"COMPONENT\""), is(true));
    assertThat(json.contains("\"isLeaf\":true"), is(true));
    assertThat(json.contains("\"nbObjects\":-1"), is(true));
    assertThat(json.contains("\"componentName\""), is(false));
    assertThat(json.contains("\"labelStyle\""), is(false));
    assertThat(json.contains("\"target\""), is(false));
    assertThat(json.contains("\"url\""), is(false));
    assertThat(json.contains("\"children\""), is(false));
  }

  @Test
  public void testGetACompleteMenuItemAsJSONObject() throws Exception {
    MenuItem item =
        new MenuItem("Add item", "itemAdd", 0, NodeType.COMPONENT, false, null, "Todo12");
    item.setComponentName("My Todo list");
    item.setNbObjects(21);
    item.setLabelStyle("darkmetal");
    String json = TreeMenuJSON.getMenuItemAsJSONObject(item);
    System.out.println(json);
    assertThat(json.contains("\"id\":\"itemAdd\""), is(true));
    assertThat(json.contains("\"label\":\"Add item\""), is(true));
    assertThat(json.contains("\"level\":0"), is(true));
    assertThat(json.contains("\"nodeType\":\"COMPONENT\""), is(true));
    assertThat(json.contains("\"isLeaf\":false"), is(true));
    assertThat(json.contains("\"nbObjects\":21"), is(true));
    assertThat(json.contains("\"componentName\":\"My Todo list\""), is(true));
    assertThat(json.contains("\"labelStyle\":\"darkmetal\""), is(true));
    assertThat(json.contains("\"target\""), is(false));
    assertThat(json.contains("\"url\""), is(false));
    assertThat(json.contains("\"children\""), is(false));
  }

  @Test
  public void testGetListAsJSONArray() throws Exception {
    MenuItem item1 =
        new MenuItem("Add item", "itemAdd", 0, NodeType.COMPONENT, true, null, "Todo12");
    MenuItem item2 =
        new MenuItem("Edit item", "itemEdit", 0, NodeType.COMPONENT, false, null, "Todo12");
    item2.setComponentName("My Todo list");
    item2.setNbObjects(21);
    item2.setLabelStyle("darkmetal");
    String json = TreeMenuJSON.getListAsJSONArray(Arrays.asList(item1, item2));
    assertThat(json.contains("\"id\":\"itemAdd\""), is(true));
    assertThat(json.contains("\"label\":\"Add item\""), is(true));
    assertThat(json.contains("\"level\":0"), is(true));
    assertThat(json.contains("\"nodeType\":\"COMPONENT\""), is(true));
    assertThat(json.contains("\"isLeaf\":true"), is(true));
    assertThat(json.contains("\"nbObjects\":-1"), is(true));

    assertThat(json.contains("\"id\":\"itemAdd\""), is(true));
    assertThat(json.contains("\"label\":\"Add item\""), is(true));
    assertThat(json.contains("\"level\":0"), is(true));
    assertThat(json.contains("\"nodeType\":\"COMPONENT\""), is(true));
    assertThat(json.contains("\"isLeaf\":false"), is(true));
    assertThat(json.contains("\"nbObjects\":21"), is(true));
    assertThat(json.contains("\"componentName\":\"My Todo list\""), is(true));
    assertThat(json.contains("\"labelStyle\":\"darkmetal\""), is(true));

    assertThat(json.contains("\"target\""), is(false));
    assertThat(json.contains("\"url\""), is(false));
    assertThat(json.contains("\"children\""), is(false));
  }
}