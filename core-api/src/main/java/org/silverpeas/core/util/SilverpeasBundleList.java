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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class permits to handle the result of property value list search.
 * @author Yohann Chastagnier
 */
public class SilverpeasBundleList extends ArrayList<String> {

  /**
   * Initializes a list with the given string values if any.
   * @param values none, one or several string values.
   * @return the initialized list.
   */
  public static SilverpeasBundleList with(String... values) {
    SilverpeasBundleList list = new SilverpeasBundleList();
    Collections.addAll(list, values);
    return list;
  }

  /**
   * Hidden constructor
   */
  private SilverpeasBundleList() {
  }

  /**
   * Converts the list into an array of {@link String}.
   * @return the current list converted into {@link String} array. No treatment is performed about
   * not defined values.
   */
  public String[] asStringArray() {
    return toArray(new String[size()]);
  }

  /**
   * Converts the list into an array of {@link String}.
   * @param defaultValue the default value to register if one is not defined.
   * @return the current list converted into {@link String} array with default value applied on not
   * defined values.
   */
  public String[] asStringArray(String defaultValue) {
    return asStringList(defaultValue).toArray(new String[size()]);
  }

  /**
   * Converts the list into an list of {@link String} with default value if necessary.
   * @param defaultValue the default value to register if one is not defined.
   * @return the current list converted into {@link String} list with default value applied on not
   * defined values.
   */
  public List<String> asStringList(String defaultValue) {
    List<String> values = new ArrayList<>(size());
    values.addAll(this.stream().map(value -> StringUtil
        .defaultStringIfNotDefined(value, defaultValue))
        .collect(Collectors.toList()));
    return values;
  }

  /**
   * Converts the list into an array of {@link Integer}.
   * @return the current list converted into {@link Integer} array with null value applied on not
   * defined values.
   * @throws NumberFormatException if the string does not contain a parsable integer.
   */
  public Integer[] asIntegerArray() {
    return asIntegerArray(null);
  }

  /**
   * Converts the list into a list of {@link Integer}.
   * @return the current list converted into {@link Integer} list with null value applied on not
   * defined values.
   * @throws NumberFormatException if the string does not contain a parsable integer.
   */
  public List<Integer> asIntegerList() {
    return asIntegerList(null);
  }

  /**
   * Converts the list into an array of {@link Integer}.
   * @param defaultValue the default value to register if one is not defined.
   * @return the current list converted into {@link Integer} array with default value applied on not
   * defined values.
   * @throws NumberFormatException if the string does not contain a parsable integer.
   */
  public Integer[] asIntegerArray(Integer defaultValue) {
    return asIntegerList(defaultValue).toArray(new Integer[size()]);
  }

  /**
   * Converts the list into a list of {@link Integer}.
   * @param defaultValue the default value to register if one is not defined.
   * @return the current list converted into {@link Integer} list with default value applied on not
   * defined values.
   * @throws NumberFormatException if the string does not contain a parsable integer.
   */
  public List<Integer> asIntegerList(Integer defaultValue) {
    List<Integer> values = new ArrayList<>(size());
    values.addAll(this.stream()
        .map(value -> StringUtil.isDefined(value) ? Integer.valueOf(Integer.parseInt(value)) : defaultValue)
        .collect(Collectors.toList()));
    return values;
  }
}
