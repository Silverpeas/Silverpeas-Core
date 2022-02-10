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
package org.silverpeas.core.webapi.variables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.variables.VariableScheduledValue;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.Temporal;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;
import static org.silverpeas.core.date.TemporalFormatter.toTemporal;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableScheduledValueEntity implements WebEntity {

  private URI uri;

  private String id;
  private String value;
  private String beginDate;
  private String endDate;

  public static VariableScheduledValueEntity fromVariableScheduledValue(
      VariableScheduledValue value) {
    return new VariableScheduledValueEntity().decorate(value);
  }

  public VariableScheduledValue toVariableScheduledValue() {
    return new VariableScheduledValue(value, getPeriod());
  }

  protected VariableScheduledValueEntity decorate(final VariableScheduledValue vPeriod) {
    this.id = vPeriod.getId();
    this.value = vPeriod.getValue();
    Period period = vPeriod.getPeriod();
    if (!period.startsAtMinDate()) {
      this.beginDate = asLocalDate(period.getStartDate()).toString();
    }
    if (!period.endsAtMaxDate()) {
      this.endDate = asLocalDate(period.getEndDate()).toString();
    }
    return this;
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

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  private Period getPeriod() {
    LocalDate start = getDate(beginDate);
    LocalDate end = getDate(endDate);
    return Period.betweenNullable(start, end);
  }

  private LocalDate getDate(String iso8601Date) {
    Temporal date = toTemporal(iso8601Date, false);
    return date == null ? null : asLocalDate(date);
  }

}