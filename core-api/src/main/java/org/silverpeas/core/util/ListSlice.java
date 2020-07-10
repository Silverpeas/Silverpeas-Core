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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This list represents a slice of a more complete lists and as such contains only the items that
 * are part of this slice.
 * @author mmoquillon
 */
public class ListSlice<T> extends ArrayList<T> implements SilverpeasList<T> {
  private static final long serialVersionUID = 679667310008767706L;

  private int start;
  private int end;
  private long maxsize = -1;

  /**
   * Returns an empty list.
   * @param <T> type of the list's items.
   * @return an empty list slice.
   */
  public static <T> ListSlice<T> emptyList() {
    return new ListSlice<>(Collections.emptyList());
  }

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index in the original list at which this slice starts.
   * @param sliceEndIndex the index in the original list at which this slice should end in the list.
   * The value must be equal or greater than the slice beginning index; a value equal to the
   * beginning index means the last item index in this slice isn't yet known.
   * If the index of the last item in the original list is lesser than the specified index, then
   * this slice ends up at the index of this last item.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex) {
    super(sliceEndIndex - sliceBeginIndex + 1);
    this.start = sliceBeginIndex;
    this.end = sliceEndIndex;
  }

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index in the original list at which this slice starts.
   * @param sliceEndIndex the index in the original list at which this slice should end in the list.
   * The value must be equal or greater than the slice beginning index; a value equal to the
   * beginning index means the last item index in this slice isn't yet known.
   * If the index of the last item in the original list is lesser than the specified index, then
   * this slice ends up at the index of this last item.
   * @param originalListSize the size of the original list this slice comes from. It must be greater
   * or equal to 0.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex, long originalListSize) {
    this(sliceBeginIndex, sliceEndIndex);
    if (originalListSize < 0) {
      throw new AssertionError();
    }
    this.maxsize = originalListSize;
  }

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index in the original list at which this slice starts.
   * @param sliceEndIndex the index in the original list at which this slice should end in the list.
   * If the index of the last item in the original list is lesser than the specified index, then
   * this slice ends up at the index of this last item. If this index is the same than the beginning
   * index then the slice is empty.
   * @param originalList the original list from which this slice comes from.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex, List<? extends T> originalList) {
    super(originalList.subList(sliceBeginIndex, sliceEndIndex));
    this.start = sliceBeginIndex;
    this.end = sliceEndIndex;
    this.maxsize = originalList.size();
  }

  /**
   * Constructs a new list slice with all the items of the specified collection. This slice will
   * contain the items of the whole specified collection and as such the original list size is equal
   * to its real size.
   * @param collection a collection of items of type T
   */
  public ListSlice(Collection<? extends T> collection) {
    super(collection);
    this.start = 0;
    this.end = collection.isEmpty() ? 0 : collection.size() - 1;
    this.maxsize = collection.size();
  }

  @Override
  public <U> SilverpeasList<U> newEmptyListWithSameProperties() {
    final ListSlice<U> newEmptyList = new ListSlice<>(this.start, this.end);
    newEmptyList.setOriginalListSize(this.maxsize);
    return newEmptyList;
  }

  /**
   * Gets the first index of this slice in the original list from which it comes. It is the
   * inclusive index at which this slice begins in the original list.
   * @return the index at which this slice begins in the original list.
   */
  public int getFirstIndex() {
    return start;
  }

  /**
   * Gets the last index of this slice in the original list from which it comes. It is the
   * inclusive index at which this slice ends in the original list.
   * @return the index at which this slice ends in the original list.
   */
  public int getLastIndex() {
    if (end > start && end < size()) {
      return end;
    }
    return isEmpty() ? 0 : size() - 1;
  }

  /**
   * Gets the size of the original list this slice comes from. If this slice covers all the original
   * list then {@code originalListSize() == size()}. The size of the original list can be
   * unknown at the time the slice is built, in this case -1 is returned.
   * @return the size of the original list or -1 if such a size isn't known.
   */
  @Override
  public long originalListSize() {
    return maxsize;
  }

  /**
   * Sets the size of the original list this slice comes from.
   * @param size the size of the original list
   */
  public void setOriginalListSize(long size) {
    this.maxsize = size;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListSlice)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final ListSlice<?> listSlice = (ListSlice<?>) o;

    if (start != listSlice.start) {
      return false;
    }
    if (end != listSlice.end) {
      return false;
    }
    if (maxsize != listSlice.maxsize) {
      return false;
    }
    return super.equals(listSlice);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(start)
        .append(end)
        .append(maxsize)
        .append(super.hashCode())
        .toHashCode();
  }
}
