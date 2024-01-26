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
package org.silverpeas.core;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.exception.NotFoundException;

import java.util.Optional;

/**
 * A service providing the transverse operations related to a given Silverpeas application. A
 * Silverpeas application is a component of the Silverpeas portal that provides the features to
 * manage in a given way some types of user contributions and contents. Such an application can be
 * either a personal tool or a shareable component.
 * <p>
 * The service gathers the business functionalities the application offers to users or to other
 * components in Silverpeas and it gives access to both the application settings and the application
 * specific l10n translations. In the approach used in Silverpeas, this service should not be
 * accessed directly by clients but through the business entities that are managed by the instances
 * of the application (in the DDD (Domain Driven Development) way).
 * </p>
 * <p>
 * Some transverse services in Silverpeas Core use this interface to discover and to access the
 * business services of the applications in order to perform some of their centralized tasks. For
 * example, the notification of user about new comments is based on this interface. So, in order to
 * profit of these centralized and transverse functionalities, an application has to provide a
 * specific business service that implements this interface. In order to facilitate the business
 * service discovery, it is recommended to follow the convention in which the service is annotated
 * by the {@code javax.inject.Named} annotation valued with the name of the application (starting in
 * lower case) followed by the term "Service". For example, a service of a Foo application should be
 * annotated by <code>@Named("fooService")</code>.
 * </p>
 * @see ApplicationServiceProvider
 */
public interface ApplicationService {

  /**
   * Gets the service of the application from which the specified instance is spawned.
   * <p>
   * This method thrown an exception if no service is found for the specified component instance.
   * To have a more control on how to handle such a situation, use instead the
   * {@link ApplicationServiceProvider#getApplicationServiceById(String)} method that returns an
   * {@link Optional<ApplicationService>} value.
   * </p>
   * @param appId the unique identifier of an application instance.
   * @return an {@link ApplicationService} object that can serv the calls for the specified
   * application instance.
   * @throws NotFoundException if the application doesn't implement any transverse service.
   */
  static ApplicationService getInstance(final String appId) throws NotFoundException {
    return ApplicationServiceProvider.get()
        .getApplicationServiceById(appId)
        .orElseThrow(
            () -> new NotFoundException("No service found for application instance " + appId));
  }

  /**
   * Gets the contribution handled by an instance of the component with the specified unique
   * identifier. The type of the contribution to get is provided by the given identifier.
   * @param contributionId the unique identifier of the contribution to get.
   * @param <T> The concrete type of the contribution to get. An application can manage more than
   * one types of contributions.
   * @return a contribution in Silverpeas.
   */
  <T extends Contribution> Optional<T> getContributionById(ContributionIdentifier contributionId);

  /**
   * Gets the settings of this Silverpeas component.
   * @return a SettingBundle instance giving access the settings.
   */
  SettingBundle getComponentSettings();

  /**
   * Gets the localized messages defined in this Silverpeas component.
   * @param language the language in which the messages has to be localized. If empty or null, then
   * the bundle with default messages is returned.
   * @return a LocalizationBundle instance giving access the localized messages.
   */
  LocalizationBundle getComponentMessages(String language);

  /**
   * Is this service related to the specified component instance. The service is related to the
   * specified instance if it is a service defined by the application from which the instance was
   * spawned.
   * @param instanceId the unique instance identifier of the component.
   * @return true if the instance is spawn from the application to which the service is related.
   * False otherwise.
   */
  boolean isRelatedTo(String instanceId);
}
