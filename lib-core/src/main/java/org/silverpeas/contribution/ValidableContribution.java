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
package org.silverpeas.contribution;

import com.silverpeas.SilverpeasContent;
import org.silverpeas.contribution.model.ContributionValidation;

/**
 * A validable contribution is an object that represents a contribution which can be validated.
 * This interface defines all methods that must be implemented in order to obtain differents
 * contribution types that can be handled by a same mechanism.
 * @author: Yohann Chastagnier
 */
public interface ValidableContribution extends SilverpeasContent {

  /**
   * Is that the status of the contribution is draft?
   * @return true if it is the case, false otherwise.
   */
  boolean isInDraft();

  /**
   * Is that the status of the contribution is refused?
   * @return true if it is the case, false otherwise.
   */
  boolean isRefused();

  /**
   * Is that the status of the contribution is pending validation?
   * @return true if it is the case, false otherwise.
   */
  boolean isPendingValidation();

  /**
   * Is that the status of the contribution is validated?
   * @return true if it is the case, false otherwise.
   */
  boolean isValidated();

  /**
   * Gets the status of the contribution.
   * @return a contribution status.
   * @see ContributionStatus
   */
  ContributionStatus getStatus();

  /**
   * Sets the specified status to the contribution.
   * @param status the status of the contribution to set.
   * @see ContributionStatus
   */
  void setStatus(final ContributionStatus status);

  /**
   * Gets the contribution validation instance.
   * @return a contribution validation object.
   * @see ContributionValidation
   */
  ContributionValidation getValidation();
}
