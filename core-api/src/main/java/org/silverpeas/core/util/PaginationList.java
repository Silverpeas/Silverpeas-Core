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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * The pagination list is a decorator of a collection by adding it some methods used in the
 * pagination. The pagination list defines a given page to a full collection of items.
 * @author mmoquillon
 * @param <T> the type of the items in the list.
 */
public class PaginationList<T> extends SilverpeasListWrapper<T> {

  private static final IllegalArgumentException MORE_THAN_MAXSIZE_EXCEPTION =
      new IllegalArgumentException("No more than maxsize items can be added to this pagination");
  private final long maxsize;

  private PaginationList(final List<T> aList) {
    this(aList, aList.size());
  }

  private PaginationList(final List<T> aList, long maxItems) {
    super(aList);
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
  public static <T> PaginationList<T> from(final Collection<T> aCollection) {
    return new PaginationList<>(new ArrayList<>(aCollection));
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
  public static <T> PaginationList<T> from(final Collection<T> aCollection, long maxItems) {
    return new PaginationList<>(new ArrayList<>(aCollection), maxItems);
  }

  @Override
  public <U> SilverpeasList<U> newEmptyListWithSameProperties() {
    return new PaginationList<>(new ArrayList<>(size()), maxsize);
  }

  @Override
  public long originalListSize() {
    return maxsize;
  }

  @Override
  public boolean add(T e) {
    if (this.size() + 1 > originalListSize()) {
      throw MORE_THAN_MAXSIZE_EXCEPTION;
    }
    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    if (this.size() + c.size() > originalListSize()) {
      throw MORE_THAN_MAXSIZE_EXCEPTION;
    }
    return super.addAll(c);
  }

  @Override
  public boolean addAll(int index,
      Collection<? extends T> c) {
    if (this.size() + c.size() > originalListSize()) {
      throw MORE_THAN_MAXSIZE_EXCEPTION;
    }
    return super.addAll(index, c);
  }

  @Override
  public void add(int index, T element) {
    if (this.size() + 1 > originalListSize()) {
      throw MORE_THAN_MAXSIZE_EXCEPTION;
    }
    super.add(index, element);
  }

  @Override
  public ListIterator<T> listIterator() {
    return new PaginationListIterator<>(this, super.listIterator());
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return new PaginationListIterator<>(this, super.listIterator(index));
  }

  private static class PaginationListIterator<T> implements ListIterator<T> {

    private final ListIterator<T> iterator;
    private final PaginationList<T> list;

    PaginationListIterator(PaginationList<T> list, ListIterator<T> iterator) {
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
      if (list.originalListSize() + 1 > 0) {
        throw MORE_THAN_MAXSIZE_EXCEPTION;
      }
      iterator.add(e);
    }


  }

}
