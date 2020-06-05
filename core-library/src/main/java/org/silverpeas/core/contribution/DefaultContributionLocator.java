/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author silveryocha
 */
@Singleton
public class DefaultContributionLocator implements ContributionLocator {

  private Map<String, List<Class<? extends ContributionLocatorByLocalIdAndType>>>
      potentialLocatorsByType = new HashMap<>();

  @Override
  public Optional<ContributionIdentifier> locateByLocalIdAndType(final String localId,
      final String type) {

    List<Class<? extends ContributionLocatorByLocalIdAndType>> potentialLocators =
        getPotentialLocators(type);

    for (Class<? extends ContributionLocatorByLocalIdAndType> locatorClass : potentialLocators) {
      ContributionLocatorByLocalIdAndType locator = ServiceProvider.getSingleton(locatorClass);
      Optional<ContributionIdentifier> contributionIdentifier =
          locator.getContributionIdentifierFromLocalIdAndType(localId, type);
      if (contributionIdentifier.isPresent()) {
        return contributionIdentifier;
      }
    }

    return notLocated(localId, type);
  }

  /**
   * From the given type, returning the potential implementations of {@link Contribution} locator.
   * @param type type of a {@link Contribution}.
   * @return the list of potential locator.
   */
  private List<Class<? extends ContributionLocatorByLocalIdAndType>> getPotentialLocators(
      final String type) {
    List<Class<? extends ContributionLocatorByLocalIdAndType>> potentialLocators =
        potentialLocatorsByType.get(type);
    if (potentialLocators == null) {
      // looking for potential implementations
      potentialLocators = new ArrayList<>();
      Collection<ContributionLocatorByLocalIdAndType> locators =
          ServiceProvider.getAllServices(ContributionLocatorByLocalIdAndType.class);
      for (ContributionLocatorByLocalIdAndType locator : locators) {
        if (locator.isContributionLocatorOfType(type)) {
          potentialLocators.add(locator.getClass());
        }
      }
      potentialLocatorsByType.put(type, potentialLocators);
    }
    return potentialLocators;
  }

  /**
   * Handle the return of the service in case of the {@link Contribution} has not been located.
   * @param localId a local identifier.
   * @param type a type of contribution.
   * @return empty optional.
   */
  private Optional<ContributionIdentifier> notLocated(final String localId, final String type) {
    // from a previous search, the system knows that it exist no way to retrieve a full
    // ContributionIdentifier from the given type
    SilverLogger.getLogger(this).warn(SilverpeasExceptionMessages
        .failureOnGetting("ContributionIdentifier", localId + "[" + type + "]"));
    return Optional.empty();
  }
}
