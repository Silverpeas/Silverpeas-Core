/*
 * Copyright (C) 2000-2014 Silverpeas
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The pagination list is a decorator of a collection by adding it some methods used in the
 * pagination. The pagination list defines a given page to a full collection of items.
 * @author mmoquillon
 * @param <T> the type of the items in the list.
 */
public class PaginationList<T> implements List<T> {

  private final List<T> wrappedList;
  private final long maxsize;

  private PaginationList(final List<T> aList) {
    this.wrappedList = aList;
    this.maxsize = this.wrappedList.size();
  }

  private PaginationList(final List<T> aList, long maxItems) {
    this.wrappedList = aList;
    this.maxsize = maxItems;
  }

  /**
   * Decorates the specified collection with pagination features. The resulting PaginationList
   * instance defines a pagination of a single page containing the whole items of the specified
   * collection; the size of the pagination is the size of the collection.
   * @param <T> the type of the items in the collection.
   * @param aCollection the collection to decorate.
   * @return a {@code PaginationList} instance.
   */
  public static final <T> PaginationList<T> from(final Collection<T> aCollection) {
    return new PaginationList<T>(new ArrayList<T>(aCollection));
  }

  /**
   * Decorates the specified collection with pagination features. The resulting PaginationList
   * instance defines one page of a pagination and this page contains the whole items of the
   * specified collections. The page is at the size of the decorated collection but the size
   * of the pagination is greater.
   * @param <T> the type of the items in the collection.
   * @param aCollection the collection to decorate.
   * @param maxItems the maximum number of items; that is the size of the pagination.
   * @return a {@code PaginationList} instance.
   */
  public static final <T> PaginationList<T> from(final Collection<T> aCollection, long maxItems) {
    return new PaginationList<T>(new ArrayList<T>(aCollection), maxItems);
  }

  /**
   * Gets the maximum number of items the pagination contain.
   * @return the maximum size of the pagination.
   */
  public long maxSize() {
    return maxsize;
  }

  @Override
  public int size() {
    return wrappedList.size();
  }

  @Override
  public boolean isEmpty() {
    return wrappedList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return wrappedList.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return wrappedList.iterator();
  }

  @Override
  public Object[] toArray() {
    return wrappedList.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return wrappedList.toArray(a);
  }

  @Override
  public boolean add(T e) {
    if (wrappedList.size() + 1 > maxSize()) {
      throw new IllegalArgumentException(
          "No more than maxsize items can be added to this pagination");
    }
    return wrappedList.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return wrappedList.remove(o);
  }

  @Override
  public boolean containsAll(
      Collection<?> c) {
    return wrappedList.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    if (wrappedList.size() + c.size() > maxSize()) {
      throw new IllegalArgumentException(
          "No more than maxsize items can be added to this pagination");
    }
    return wrappedList.addAll(c);
  }

  @Override
  public boolean addAll(int index,
      Collection<? extends T> c) {
    if (wrappedList.size() + c.size() > maxSize()) {
      throw new IllegalArgumentException(
          "No more than maxsize items can be added to this pagination");
    }
    return wrappedList.addAll(index, c);
  }

  @Override
  public boolean removeAll(
      Collection<?> c) {
    return wrappedList.removeAll(c);
  }

  @Override
  public boolean retainAll(
      Collection<?> c) {
    return wrappedList.retainAll(c);
  }

  @Override
  public void clear() {
    wrappedList.clear();
  }

  @Override
  public boolean equals(Object o) {
    return wrappedList.equals(o);
  }

  @Override
  public int hashCode() {
    return wrappedList.hashCode();
  }

  @Override
  public T get(int index) {
    return wrappedList.get(index);
  }

  @Override
  public T set(int index, T element) {
    return wrappedList.set(index, element);
  }

  @Override
  public void add(int index, T element) {
    if (wrappedList.size() + 1 > maxSize()) {
      throw new IllegalArgumentException(
          "No more than maxsize items can be added to this pagination");
    }
    wrappedList.add(index, element);
  }

  @Override
  public T remove(int index) {
    return wrappedList.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return wrappedList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return wrappedList.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return new PaginationListIterator<T>(this, wrappedList.listIterator());
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return new PaginationListIterator<T>(this, wrappedList.listIterator(index));
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return wrappedList.subList(fromIndex, toIndex);
  }

  private static class PaginationListIterator<T> implements ListIterator<T> {

    private final ListIterator<T> iterator;
    private final PaginationList<T> list;

    public PaginationListIterator(PaginationList<T> list, ListIterator<T> iterator) {
      this.iterator = iterator;
      this.list = list;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      return iterator.next();
    }

    @Override
    public boolean hasPrevious() {
      return iterator.hasPrevious();
    }

    @Override
    public T previous() {
      return iterator.previous();
    }

    @Override
    public int nextIndex() {
      return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
      return iterator.previousIndex();
    }

    @Override
    public void remove() {
      iterator.remove();
    }

    @Override
    public void set(T e) {
      iterator.set(e);
    }

    @Override
    public void add(T e) {
      if (list.maxSize() + 1 > 0) {
        throw new IllegalArgumentException(
            "No more than maxsize items can be added to this pagination");
      }
      iterator.add(e);
    }


  }

}
