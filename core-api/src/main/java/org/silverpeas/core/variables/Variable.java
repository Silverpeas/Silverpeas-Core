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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A variable is a mapping between a literal and one or more values, each of them being scheduled
 * in a given period. Its then a place-holder of a value in the time. It is used to be referred
 * by the content of contributions so that the content can be kept to date when the value of the
 * variable change.
 */
@Entity
@Table(name = "sb_variables_variable")
@NamedQuery(name = "allVariables", query = "select v from Variable v order by v.label ASC")
@NamedQuery(name = "currentVariables", query = "select distinct v from Variable v join v.values vv where vv.startDate <= :today and :today <= vv.endDate order by v.label ASC")
public class Variable extends SilverpeasJpaEntity<Variable, UuidIdentifier> implements
    Securable {

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private String description;

  @OneToMany(mappedBy = "variable", fetch = FetchType.EAGER, cascade = CascadeType.ALL,
      orphanRemoval = true)
  private final Set<VariableScheduledValue> values = new HashSet<>();

  protected Variable() {
    // default constructor for the persistence engine
  }

  /**
   * Constructs a new variable with the specified label and short description.
   * @param label the label of this variable.
   * @param description a short description of this variable.
   */
  public Variable(String label, String description) {
    this.label = label;
    this.description = description;
  }

  /**
   * Gets the variable by its unique identifier.
   * @param id the unique identifier of the variable to get.
   * @return the variable or null if no such variable exists with the specified identifier.
   */
  public static Variable getById(final String id) {
    return VariablesRepository.get().getById(id);
  }

  /**
   * Gets all the variables available in the persistence context (data source).
   * @return a list of all of the available defined variables or an empty list if no variables has
   * been yet created.
   */
  public static List<Variable> getAll() {
    return VariablesRepository.get().getAllVariables();
  }

  /**
   * Gets this variable's label.
   * @return the label of this variable.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Gets this variable's description.
   * @return a short description of this variable.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Changes the label of this variable.
   * @param label the new label.
   */
  public void setLabel(final String label) {
    this.label = label;
  }

  /**
   * Changes the description of this variable.
   * @param description the new description of this variable.
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Gets all the values that are scheduled for this variable.
   * @return a set of this variable's scheduled values.
   */
  public VariableValueSet getVariableValues() {
    return new VariableValueSet(this);
  }

  /*
   * Gets this variable's number of values
   * @return the number of values
   */
  public int getNumberOfValues() {
    return values().size();
  }

  /**
   * Merges the attributes of this variable with the ones from the specified variable.
   * Only the title and the description are merged. For the different values, please see the
   * {@link VariableValueSet} instance returned by the {@link #getVariableValues()} method.
   * @param variable the variable with which the attributes of this variable are merged.
   */
  public void merge(Variable variable) {
    setLabel(variable.getLabel());
    setDescription(variable.getDescription());
  }

  /**
   * Saves this variable into the persistence context.
   * @return the variable from the persistence context once saved.
   */
  public Variable save() {
    return Transaction.performInOne(() -> VariablesRepository.get().save(this));
  }

  /**
   * Deletes this variable in the persistence context so it will be lost once the transaction
   * terminated.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      VariablesRepository.get().delete(this);
      return null;
    });
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return true;
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return user.isAccessAdmin();
  }

  @Override
  public boolean canBeDeletedBy(final User user) {
    return user.isAccessAdmin();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  Set<VariableScheduledValue> values() {
    return this.values;
  }
}