/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.date.period;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.util.DateUtil;

import java.util.Date;
import java.util.TimeZone;

/**
 * An immutable UNDIFINED period.
 */
class UndefinedPeriod extends Period {
  private static final long serialVersionUID = 2619196735517207878L;

  /**
   * Constructor : Constructs a newly allocated <code>period</code>.
   */
  protected UndefinedPeriod() {
    super(new DateTime(DateUtil.MINIMUM_DATE), new DateTime(DateUtil.MAXIMUM_DATE));
    setPeriodType(PeriodType.unknown);
  }

  @Override
  public void setDate(final Date dateReference, final PeriodType periodType) {
    // Nothing is done (as the class is an immutable one)
  }

  @Override
  public void setDate(final Date dateReference, final TimeZone timeZone,
      final PeriodType periodType) {
    // Nothing is done (as the class is an immutable one)
  }

  @Override
  public void setDate(final DateTime referenceDatable, final PeriodType periodType) {
    // Nothing is done (as the class is an immutable one)
  }

  @Override
  public void setDates(final Date beginDate, final Date endDate) {
    // Nothing is done (as the class is an immutable one)
  }

  @Override
  public void setDates(final Date beginDate, final Date endDate, final TimeZone timeZone) {
    // Nothing is done (as the class is an immutable one)
  }

  @Override
  public void setDates(final DateTime beginDatable, final DateTime endDatable) {
    // Nothing is done (as the class is an immutable one)
  }

  @SuppressWarnings("CloneDoesntCallSuperClone")
  @Override
  public Period clone() {
    // the class is an immutable one
    return this;
  }

  @Override
  protected void setPeriodType(final PeriodType periodType) {
    // Nothing is done (as the class is an immutable one)
  }
}
