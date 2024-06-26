/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.test.UnitTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@UnitTest
class MapUtilTest {

  /**
   * Test of putAddList method, of class MapUtil.
   */
  @Test
  void testPutAdd() {
    Map<Integer, Collection<String>> map = new HashMap<>(2);
    map.put(1, CollectionUtil.asList("bart"));
    String value = "homer";
    Collection<String> result = MapUtil.putAdd(ArrayList::new, map, 1, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAdd(ArrayList::new, map, 2, value);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result, contains("lisa"));
    assertThat(map.size(), is(2));
  }

  /**
   * Test of putAddAllList method, of class MapUtil.
   */
  @Test
  void testPutAddAll() {
    Map<Integer, Collection<String>> map = new HashMap<>(2);
    map.put(1, CollectionUtil.asList("bart"));
    String value = "homer";
    Collection<String> result = MapUtil.putAddAll(ArrayList::new, map, 1,
        CollectionUtil.asList(value));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAddAll(ArrayList::new, map, 2, CollectionUtil.asList(value + 1, value + 2));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("lisa1", "lisa2"));
    assertThat(map.size(), is(2));

    result = MapUtil.putAddAll(ArrayList::new, map, 3, new ArrayList<>());
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(3));

    result = MapUtil.putAddAll(ArrayList::new, map, 4, null);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(4));
  }

  /**
   * Test of putAddList method, of class MapUtil.
   */
  @Test
  void testPutAddList() {
    Map<Integer, List<String>> map = new HashMap<>(2);
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
   * Test of putAddAllList method, of class MapUtil.
   */
  @Test
  void testPutAddAllList() {
    Map<Integer, List<String>> map = new HashMap<>(2);
    map.put(1, CollectionUtil.asList("bart"));
    String value = "homer";
    List<String> result = MapUtil.putAddAllList(map, 1, CollectionUtil.asList(value));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAddAllList(map, 2, CollectionUtil.asList(value + 1, value + 2));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, contains("lisa1", "lisa2"));
    assertThat(map.size(), is(2));

    result = MapUtil.putAddAllList(map, 3, new ArrayList<>());
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(3));

    result = MapUtil.putAddAllList(map, 4, null);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(4));
  }

  /**
   * Test of putAddSet method, of class MapUtil.
   */
  @Test
  void testPutAddSet() {
    Map<Integer, Set<String>> map = new HashMap<>(2);
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
   * Test of putAddAllSet method, of class MapUtil.
   */
  @Test
  void testPutAddAllSet() {
    Map<Integer, Set<String>> map = new HashMap<>(2);
    map.put(1, CollectionUtil.asSet("bart"));
    String value = "homer";
    Set<String> result = MapUtil.putAddAllSet(map, 1, CollectionUtil.asList(value));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, containsInAnyOrder("bart", "homer"));
    assertThat(map.size(), is(1));

    value = "lisa";
    result = MapUtil.putAddAllSet(map, 2, CollectionUtil.asList(value + 1, value + 2));
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    assertThat(result, containsInAnyOrder("lisa1", "lisa2"));
    assertThat(map.size(), is(2));

    result = MapUtil.putAddAllSet(map, 3, new HashSet<>());
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(3));

    result = MapUtil.putAddAllSet(map, 4, null);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next(), is(emptyOrNullString()));
    assertThat(map.size(), is(4));
  }

  /**
   * Test of removeValueList method, of class MapUtil.
   */
  @Test
  void testRemoveValueList() {
    Map<Integer, List<String>> map = new HashMap<>(2);
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
  void testRemoveValueSet() {
    Map<Integer, Set<String>> map = new HashMap<>(2);
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
  void testEquals() {
    Map<Integer, String> left = new HashMap<>(2);
    Map<Integer, String> right = new HashMap<>(2);
    left.put(1, "bart");
    left.put(2, "lisa");

    right.put(1, "bart");
    right.put(2, "lisa");
    assertThat(MapUtil.areEqual(left, right), is(true));

    left.remove(2);
    assertThat(MapUtil.areEqual(left, right), is(false));

    left.put(2, "lisa");
    left.put(3, "homer");
    assertThat(MapUtil.areEqual(left, right), is(false));
  }

}
