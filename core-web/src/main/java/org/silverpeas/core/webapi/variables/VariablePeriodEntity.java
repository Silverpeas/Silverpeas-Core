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
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.variables.VariablePeriod;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariablePeriodEntity implements WebEntity {

  private URI uri;

  private String id;
  private String value;
  private String beginDate;
  private String endDate;

  public static VariablePeriodEntity fromVariablePeriod(VariablePeriod period) {
    return new VariablePeriodEntity().decorate(period);
  }

  public VariablePeriod toVariablePeriod() {
    return new VariablePeriod(value, getPeriod());
  }

  protected VariablePeriodEntity decorate(final VariablePeriod vPeriod) {
    this.id = vPeriod.getId();
    this.value = vPeriod.getValue();
    Period period = vPeriod.getPeriod();
    if (period.isBeginDefined()) {
      this.beginDate = DateUtil.formatAsISO8601Day(period.getBeginDate());
    }
    if (period.isEndDefined()) {
      this.endDate = DateUtil.formatAsISO8601Day(period.getEndDate());
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
    Date bDate = getDate(beginDate);
    Date eDate = getDate(endDate);
    return Period.getPeriodWithUndefinedIfNull(bDate, eDate);
  }

  private Date getDate(String iso8601Date) {
    Date date = null;
    if (StringUtil.isDefined(iso8601Date)) {
      try {
        date = DateUtil.parseISO8601Date(iso8601Date);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return date;
  }

}