/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sb_variables_variable")
@NamedQueries({
    @NamedQuery(name = "allVariables", query = "select v from Variable v order by v.label ASC")})
public class Variable extends SilverpeasJpaEntity<Variable, UuidIdentifier> implements
    Securable {

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private String description;

  @OneToMany(mappedBy = "variable", fetch = FetchType.EAGER, cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<VariablePeriod> periods = new ArrayList<>();

  protected Variable() {
    // default constructor for the persistence engine
  }

  public Variable(String label, String description) {
    this.label = label;
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public VariablePeriod getCurrentPeriod() {
    for (VariablePeriod period : periods) {
      if (period.getPeriod().contains(new Date())) {
        return period;
      }
    }
    if (periods.size() == 1) {
      return periods.get(0);
    }
    return null;
  }

  public List<VariablePeriod> getPeriods() {
    return periods;
  }

  public int getNumberOfPeriods() {
    return periods.size();
  }

  public void merge(Variable variable) {
    setLabel(variable.getLabel());
    setDescription(variable.getDescription());
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
}