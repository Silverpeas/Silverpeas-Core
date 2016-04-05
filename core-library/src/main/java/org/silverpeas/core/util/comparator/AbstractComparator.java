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
package org.silverpeas.core.util.comparator;

import java.util.Comparator;

/**
 * @author yohann.chastagnier
 * @param <C>
 */
public abstract class AbstractComparator<C> implements Comparator<C> {

  /**
   * Centralizes bean comparison mechanism
   * @return
   */
  protected static <T> boolean areInstancesComparable(final T comp1, final T comp2) {
    return (comp1 != null && comp2 != null);
  }

  /**
   * Centralizes bean comparison mechanism
   * @return
   */
  protected static <T> int compareInstance(final T comp1, final T comp2) {
    int result = 0;
    if (comp1 == null && comp2 != null) {
      result = -1;
    } else if (comp1 != null && comp2 == null) {
      result = 1;
    }
    return result;
  }

  /**
   * Centralizes bean comparison mechanism
   * @return
   */
  protected static <T> int compare(final Comparable<? super T> comp1, final T comp2) {
    int result = 0;
    if (comp1 == null && comp2 != null) {
      result = -1;
    } else if (comp1 != null && comp2 == null) {
      result = 1;
    } else if (comp1 != null && comp2 != null) {
      result = comp1.compareTo(comp2);
    }
    return result;
  }
}
