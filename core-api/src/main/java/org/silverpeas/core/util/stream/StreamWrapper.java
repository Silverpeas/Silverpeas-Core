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

package org.silverpeas.core.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author silveryocha
 */
public abstract class StreamWrapper<T> implements Stream<T> {

  private final Stream<T> stream;

  public StreamWrapper(final Stream<T> stream) {
    this.stream = stream;
  }

  public static <T1> Builder<T1> builder() {
    return Stream.builder();
  }

  public static <T1> Stream<T1> empty() {
    return Stream.empty();
  }

  public static <T1> Stream<T1> of(final T1 t1) {
    return Stream.of(t1);
  }

  @SafeVarargs
  public static <T1> Stream<T1> of(final T1... values) {
    return Stream.of(values);
  }

  public static <T1> Stream<T1> iterate(final T1 seed, final UnaryOperator<T1> f) {
    return Stream.iterate(seed, f);
  }

  public static <T1> Stream<T1> generate(final Supplier<T1> s) {
    return Stream.generate(s);
  }

  public static <T1> Stream<T1> concat(final Stream<? extends T1> a, final Stream<? extends T1> b) {
    return Stream.concat(a, b);
  }

  protected Stream<T> stream() {
    return stream;
  }

  @Override
  public Stream<T> filter(final Predicate<? super T> predicate) {
    return stream.filter(predicate);
  }

  @Override
  public <R> Stream<R> map(final Function<? super T, ? extends R> mapper) {
    return stream.map(mapper);
  }

  @Override
  public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
    return stream.mapToInt(mapper);
  }

  @Override
  public LongStream mapToLong(final ToLongFunction<? super T> mapper) {
    return stream.mapToLong(mapper);
  }

  @Override
  public DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
    return stream.mapToDouble(mapper);
  }

  @Override
  public <R> Stream<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> mapper) {
    return stream.flatMap(mapper);
  }

  @Override
  public IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
    return stream.flatMapToInt(mapper);
  }

  @Override
  public LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
    return stream.flatMapToLong(mapper);
  }

  @Override
  public DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
    return stream.flatMapToDouble(mapper);
  }

  @Override
  public Stream<T> distinct() {
    return stream.distinct();
  }

  @Override
  public Stream<T> sorted() {
    return stream.sorted();
  }

  @Override
  public Stream<T> sorted(final Comparator<? super T> comparator) {
    return stream.sorted(comparator);
  }

  @Override
  public Stream<T> peek(final Consumer<? super T> action) {
    return stream.peek(action);
  }

  @Override
  public Stream<T> limit(final long maxSize) {
    return stream.limit(maxSize);
  }

  @Override
  public Stream<T> skip(final long n) {
    return stream.skip(n);
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    stream.forEach(action);
  }

  @Override
  public void forEachOrdered(final Consumer<? super T> action) {
    stream.forEachOrdered(action);
  }

  @Override
  public Object[] toArray() {
    return stream.toArray();
  }

  @Override
  public <A> A[] toArray(final IntFunction<A[]> generator) {
    return stream.toArray(generator);
  }

  @Override
  public T reduce(final T identity, final BinaryOperator<T> accumulator) {
    return stream.reduce(identity, accumulator);
  }

  @Override
  public Optional<T> reduce(final BinaryOperator<T> accumulator) {
    return stream.reduce(accumulator);
  }

  @Override
  public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator,
      final BinaryOperator<U> combiner) {
    return stream.reduce(identity, accumulator, combiner);
  }

  @Override
  public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator,
      final BiConsumer<R, R> combiner) {
    return stream.collect(supplier, accumulator, combiner);
  }

  @Override
  public <R, A> R collect(final Collector<? super T, A, R> collector) {
    return stream.collect(collector);
  }

  @Override
  public Optional<T> min(final Comparator<? super T> comparator) {
    return stream.min(comparator);
  }

  @Override
  public Optional<T> max(final Comparator<? super T> comparator) {
    return stream.max(comparator);
  }

  @Override
  public long count() {
    return stream.count();
  }

  @Override
  public boolean anyMatch(final Predicate<? super T> predicate) {
    return stream.anyMatch(predicate);
  }

  @Override
  public boolean allMatch(final Predicate<? super T> predicate) {
    return stream.allMatch(predicate);
  }

  @Override
  public boolean noneMatch(final Predicate<? super T> predicate) {
    return stream.noneMatch(predicate);
  }

  @Override
  public Optional<T> findFirst() {
    return stream.findFirst();
  }

  @Override
  public Optional<T> findAny() {
    return stream.findAny();
  }

  @Override
  public Iterator<T> iterator() {
    return stream.iterator();
  }

  @Override
  public Spliterator<T> spliterator() {
    return stream.spliterator();
  }

  @Override
  public boolean isParallel() {
    return stream.isParallel();
  }

  @Override
  public Stream<T> sequential() {
    return stream.sequential();
  }

  @Override
  public Stream<T> parallel() {
    return stream.parallel();
  }

  @Override
  public Stream<T> unordered() {
    return stream.unordered();
  }

  @Override
  public Stream<T> onClose(final Runnable closeHandler) {
    return stream.onClose(closeHandler);
  }

  @Override
  public void close() {
    stream.close();
  }
}
