/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;

import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 10/09/13
 */
public enum TermsOfServiceAcceptanceFrequency {
  NEVER(null),
  ONE(null),
  DAILY(PeriodType.day),
  WEEKLY(PeriodType.week),
  MONTHLY(PeriodType.month),
  YEARLY(PeriodType.year),
  ALWAYS(null);

  private PeriodType periodeType;

  private TermsOfServiceAcceptanceFrequency(PeriodType periodeType) {
    this.periodeType = periodeType;
  }

  /**
   * Indicates if terms of service frequency is activated.
   * @return
   */
  public boolean isActivated() {
    return this != NEVER;
  }

  /**
   * Indicates if the given acceptance date of terms of service is valid compared to the current
   * date.
   * @param tosAcceptanceDate
   * @param locale
   * @return
   */
  public boolean isAcceptanceDateExpired(Date tosAcceptanceDate, String locale) {
    return isAcceptanceDateExpired(DateUtil.getNow(), tosAcceptanceDate, locale);
  }

  /**
   * Indicates if the given acceptance date of terms of service is valid compared to the reference
   * date.
   * @param referenceDate
   * @param tosAcceptanceDate
   * @param locale
   * @return
   */
  protected boolean isAcceptanceDateExpired(Date referenceDate, Date tosAcceptanceDate,
      String locale) {
    switch (this) {
      case NEVER:
        return false;
      case ALWAYS:
        return true;
      case ONE:
        if (tosAcceptanceDate != null) {
          return false;
        }
        break;
      default:
        if (tosAcceptanceDate != null) {
          Period validPeriod = Period.from(referenceDate, periodeType, locale);
          return !validPeriod.contains(tosAcceptanceDate);
        }
    }
    return true;
  }

  /**
   * Decode from a string value.
   * NEVER by default (even the given value is unknown).
   * @param tosAcceptanceFrequency
   * @return
   */
  public static TermsOfServiceAcceptanceFrequency decode(final String tosAcceptanceFrequency) {
    if (StringUtil.isDefined(tosAcceptanceFrequency)) {
      for (TermsOfServiceAcceptanceFrequency current : TermsOfServiceAcceptanceFrequency.values()) {
        if (current.name().equalsIgnoreCase(tosAcceptanceFrequency)) {
          return current;
        }
      }
    }
    return NEVER;
  }
}
