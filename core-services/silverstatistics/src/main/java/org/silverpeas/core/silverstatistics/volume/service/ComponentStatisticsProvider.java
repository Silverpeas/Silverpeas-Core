/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.Optional;

/**
 * A provider of statistics data about some resources managed in a given Silverpeas Component.
 *
 * Each statistics provider must be managed in CDI and retrieved by a name that qualifies them
 * (annotation @Named). This name must satisfy the following rule: the name of the component that
 * provides this statistics provider suffixed by the value of the <code>QUALIFIER_SUFFIX</code>
 * constant.
 */
public interface ComponentStatisticsProvider {

  /**
   * The suffix of the name for each statistics provider.
   */
  String QUALIFIER_SUFFIX = "Statistics";

  static Optional<ComponentStatisticsProvider> getByComponentName(final String componentName) {
    /*
     * Gets the component statistics qualifier defined into SilverStatistics.properties file
     * associated to the component identified by the given name.
     * If no qualifier is defined for the component, a default conventional one is computed.
     */
    final String qualifier = ResourceLocator
        .getSettingBundle("org.silverpeas.silverstatistics.SilverStatistics")
        .getString(componentName, componentName + QUALIFIER_SUFFIX);

    try {
      return Optional.of(ServiceProvider.getService(qualifier));
    } catch (Exception e) {
      SilverLogger.getLogger(e).silent(e);
    }
    return Optional.empty();
  }

  /**
   * Gets by user the number of owned contributions.
   * @param spaceId the identifier of space which the component represented by componentId
   * parameter is linked to.
   * @param componentId the identifier of the current looked component instance.
   * @return a collection of {@link UserIdCountVolumeCouple}. The collection can contains several
   * {@link UserIdCountVolumeCouple} with the same user identifier.
   * @throws SilverpeasException on technical error.
   */
  Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws SilverpeasException;

  /**
   * Gets the memory size of documents of the application instance by taking into account only
   * the specific files handled by the application.
   * <p>
   * The files handled by transverse services are taken into account by other statistic services.
   * </p>
   * @param componentId the identifier of the current looked component instance.
   * @return a long.
   */
  default long memorySizeOfSpecificFiles(final String componentId) {
    return 0L;
  }

  /**
   * Counts the number of document the application instance uses by taking into account only the
   * specific files handled by the application.
   * <p>
   * The files handled by transverse services are taken into account by other statistic services.
   * </p>
   * @param componentId the identifier of the current looked component instance.
   * @return a long.
   */
  default long countSpecificFiles(final String componentId) {
    return 0L;
  }
}
