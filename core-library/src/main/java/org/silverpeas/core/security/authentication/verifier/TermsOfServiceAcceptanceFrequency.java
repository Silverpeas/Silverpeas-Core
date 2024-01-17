/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.StringUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Frequency at which the acceptance of the terms of service have to be checked.
 * @author Yohann Chastagnier Date: 10/09/13
 */
public enum TermsOfServiceAcceptanceFrequency {

  NEVER(null),
  ONE(null),
  DAILY(ChronoUnit.DAYS),
  WEEKLY(ChronoUnit.WEEKS),
  MONTHLY(ChronoUnit.MONTHS),
  YEARLY(ChronoUnit.YEARS),
  ALWAYS(ChronoUnit.FOREVER);

  private final ChronoUnit periodType;

  TermsOfServiceAcceptanceFrequency(ChronoUnit periodType) {
    this.periodType = periodType;
  }

  /**
   * Indicates if terms of service frequency is activated.
   * @return true if the terms of service is activated. False otherwise.
   */
  public boolean isActivated() {
    return this != NEVER;
  }

  /**
   * Indicates if the given acceptance date of terms of service is valid compared to the current
   * date.
   * @param tosAcceptanceDate the date at which the terms of service have been accepted.
   * @return true if the acceptance date has expired. False otherwise.
   */
  public boolean isAcceptanceDateExpired(Date tosAcceptanceDate) {
    LocalDate acceptanceDate = tosAcceptanceDate == null ? null :
        LocalDate.ofInstant(tosAcceptanceDate.toInstant(), ZoneId.systemDefault());
    return isAcceptanceDateExpired(LocalDate.now(), acceptanceDate);
  }

  /**
   * Indicates if the given acceptance date of terms of service is valid compared to the reference
   * date.
   * @param referenceDate a date of reference to accept the terms of service.
   * @param tosAcceptanceDate the date at which the terms of service have been accepted.
   * @return true if the acceptance date has expired. False otherwise.
   */
  private boolean isAcceptanceDateExpired(LocalDate referenceDate, LocalDate tosAcceptanceDate) {
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
          Period validPeriod = Period.from(referenceDate).of(1, periodType);
          return !validPeriod.includes(tosAcceptanceDate);
        }
    }
    return true;
  }

  /**
   * Decode from a string value. NEVER by default (even the given value is unknown).
   * @param tosAcceptanceFrequency a string encoding the frequency at which the acceptance of the
   * terms of service has to be checked.
   * @return the check frequency of the acceptance of the terms of service.
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
