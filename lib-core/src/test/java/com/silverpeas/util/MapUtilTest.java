/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MapUtilTest {

  public MapUtilTest() {
  }

  /**
   * Test of putAddList method, of class MapUtil.
   */
  @Test
  public void testPutAddList() {
    Map<Integer, List<String>> map = new HashMap<Integer, List<String>>(2);
    map.put(1, CollectionUtil.asList("bart"));
    String value = "homer";
    List<String> result = MapUtil.putAddList(map, 1, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAddList(map, 2, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains("lisa"));
    assertThat(map.size(), is(2));
  }

  /**
   * Test of putAddSet method, of class MapUtil.
   */
  @Test
  public void testPutAddSet() {
    Map<Integer, Set<String>> map = new HashMap<Integer, Set<String>>(2);
    map.put(1, CollectionUtil.asSet("bart"));
    String value = "homer";
    Set<String> result = MapUtil.putAddSet(map, 1, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, containsInAnyOrder("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAddSet(map, 2, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains("lisa"));
    assertThat(map.size(), is(2));
  }

  /**
   * Test of removeValueList method, of class MapUtil.
   */
  @Test
  public void testRemoveValueList() {
    Map<Integer, List<String>> map = new HashMap<Integer, List<String>>(2);
    map.put(1, CollectionUtil.asList("bart", "homer"));
    map.put(2, CollectionUtil.asList("lisa"));
    String value = "homer";
    List<String> result = MapUtil.removeValueList(map, 1, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains("bart"));
    assertThat(map.size(), is(2));

    value = "lisa";
    result = MapUtil.removeValueList(map, 2, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
    assertThat(map.size(), is(2));
  }

  /**
   * Test of removeValueSet method, of class MapUtil.
   */
  @Test
  public void testRemoveValueSet() {
    Map<Integer, Set<String>> map = new HashMap<Integer, Set<String>>(2);
    map.put(1, CollectionUtil.asSet("bart", "homer"));
    map.put(2, CollectionUtil.asSet("lisa"));
    String value = "homer";
    Set<String> result = MapUtil.removeValueSet(map, 1, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains("bart"));
    assertThat(map.size(), is(2));

    value = "lisa";
    result = MapUtil.removeValueSet(map, 2, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
    assertThat(map.size(), is(2));
  }

  /**
   * Test of equals method, of class MapUtil.
   */
  @Test
  public void testEquals() {
    Map<Integer, String> left = new HashMap<Integer, String>(2);
    Map<Integer, String> right = new HashMap<Integer, String>(2);
    left.put(1, "bart");
    left.put(2, "lisa");

    right.put(1, "bart");
    right.put(2, "lisa");
    assertThat(MapUtil.equals(left, right), is(true));

    left.remove(2);
    assertThat(MapUtil.equals(left, right), is(false));

    left.put(2, "lisa");
    left.put(3, "homer");
    assertThat(MapUtil.equals(left, right), is(false));
  }

}
