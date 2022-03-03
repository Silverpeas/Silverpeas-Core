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
package org.silverpeas.core.variables;

import org.silverpeas.core.annotation.constraint.DateRange;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.util.WebEncodeHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;

/**
 * A scheduled value of a given {@link Variable}. Such a value is said scheduled because it is
 * scheduled to be enabled during a given period of time; during this period, the related variable
 * is then valued with this value. Don't forget that a variable can have then at the same time
 * several possible values. In such a case, only the first obtained one will be valid (the first
 * obtained one isn't necessary the first defined one; in that case we don't ensure any coherent
 * behavior).
 */
@Entity
@Table(name = "sb_variables_value")
@DateRange(start = "startDate", end = "endDate")
public class VariableScheduledValue
    extends SilverpeasJpaEntity<VariableScheduledValue, UuidIdentifier> implements Securable {

  @Column(nullable = false)
  private String value;

  @Column
  private LocalDate startDate;

  @Column
  private LocalDate endDate;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "variableId", referencedColumnName = "id", nullable = false)
  private Variable variable;

  protected VariableScheduledValue() {
    // default constructor for the persistence engine
  }

  /**
   * Constructs a new variable value to schedule at the specified period of time.
   * @param value the value of a variable
   * @param period the period in time during which the value will be active.
   */
  public VariableScheduledValue(String value, Period period) {
    this.value = value;
    this.startDate = asLocalDate(period.getStartDate());
    this.endDate = asLocalDate(period.getEndDate());
  }

  /**
   * Gets all the variable scheduled values that are currently valid. If a variable has several
   * values that are scheduled at the current date, only one of them is returned (the first found
   * one).
   * @return a list with the current scheduled variable values or an empty list if there is no
   * variable values scheduled at the current date.
   */
  public static List<VariableScheduledValue> getCurrentOnes() {
    return VariablesRepository.get()
        .getAllCurrentVariables()
        .stream()
        .map(v -> v.getVariableValues().getCurrent())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  /**
   * Gets the period in time during which this variable's value will be active.
   * @return the period in time. The start date and the end date are expressed in {@link LocalDate}
   */
  public Period getPeriod() {
    return Period.between(startDate, endDate);
  }

  /**
   * Gets the embedded value of this scheduled value.
   * @return the value.
   */
  public String getValue() {
    return value;
  }

  void setVariable(Variable value) {
    variable = value;
  }

  /**
   * Gets the variable this scheduled value belongs to.
   * @return the variable that is valued by this value.
   */
  public Variable getVariable() {
    return variable;
  }

  /**
   * Gets the value formatted in HTML.
   * @return the HTML-formatted value.
   */
  public String getValueForHTML() {
    return WebEncodeHelper.javaStringToHtmlParagraphe(getValue());
  }

  /**
   * Merges this variable's value with the properties of the specified value.
   * @param value a variable value from which the merge has to be done.
   */
  public void merge(final VariableScheduledValue value) {
    this.value = value.getValue();
    this.startDate = asLocalDate(value.getPeriod().getStartDate());
    this.endDate = asLocalDate(value.getPeriod().getEndDate());
  }

  /**
   * Updates both the value and the period in time this value is valid of this variable value from
   * the specified another variable value. Be cautious: the modification is automatically
   * synchronized into the persistence context.
   * @param newValue the new variable value to set.
   */
  public VariableScheduledValue updateFrom(final VariableScheduledValue newValue) {
    return Transaction.performInOne(() -> {
      merge(newValue);
      return VariableScheduledValueRepository.get().save(this);
    });
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
