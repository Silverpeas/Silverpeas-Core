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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.util.Optional;

/**
 * <p>
 * This interface defines the locator of {@link Contribution} instances which must be implemented by
 * services providing contributions.
 * </p>
 * <p>
 * From a local identifier and a type, the system asks to the several
 * {@link ContributionLocatorByLocalIdAndType} implements the related full
 * {@link ContributionIdentifier}.<br> Ths {@link ContributionIdentifier} permits to use
 * {@link org.silverpeas.core.ApplicationService} service to retrieve some contributions.
 * </p>
 * @author silveryocha
 */
public interface ContributionLocatorByLocalIdAndType {

  /**
   * <p>
   * Indicates if the implementation is able to locate a contribution of the given type.
   * </p>
   * <p>
   * The main interest of this method is to increase the performance into mechanism of locating.
   * </p>
   * @param type a contribution type.
   * @return true if implementation is able to locate a contribution of the given type, false
   * otherwise.
   */
  boolean isContributionLocatorOfType(String type);

  /**
   * <p>
   * Gets the full {@link ContributionIdentifier} of a {@link Contribution} from a local identifier
   * and a type.
   * </p>
   * <p>
   * A local identifier is an identifier which is unique into the context of a component.<br> In a
   * higher scope level, application level so, there is no guarantee that the local identifier could
   * be unique.<br> That is why the type is a mandatory data for the contribution location search.
   * Indeed, a local id could not be unique at application level, but a couple localId / type has to
   * (publication services for example).
   * </p>
   * @param localId a local identifier.
   * @param type a type of contribution.
   * @return the optional full {@link ContributionIdentifier}.
   */
  Optional<ContributionIdentifier> getContributionIdentifierFromLocalIdAndType(String localId,
      final String type);
}
