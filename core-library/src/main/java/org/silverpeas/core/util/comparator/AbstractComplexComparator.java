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

import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Yohann Chastagnier
 * @param <C>
 */
public abstract class AbstractComplexComparator<C> extends
    AbstractComparator<C> {

  /**
   * Value list to compare
   * @param object
   * @return
   */
  protected abstract ValueBuffer getValuesToCompare(final C object);

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public int compare(final C o1, final C o2) {

    // Value lists to compare
    final ValueBuffer baseValues = getValuesToCompare(o1);
    final ValueBuffer comparedValues = getValuesToCompare(o2);

    // Tests
    int result = 0;
    final Iterator<Object> it1 = baseValues.getValues().iterator();
    final Iterator<Object> it2 = comparedValues.getValues().iterator();
    final Iterator<Integer> itSens = baseValues.getSens().iterator();
    Object curO1;
    Object curO2;
    Integer sens;
    while (it1.hasNext()) {
      curO1 = it1.next();
      curO2 = it2.next();
      sens = itSens.next();

      // Instance
      result = compareInstance(curO1, curO2);
      if (result != 0) {
        return result * sens.intValue();
      }

      // Value
      if (areInstancesComparable(curO1, curO2)) {
        result = compare((Comparable) curO1, curO2);
        if (result != 0) {
          return result * sens.intValue();
        }
      }
    }

    // The two objects are identical
    return result;
  }

  /**
   * A value
   * @author yohann.chastagnier
   */
  public class ValueBuffer {

    /** Sens */
    final private List<Integer> sens = new ArrayList<Integer>();

    /** Valeur */
    final private List<Object> values = new ArrayList<Object>();

    /**
     * Default constructor
     */
    public ValueBuffer() {
      // NTD
    }

    /**
     * Adding a value
     * @param object
     * @param isAscending
     * @return
     */
    public ValueBuffer append(final Object object, final boolean isAscending) {
      values.add(object);
      if (isAscending) {
        sens.add(1);
      } else {
        sens.add(-1);
      }
      return this;
    }

    /**
     * Adding a value
     * @param object
     * @return
     */
    public ValueBuffer append(final Object object) {
      return append(object, true);
    }

    /**
     * @return the sens
     */
    public List<Integer> getSens() {
      return sens;
    }

    /**
     * @return the values
     */
    public List<Object> getValues() {
      return values;
    }
  }

  /**
   * Class that permits to put null or empty String value always at the bottom of a list.
   */
  public class StringWrapper implements Comparable<StringWrapper> {
    final String string;
    final boolean sort;
    final boolean emptyAtEnd;

    public StringWrapper(final String string, final boolean sort, final boolean emptyAtEnd) {
      this.string = StringUtil.isNotDefined(string) ? null : string;
      this.sort = sort;
      this.emptyAtEnd = emptyAtEnd;
    }

    @Override
    public int compareTo(final StringWrapper o) {

      // Instance
      int result = compareInstance(string, o.string);
      if (result != 0) {
        if (emptyAtEnd) {
          return result * (sort ? -1 : 1);
        } else {
          return result * (sort ? 1 : -1);
        }
      }

      // Value
      if (areInstancesComparable(string, o.string)) {
        return string.compareTo(o.string);
      }

      // Identical
      return 0;
    }
  }
}
