/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Optional;

/**
 * <p>
 * This interface defines the locator of {@link Contribution} instances from part of {@link
 * ContributionIdentifier} data.
 * </p>
 * <p>
 * The aim is to compute from partial identifier the full {@link ContributionIdentifier} which can
 * then be used by {@link ContributionManager} services to get the contribution data.<br>
 * This mechanism is useful for core services which have to perform treatments on contribution data.
 * (permalink for example)
 * </p>
 * <p>
 * Its important, from the point of view of performances, to use this service only in particular
 * cases. For example, a permalink can not have the component instance identifier about a
 * contribution identifier, otherwise a contribution moved from a component instance to another one
 * become not valid.
 * </p>
 * @author silveryocha
 */
public interface ContributionLocator {

  static ContributionLocator get() {
    return ServiceProvider.getService(ContributionLocator.class);
  }

  /**
   * <p>
   * Gets the full {@link ContributionIdentifier} of a {@link Contribution} from a local identifier
   * and a type.
   * </p>
   * <p>
   * A local identifier is an identifier which is unique into the context of a component.<br>
   * In a higher scope level, application level so, there is no guarantee that the local identifier
   * could be unique.<br>
   * That is why the type is a mandatory data for the contribution location search. Indeed, a local
   * id could not be unique at application level, but a couple localId / type has to (publication
   * services for example).
   * </p>
   * @param localId a local identifier.
   * @param type a type of contribution.
   * @return the optional full {@link ContributionIdentifier}.
   */
  Optional<ContributionIdentifier> locateByLocalIdAndType(String localId, final String type);
}
