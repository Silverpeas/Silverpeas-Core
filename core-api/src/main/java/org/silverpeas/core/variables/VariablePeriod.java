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

import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.util.WebEncodeHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "sb_variables_period")
@NamedQueries({
    @NamedQuery(name = "currentPeriods", query = "select p from VariablePeriod p where p" +
        ".startDate <= :today and :today <= p.endDate order by p.variable.label ASC")})
public class VariablePeriod extends SilverpeasJpaEntity<VariablePeriod, UuidIdentifier>
    implements Securable {

  @Column(nullable = false)
  private String value;

  @Column
  private Date startDate;

  @Column
  private Date endDate;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "variableId", referencedColumnName = "id", nullable = false)
  private Variable variable;

  protected VariablePeriod() {
    // default constructor for the persistence engine
  }

  public VariablePeriod(String value, Period period) {
    this.value = value;
    this.startDate = period.getBeginDate();
    this.endDate = period.getEndDate();
  }

  public Period getPeriod() {
    return Period.from(startDate, endDate);
  }

  public String getValue() {
    return value;
  }

  public void setVariable(Variable value) {
    variable = value;
  }

  public Variable getVariable() {
    return variable;
  }

  public String getValueForHTML() {
    return WebEncodeHelper.javaStringToHtmlParagraphe(getValue());
  }

  @Override
  public VariablePeriod setId(String id) {
    super.setId(id);
    return this;
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
