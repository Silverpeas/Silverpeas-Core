/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util.comparator;

import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This tool handles all the boilerplate of sort implementations of list of data.
 * <p>
 *   By default, the text comparison takes not care about accent and case.<br/>
 *   To deactivate this behavior, please call from constructor {@link #strictComparisonOnText()}
 *   method.
 * </p>
 * @author Yohann Chastagnier
 * @param <C>
 */
public abstract class AbstractComplexComparator<C> extends AbstractComparator<C> {
  private static final long serialVersionUID = 735351151555082611L;

  protected AbstractComplexComparator() {
    super();
  }

  /**
   * Value list to compare
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
        return result * sens;
      }

      // Value
      if (areInstancesComparable(curO1, curO2)) {
        result = compare((Comparable) curO1, curO2);
        if (result != 0) {
          return result * sens;
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
    private final List<Integer> sens = new ArrayList<>();

    /** Valeur */
    private final List<Object> values = new ArrayList<>();

    /**
     * Default constructor
     */
    public ValueBuffer() {
      // NTD
    }

    /**
     * Adding a value
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
     */
    public ValueBuffer append(final Object object) {
      return append(object, true);
    }

    /**
     * @return the sens
     */
    List<Integer> getSens() {
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

    @SuppressWarnings("NullableProblems")
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
        return compare(string, o.string);
      }

      // Identical
      return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final StringWrapper that = (StringWrapper) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }
  }
}
