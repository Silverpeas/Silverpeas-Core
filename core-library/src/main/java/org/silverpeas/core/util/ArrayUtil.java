/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.apache.commons.lang3.ArrayUtils;

public class ArrayUtil {

  private ArrayUtil() {
  }

  public static byte[] emptyByteArray() {
    return ArrayUtils.EMPTY_BYTE_ARRAY;
  }

  public static String[] emptyStringArray() {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  public static boolean isEmpty(byte[] array) {
    return ArrayUtils.isEmpty(array);
  }

  public static boolean isEmpty(Object[] array) {
    return ArrayUtils.isEmpty(array);
  }

  public static boolean isNotEmpty(Object[] array) {
    return ArrayUtils.isNotEmpty(array);
  }

  public static String[] nullToEmpty(String[] array) {
    return ArrayUtils.nullToEmpty(array);
  }

  public static int indexOf(Object[] array, Object itemToFind) {
    return ArrayUtils.indexOf(array, itemToFind);
  }

  public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
    return ArrayUtils.subarray(array, startIndexInclusive, endIndexExclusive);
  }

  public static byte[] addAll(byte[] array1, byte... array2) {
    return ArrayUtils.addAll(array1, array2);
  }

  public static <T> T[] add(T[] array, int index, T item) {
    return ArrayUtils.insert(index, array, item);
  }

  public static String[] removeElement(String[] array, String element) {
    return ArrayUtils.removeElement(array, element);
  }

  /**
   * @see ArrayUtils#contains(Object[], Object)
   */
  public static boolean contains(final Object[] array, final Object objectToFind) {
    return ArrayUtils.contains(array, objectToFind);
  }
}
