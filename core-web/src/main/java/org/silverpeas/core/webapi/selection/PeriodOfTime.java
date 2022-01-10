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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.selection;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TemporalConverter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Period of time serializable for the Web. The date time at which the period is bounded are
 * set by default
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PeriodOfTime implements Serializable {

  private Date startDate;
  private Date endDate;
  private boolean isInDays;

  private PeriodOfTime() {
  }

  public static PeriodOfTime from(final Period period) {
    PeriodOfTime periodOfTime = new PeriodOfTime();
    periodOfTime.isInDays = period.isInDays();
    periodOfTime.startDate = Date.from(TemporalConverter.asInstant(period.getStartDate()));
    periodOfTime.endDate = Date.from(TemporalConverter.asInstant(period.getEndDate()));
    return periodOfTime;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public boolean isInDays() {
    return isInDays;
  }
}
