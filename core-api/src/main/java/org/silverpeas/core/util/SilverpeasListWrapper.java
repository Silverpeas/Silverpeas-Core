/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A wrapper of {@link List} instances.
 * @author Yohann Chastagnier
 */
public class SilverpeasListWrapper<E> implements SilverpeasList<E> {

  private final List<E> wrappedList;

  SilverpeasListWrapper(final List<E> wrappedList) {
    this.wrappedList = wrappedList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <U> SilverpeasList<U> newEmptyListWithSameProperties() {
    return new SilverpeasListWrapper(new ArrayList<>());
  }

  @Override
  public long originalListSize() {
    return wrappedList instanceof SilverpeasList ?
        ((SilverpeasList) wrappedList).originalListSize() :
        size();
  }

  public boolean containsAll(Collection<?> c) {
    return wrappedList.containsAll(c);
  }

  public boolean equals(Object o) {
    return wrappedList.equals(o);
  }

  public int hashCode() {
    return wrappedList.hashCode();
  }

  public int size() {
    return wrappedList.size();
  }

  public boolean isEmpty() {
    return wrappedList.isEmpty();
  }

  public boolean contains(Object o) {
    return wrappedList.contains(o);
  }

  public int indexOf(Object o) {
    return wrappedList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return wrappedList.lastIndexOf(o);
  }

  public Object[] toArray() {
    return wrappedList.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return wrappedList.toArray(a);
  }

  public E get(int index) {
    return wrappedList.get(index);
  }

  public E set(int index, E element) {
    return wrappedList.set(index, element);
  }

  public boolean add(E e) {
    return wrappedList.add(e);
  }

  public void add(int index, E element) {
    wrappedList.add(index, element);
  }

  public E remove(int index) {
    return wrappedList.remove(index);
  }

  public boolean remove(Object o) {
    return wrappedList.remove(o);
  }

  public void clear() {
    wrappedList.clear();
  }

  public boolean addAll(Collection<? extends E> c) {
    return wrappedList.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    return wrappedList.addAll(index, c);
  }

  public boolean removeAll(Collection<?> c) {
    return wrappedList.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return wrappedList.retainAll(c);
  }

  public ListIterator<E> listIterator(int index) {
    return wrappedList.listIterator(index);
  }

  public ListIterator<E> listIterator() {
    return wrappedList.listIterator();
  }

  public Iterator<E> iterator() {
    return wrappedList.iterator();
  }

  public List<E> subList(int fromIndex, int toIndex) {
    return wrappedList.subList(fromIndex, toIndex);
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    wrappedList.forEach(action);
  }

  @Override
  public Spliterator<E> spliterator() {
    return wrappedList.spliterator();
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    return wrappedList.removeIf(filter);
  }

  @Override
  public void replaceAll(UnaryOperator<E> operator) {
    wrappedList.replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super E> c) {
    wrappedList.sort(c);
  }
}
