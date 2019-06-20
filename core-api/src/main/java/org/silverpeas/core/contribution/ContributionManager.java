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
 * This interface defines the manager of {@link Contribution} instances.
 * </p>
 * <p>
 * The aim is to handles from a high level the getting of contribution data.
 * </p>
 * @author silveryocha
 */
public interface ContributionManager {

  static ContributionManager get() {
    return ServiceProvider.getService(ContributionManager.class);
  }

  /**
   * Gets the {@link Contribution} instance linked to the given contribution identifier.
   * @param contributionId the representation of contribution identifier.
   * @return the optional {@link Contribution} instance.
   */
  Optional<Contribution> getById(ContributionIdentifier contributionId);
}
