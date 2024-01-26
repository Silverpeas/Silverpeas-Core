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

package org.silverpeas.core.contribution.indicator;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.admin.component.model.SilverpeasComponentInstance.getComponentName;

/**
 * Registry of different kind of contribution indicator.
 * <p>
 *   For now, there is just {@link NewContributionIndicator} which can be registered and used.
 * </p>
 * @author silveryocha
 */
@Singleton
public class ContributionIndicatorRegistry {

  private final Map<String, NewContributionIndicator> newRegistry = new HashMap<>();

  protected ContributionIndicatorRegistry() {
    // default constructor
  }

  public static ContributionIndicatorRegistry get() {
    return ServiceProvider.getService(ContributionIndicatorRegistry.class);
  }


  /**
   * Adds a {@link NewContributionIndicator} instance into registry.
   * @param indicator the implementation of {@link NewContributionIndicator}.
   */
  public void addNewContributionIndicator(final NewContributionIndicator indicator) {
    final Pair<String, String> relatedTo = indicator.relatedToComponentAndResourceType();
    newRegistry.put(relatedTo.getFirst() + "@" + relatedTo.getSecond(), indicator);
  }

  /**
   * Gets the {@link NewContributionIndicator} according to {@link ContributionIdentifier} instance.
   * @param cId a {@link ContributionIdentifier} instance aiming the contribution.
   * @return an optional {@link NewContributionIndicator} indicator implementation.
   */
  protected Optional<NewContributionIndicator> getNewContributionIndicatorBy(
      final ContributionIdentifier cId) {
    final String componentName = getComponentName(cId.getComponentInstanceId());
    return ofNullable(newRegistry.get(componentName + "@" + cId.getType()));
  }
}
