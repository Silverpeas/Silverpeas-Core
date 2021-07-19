package org.silverpeas.web.variables;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariableScheduledValue;
import org.silverpeas.core.variables.VariableValueSet;
import org.silverpeas.core.web.util.SelectableUIEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.silverpeas.core.date.TemporalConverter.asDate;

public class VariableUIEntity extends SelectableUIEntity<Variable> {

  private VariableScheduledValue refValue;

  private VariableUIEntity(final Variable data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  private final Supplier<VariableScheduledValue> defaultValue =
      () -> new VariableScheduledValue("", Period.between(LocalDate.MIN, LocalDate.MAX));

  @Override
  public String getId() {
    return String.valueOf(getData().getId());
  }

  public static SilverpeasList<VariableUIEntity> convertList(
      final SilverpeasList<Variable> values, final Set<String> selectedIds) {
    final Function<Variable, VariableUIEntity> converter =
        c -> new VariableUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }

  /**
   * Gets the value in the underlying variable to use as reference for comparison operation.
   * @return a variable's value.
   */
  public VariableScheduledValue getRefVariableValue() {
    if (refValue == null) {
      VariableValueSet valueSet = getData().getVariableValues();
      Supplier<Optional<VariableScheduledValue>> current = valueSet::getCurrent;
      refValue = Stream.of(current, valueSet::getNext, valueSet::getPrevious)
          .map(Supplier::get)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst()
          .orElseGet(defaultValue);
    }
    return refValue;
  }

  /**
   * Gets the start date of the reference value's period.
   * @return the date at which the reference value starts to be valid (to be used).
   */
  public Date getRefStartDate() {
    return asDate(
        getRefVariableValue().getPeriod().getStartDate());
  }

  /**
   * Gets the end date of the reference value's period.
   * @return the date at which the reference value ends to be valid (to be used).
   */
  public Date getRefEndDate() {
    return asDate(
        getRefVariableValue().getPeriod().getEndDate());
  }

}