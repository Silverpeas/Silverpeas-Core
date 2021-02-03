/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * It represents objects with two exclusive possibilities: either it is an object of type T or an
 * object of type U.
 * @author mmoquillon
 */
public class Either<T, U> {

  private final U right;
  private final T left;

  /**
   * Constructs an Either as a left-object
   * @param left the object from which to construct an Either.
   * @param <T> the type of the left-object
   * @param <U> the type of the right-object.
   * @return either an object of type T or an object of type U but whose the actual value is of
   * type T.
   */
  public static <T, U> Either<T, U> left(T left) {
    assert left != null;
    return new Either<T, U>(left, null);
  }

  /**
   * Constructs an Either as a right-object
   * @param right the object from which to construct an Either.
   * @param <T> the type of the left-object
   * @param <U> the type of the right-object.
   * @return either an object of type T or an object of type U but whose the actual value is of
   * type U.
   */
  public static <T, U> Either<T, U> right(U right) {
    assert right != null;
    return new Either<T, U>(null, right);
  }

  /**
   * Is an object of type T?
   * @return true if the actual value is a left-object, false otherwise.
   */
  public boolean isLeft() {
    return left != null;
  }

  /**
   * Is an object of type U?
   * @return true if the actual value is a right-object, false otherwise.
   */
  public boolean isRight() {
    return right != null;
  }

  /**
   * If this Either is a left-object, then returns it, otherwise throws {@code
   * NoSuchElementException}.
   * @return the left-object of this Either.
   */
  public T getLeft() {
    if (left == null) {
      throw new NoSuchElementException("No value present");
    }
    return left;
  }

  /**
   * If this Either is a right-object, then returns it, otherwise throws {@code
   * NoSuchElementException}.
   * @return the right-object of this Either.
   */
  public U getRight() {
    if (right == null) {
      throw new NoSuchElementException("No value present");
    }
    return right;
  }

  /**
   * Applies one of the specified consumers according to the nature of this Either: apply leftConsumer
   * if it is a left-object or apply rightConsumer if it is a right-object.
   * @param leftConsumer a consumer of a left-object.
   * @param rightConsumer a consumer of a right-object.
   */
  public void ifLeftOrRight(Consumer<? super T> leftConsumer, Consumer<? super U> rightConsumer) {
    if (isLeft()) {
      leftConsumer.accept(left);
    } else {
      rightConsumer.accept(right);
    }
  }

  /**
   * Applies one of the specified functions according to the nature of this Either: apply
   * leftFunction if it is a left-object or apply the rightFunction if it is a right-object.
   * @param leftFunction a function on a left-object and returning a given value of type R.
   * @param rightFunction a function on a right-object and returning a given value of type R.
   * @param <R> the type of the returning object computed by one of the specified functions.
   * @return the object computed by one of the specified functions according to the actual value
   * of this Either.
   */
  public <R> R apply(Function<? super T, ? extends R> leftFunction,
      Function<? super U, ? extends R> rightFunction) {
    if (isLeft()) {
      return leftFunction.apply(left);
    } else {
      return rightFunction.apply(right);
    }
  }

  private Either(final T left, final U right) {
    this.left = left;
    this.right = right;
  }
}
