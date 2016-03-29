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
package org.silverpeas.core.contribution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the different possible status in which a contribution must be.
 * @author: Yohann Chastagnier
 */
public enum ContributionStatus {
  UNKNOWN,
  DRAFT,
  PENDING_VALIDATION,
  REFUSED,
  VALIDATED;

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static ContributionStatus from(String name) {
    try {
      return valueOf(name.toUpperCase());
    } catch (Exception e) {
      return UNKNOWN;
    }
  }

  public boolean isInDraft() {
    return this == ContributionStatus.DRAFT;
  }

  public boolean isRefused() {
    return this == ContributionStatus.REFUSED;
  }

  public boolean isPendingValidation() {
    return this == ContributionStatus.PENDING_VALIDATION;
  }

  public boolean isValidated() {
    return this == ContributionStatus.VALIDATED;
  }
}
