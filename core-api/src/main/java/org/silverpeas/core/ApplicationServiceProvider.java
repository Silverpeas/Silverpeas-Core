/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Optional;

/**
 * A provider of objets implementing the {@link ApplicationService} interface.
 * @author mmoquillon
 */
@Provider
public class ApplicationServiceProvider {

  private static final String SERVICE_NAME_PATTERN = "%sService";

  /**
   * Gets an instance of the {@link ApplicationServiceProvider} class.
   * @return an {@link ApplicationServiceProvider} instance.
   */
  public static ApplicationServiceProvider get() {
    return ServiceProvider.getService(ApplicationServiceProvider.class);
  }

  /**
   * Gets the application service that is defined for the given application instance. If no such
   * application service exists, then nothing is returned.
   * @param appId the unique identifier of an application instance in Silverpeas.
   * @param <T> the type of {@link Contribution}s on which the service works.
   * @return optionally the transverse service that is defined for the type of applications the
   * given component instance is of. If no such application service exists, then nothing is
   * returned.
   */
  public <T extends Contribution> Optional<ApplicationService<T>> getApplicationServiceById(
      final String appId) {
    String appName = SilverpeasComponentInstance.getComponentName(appId);
    try {
      String serviceName = String.format(SERVICE_NAME_PATTERN, appName);
      return Optional.of(ServiceProvider.getService(serviceName));
    } catch (IllegalStateException e) {
      SilverLogger.getLogger(this).warn(e.getMessage());
      return Optional.empty();
    }
  }
}
