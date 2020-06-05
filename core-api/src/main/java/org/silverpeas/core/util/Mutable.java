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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>A mutable value wrapper which may or may not contain a non-null value.
 * If a value is present, {@code isPresent()} will return {@code true} and
 * {@code get()} will return the value.
 * <p>Additional methods that depend on the presence or absence of a contained
 * value are provided, such as {@link #orElse(java.lang.Object) orElse()}
 * (return a default value if value not present) and
 * {@link #ifPresent(java.util.function.Consumer) ifPresent()} (execute a block
 * of code if the value is present).
 * @author Yohann Chastagnier
 */
public final class Mutable<T> {

  private T value;

  /**
   * Constructs an instance with null value.
   */
  private Mutable() {
    this.value =  null;
  }

  /**
   * Constructs an instance with the given value.
   * @param value the value (which can be null)
   * @throws NullPointerException if value is null
   */
  private Mutable(T value) {
    Objects.requireNonNull(value);
    this.value = value;
  }

  /**
   * Returns an {@code Mutable} with the specified value.
   * @param <T> the class of the value
   * @param value the value to be present, which must be non-null
   * @return an {@code Mutable} with the value present
   * @throws NullPointerException if value is null
   */
  public static <T> Mutable<T> of(T value) {
    return new Mutable<>(value);
  }

  /**
   * Returns an {@code Mutable} with the specified value which can be null.
   * @param <T> the class of the value
   * @param value the value to be present, which must be null
   * @return an {@code Mutable} with the value present
   */
  public static <T> Mutable<T> ofNullable(T value) {
    return value == null ? new Mutable<>():new Mutable<>(value);
  }

  /**
   * Returns an empty {@code Mutable}. Its value is by default null.
   * @param <T> class of the value.
   * @return a {@code Mutable} with a null value.
   */
  public static <T> Mutable<T> empty() {
    return new Mutable<>();
  }

  /**
   * If a value is present in this {@code Mutable}, returns the value,
   * otherwise throws {@code NoSuchElementException}.
   * @return the non-null value held by this {@code Mutable}
   * @throws NoSuchElementException if there is no value present
   * @see Mutable#isPresent()
   */
  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /**
   * Sets the value of the {@code Mutable} instance.
   * @param value the value to set.
   */
  public void set(final T value) {
    this.value = value;
  }

  /**
   * Indicates id the given value is equal to the one of the {@code Mutable} instance.
   * @param value the value to verify.
   * @return true if the value is equal to the wrapped one in this mutable. False otherwise.
   */
  public boolean is(final T value) {
    return Objects.equals(this.value, value);
  }

  /**
   * Return {@code true} if there is a value present, otherwise {@code false}.
   * @return {@code true} if there is a value present, otherwise {@code false}
   */
  public boolean isPresent() {
    return value != null;
  }

  /**
   * If a value is present, invoke the specified consumer with the value,
   * otherwise do nothing.
   * @param consumer block to be executed if a value is present
   * @throws NullPointerException if value is present and {@code consumer} is
   * null
   */
  public void ifPresent(Consumer<? super T> consumer) {
    if (value != null) {
      consumer.accept(value);
    }
  }

  /**
   * If a value is present, and the value matches the given predicate,
   * return an {@code Mutable} describing the value, otherwise return an
   * empty {@code Mutable}.
   * @param predicate a predicate to apply to the value, if present
   * @return an {@code Mutable} describing the value of this {@code Mutable}
   * if a value is present and the value matches the given predicate,
   * otherwise an empty {@code Mutable}
   * @throws NullPointerException if the predicate is null
   */
  public Mutable<T> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    if (!isPresent()) {
      return this;
    } else {
      return predicate.test(value) ? this : new Mutable<>();
    }
  }

  /**
   * If a value is present, apply the provided mapping function to it,
   * and if the result is non-null, return an {@code Mutable} describing the
   * result.  Otherwise return an empty {@code Mutable}.
   * @param <U> The type of the result of the mapping function
   * @param mapper a mapping function to apply to the value, if present
   * @return an {@code Mutable} describing the result of applying a mapping
   * function to the value of this {@code Mutable}, if a value is present,
   * otherwise an empty {@code Mutable}
   * @throws NullPointerException if the mapping function is null
   * @apiNote This method supports post-processing on mutable values, without
   * the need to explicitly check for a return status.  For example, the
   * following code traverses a stream of file names, selects one that has
   * not yet been processed, and then opens that file, returning an
   * {@code Mutable<FileInputStream>}:
   * <br>
   * <pre>{@code
   *     Mutable<FileInputStream> fis =
   *         names.stream().filter(name -> !isProcessedYet(name))
   *                       .findFirst()
   *                       .map(name -> new FileInputStream(name));
   * }</pre>
   * <p>
   * Here, {@code findFirst} returns an {@code Mutable<String>}, and then
   * {@code map} returns an {@code Mutable<FileInputStream>} for the desired
   * file if one exists.
   */
  public <U> Mutable<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return new Mutable<>();
    } else {
      return Mutable.ofNullable(mapper.apply(value));
    }
  }

  /**
   * If a value is present, apply the provided {@code Mutable}-bearing
   * mapping function to it, return that result, otherwise return an empty
   * {@code Mutable}.  This method is similar to {@link #map(Function)},
   * but the provided mapper is one whose result is already an {@code Mutable},
   * and if invoked, {@code flatMap} does not wrap it with an additional
   * {@code Mutable}.
   * @param <U> The type parameter to the {@code Mutable} returned by
   * @param mapper a mapping function to apply to the value, if present
   * the mapping function
   * @return the result of applying an {@code Mutable}-bearing mapping
   * function to the value of this {@code Mutable}, if a value is present,
   * otherwise an empty {@code Mutable}
   * @throws NullPointerException if the mapping function is null or returns
   * a null result
   */
  public <U> Mutable<U> flatMap(Function<? super T, Mutable<U>> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return new Mutable<>();
    } else {
      return Objects.requireNonNull(mapper.apply(value));
    }
  }

  /**
   * Return the value if present, otherwise return {@code other}.
   * @param other the value to be returned if there is no value present, may
   * be null
   * @return the value, if present, otherwise {@code other}
   */
  public T orElse(T other) {
    return value != null ? value : other;
  }

  /**
   * Return the value if present, otherwise invoke {@code other} and return
   * the result of that invocation.
   * @param other a {@code Supplier} whose result is returned if no value
   * is present
   * @return the value if present otherwise the result of {@code other.get()}
   * @throws NullPointerException if value is not present and {@code other} is
   * null
   */
  public T orElseGet(Supplier<? extends T> other) {
    return value != null ? value : other.get();
  }

  /**
   * Return the contained value, if present, otherwise throw an exception
   * to be created by the provided supplier.
   * @param <X> Type of the exception to be thrown
   * @param exceptionSupplier The supplier which will return the exception to
   * be thrown
   * @return the present value
   * @throws X if there is no value present
   * @throws NullPointerException if no value is present and
   * {@code exceptionSupplier} is null
   * @apiNote A method reference to the exception constructor with an empty
   * argument list can be used as the supplier. For example,
   * {@code IllegalStateException::new}
   */
  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Indicates whether some other object is "equal to" this Mutable. The
   * other object is considered equal if:
   * <ul>
   * <li>it is also an {@code Mutable} and;
   * <li>both instances have no value present or;
   * <li>the present values are "equal to" each other via {@code equals()}.
   * </ul>
   * @param obj an object to be tested for equality
   * @return {code true} if the other object is "equal to" this object
   * otherwise {@code false}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Mutable)) {
      return false;
    }

    Mutable<?> other = (Mutable<?>) obj;
    return Objects.equals(value, other.value);
  }

  /**
   * Returns the hash code value of the present value, if any, or 0 (zero) if
   * no value is present.
   * @return hash code value of the present value or 0 if no value is present
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  /**
   * Returns a non-empty string representation of this Mutable suitable for
   * debugging. The exact presentation format is unspecified and may vary
   * between implementations and versions.
   * @return the string representation of this instance
   * @implSpec If a value is present the result must include its string
   * representation in the result. Empty and present Mutables must be
   * unambiguously differentiable.
   */
  @Override
  public String toString() {
    return value != null ? String.format("Mutable[%s]", value) : "Mutable.nullValue";
  }
}