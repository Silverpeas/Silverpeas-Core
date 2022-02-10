/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.variables;

import org.silverpeas.core.persistence.Transaction;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A set of variable's values. The set is related to a given variable and each value is defined for
 * a given period in time.
 * @author mmoquillon
 */
public class VariableValueSet implements Collection<VariableScheduledValue> {

  final Variable variable;

  VariableValueSet(final Variable variable) {
    this.variable = variable;
  }

  /**
   * Adds the specified variable value into this variable's values set. In order the adding to
   * be effective you have to invoke {@link Variable#save()}.
   * @param value the value, defined in a given period, to add in this set.
   */
  public boolean add(final VariableScheduledValue value) {
    value.setVariable(variable);
    return variable.values().add(value);
  }

  @Override
  public boolean remove(final Object o) {
    return false;
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return false;
  }

  /**
   * Adds the specified variable values into this variable's values set. In order the adding to
   * be effective you have to invoke {@link Variable#save()}.
   * @param values a collection of values, each of then defined in a given period, to add in this
   * set.
   */
  public boolean addAll(final Collection<? extends VariableScheduledValue> values) {
    return variable.values()
        .addAll(values.stream().map(v -> {
          v.setVariable(variable);
          return v;
        }).collect(Collectors.toList()));
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return variable.values().removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return variable.values().retainAll(c);
  }

  @Override
  public void clear() {
    variable.values().clear();
  }

  /**
   * Adds the specified variable value into the variable's values set and saves automatically the
   * set. This method can be invoked only with an already persisted variable otherwise a
   * {@link javax.persistence.PersistenceException} will be thrown. Useful when just to add a new
   * value to the underlying variable for a given period.
   * @param value the value, defined in a given period, to add in this set.
   * @return the persisted variable value.
   */
  public VariableScheduledValue addAndSave(final VariableScheduledValue value) {
    return Transaction.performInOne(() -> {
      value.setVariable(variable);
      return VariableScheduledValueRepository.get().save(value);
    });
  }

  /**
   * Gets a stream on all of the values of the underlying variable.
   * @return a stream of variable values.
   */
  @Override
  public Stream<VariableScheduledValue> stream() {
    return variable.values().stream();
  }

  /**
   * Gets from this variable's value set the value with the specified identifier.
   * @param id the unique identifier of the value to get.
   * @return an optional value. If no such value exists among the variable's values, then an empty
   * optional value is returned.
   */
  public Optional<VariableScheduledValue> get(final String id) {
    return variable.values().stream().filter(v -> v.getId().equals(id)).findFirst();
  }

  /**
   * Removes the specified variables' value from this set. In order the removing to be effective,
   * you have to invoke {@link Variable#save()}.
   * @param id the unique identifier of the variable's value to remove.
   * @return true if the value with the specified identifier is removed, false otherwise. False is
   * returned if no such value is defined for the underlying variable.
   */
  public boolean remove(final String id) {
    return variable.values().removeIf(v -> v.getId().equals(id));
  }

  /**
   * Gets the value among the variable's value set that is valid at the current date time.
   * @return an optional variable value. If the underlying variable has no value or none that is
   * valid at now, then an empty optional value is returned.
   */
  public Optional<VariableScheduledValue> getCurrent() {
    LocalDate today = LocalDate.now();
    return variable.values().stream().filter(v -> v.getPeriod().includes(today)).findFirst();
  }

  /**
   * Gets the value among the variable's value set that will be next scheduled.
   * @return an optional variable value. If the underlying variable has no value or none that will
   * be scheduled in the future, then an empty optional value is returned.
   */
  public Optional<VariableScheduledValue> getNext() {
    LocalDate today = LocalDate.now();
    return variable.values().stream().filter(v -> v.getPeriod().startsAfter(today)).findFirst();
  }

  /**
   * Gets the value among the variable's value set that was previously scheduled.
   * @return an optional variable value. If the underlying variable has no value or none that was
   * previously scheduled, then an empty optional value is returned.
   */
  public Optional<VariableScheduledValue> getPrevious() {
    LocalDate today = LocalDate.now();
    return variable.values().stream().filter(v -> v.getPeriod().endsBefore(today)).findFirst();
  }

  /**
   * Gets the size in values of this variable's values set.
   * @return the count of values of the underlying variable.
   */
  public int size() {
    return variable.values().size();
  }

  /**
   * Is this variable's value set empty?
   * @return true if the underlying variable has no defined values. False otherwise.
   */
  public boolean isEmpty() {
    return variable.values().isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    return variable.values().contains(o);
  }

  @Override
  public Iterator<VariableScheduledValue> iterator() {
    return variable.values().iterator();
  }

  @Override
  public Object[] toArray() {
    return variable.values().toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return variable.values().toArray(a);
  }

  @Override
  public void forEach(final Consumer<? super VariableScheduledValue> action) {
    variable.values().forEach(action);
  }

  @Override
  public Spliterator<VariableScheduledValue> spliterator() {
    return variable.values().spliterator();
  }
}
  