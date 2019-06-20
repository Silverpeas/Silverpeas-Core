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

  @Override
  public boolean containsAll(Collection<?> c) {
    return wrappedList.containsAll(c);
  }

  public boolean equals(Object o) {
    return wrappedList.equals(o);
  }

  public int hashCode() {
    return wrappedList.hashCode();
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
  public int indexOf(Object o) {
    return wrappedList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return wrappedList.lastIndexOf(o);
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
  public E get(int index) {
    return wrappedList.get(index);
  }

  @Override
  public E set(int index, E element) {
    return wrappedList.set(index, element);
  }

  @Override
  public boolean add(E e) {
    return wrappedList.add(e);
  }

  @Override
  public void add(int index, E element) {
    wrappedList.add(index, element);
  }

  @Override
  public E remove(int index) {
    return wrappedList.remove(index);
  }

  @Override
  public boolean remove(Object o) {
    return wrappedList.remove(o);
  }

  @Override
  public void clear() {
    wrappedList.clear();
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return wrappedList.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return wrappedList.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return wrappedList.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return wrappedList.retainAll(c);
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return wrappedList.listIterator(index);
  }

  @Override
  public ListIterator<E> listIterator() {
    return wrappedList.listIterator();
  }

  @Override
  public Iterator<E> iterator() {
    return wrappedList.iterator();
  }

  @Override
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
