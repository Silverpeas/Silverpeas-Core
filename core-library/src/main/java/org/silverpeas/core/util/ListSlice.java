/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This list represents a slice of a more complete lists and as such contains only the items that
 * are part of this slice.
 * @author mmoquillon
 */
public class ListSlice<T> extends ArrayList<T> {

  private int start;
  private int end;
  private int maxsize = -1;

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index from which this slice starts in the list.
   * @param sliceEndIndex the index to which this slice ends in the list.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex) {
    super();
    this.start = sliceBeginIndex;
    this.end = sliceEndIndex;
  }

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index from which this slice starts in the list.
   * @param sliceEndIndex the index to which this slice ends in the list.
   * @param originalListSize the size of the list this slice comes from.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex, int originalListSize) {
    this(sliceBeginIndex, sliceEndIndex);
    this.maxsize = originalListSize;
  }

  /**
   * Constructs a new slice for a given list.
   * @param sliceBeginIndex the index from which this slice starts in the list.
   * @param sliceEndIndex the index to which this slice ends in the list.
   * @param originalList the list from which this slice comes from.
   */
  public ListSlice(int sliceBeginIndex, int sliceEndIndex, List<? extends T> originalList) {
    super(originalList.subList(sliceBeginIndex, sliceEndIndex));
    this.start = sliceBeginIndex;
    this.end = sliceEndIndex;
    this.maxsize = originalList.size();
  }

  /**
   * Constructs a new list slice with all the items of the specified collection. This slice will
   * cover the whole specified collection; this is it represents a whole list of the specified
   * collection and as such the original list size is equal to its real size.
   * @param collection a collection of items of type T
   */
  public ListSlice(Collection<? extends T> collection) {
    super(collection);
    this.start = 0;
    this.end = collection.size();
    this.maxsize = collection.size();
  }

  /**
   * Gets the beginning index of this slice in the original list from which it comes.
   * @return the index at which this slice begins in the original list.
   */
  public int getBeginIndex() {
    return start;
  }

  /**
   * Gets the end index of this slice in the original list from which it comes.
   * @return the index at which this slice ends in the original list.
   */
  public int getEndIndex() {
    return end;
  }

  /**
   * Gets the size of the list this slice comes from.
   * @return the size of the original list or -1 if no such information is set.
   */
  public int getOriginalListSize() {
    return maxsize;
  }

  /**
   * Sets the size of the list this slice comes from.
   */
  public void setOriginalListSize(int size) {
    this.maxsize = size;
  }


}
