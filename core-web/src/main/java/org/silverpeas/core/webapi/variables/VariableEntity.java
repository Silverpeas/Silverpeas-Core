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
package org.silverpeas.core.webapi.variables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariablePeriod;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableEntity implements WebEntity {

  private URI uri;

  private String id;
  private String label;
  private String description;
  private String value;
  private List<VariablePeriodEntity> periods;

  protected VariableEntity() {
  }

  public static VariableEntity fromVariable(Variable variable) {
    VariableEntity entity = new VariableEntity().decorate(variable);
    List<VariablePeriodEntity> periods = new ArrayList<>();
    for (VariablePeriod period : variable.getPeriods()) {
      periods.add(VariablePeriodEntity.fromVariablePeriod(period));
    }
    entity.setValue(variable.getCurrentPeriod().getValue());
    entity.setPeriods(periods);
    return entity;
  }

  public Variable toVariable() {
    Variable variable = new Variable(label, description);
    variable.getPeriods().addAll(toPeriods());
    return variable;
  }

  public List<VariablePeriod> toPeriods() {
    List<VariablePeriod> result = new ArrayList<>();
    for (VariablePeriodEntity entity : this.periods) {
      result.add(entity.toVariablePeriod());
    }
    return result;
  }

  protected VariableEntity decorate(final Variable value) {
    this.id = value.getId();
    this.label = value.getLabel();
    this.description = value.getDescription();
    return this;
  }

  protected void merge(final Variable value) {
    value.setLabel(getLabel());
    value.setDescription(getDescription());
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public List<VariablePeriodEntity> getPeriods() {
    return periods;
  }

  public void setPeriods(List<VariablePeriodEntity> periods) {
    this.periods = periods;
  }

}