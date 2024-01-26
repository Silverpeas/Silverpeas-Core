/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * A provider of objets implementing the {@link ApplicationService} interface. The provider looks
 * for an implementation of the {@link ApplicationService} interface by the identifier of the
 * application instance. In order the discovery of such a service succeeds, it is required that each
 * implementation are annotated by the @{@link javax.inject.Named} annotation with as value the name
 * of the component with the first character in lowercase following by the term
 * <code>Service</code>; for example an application <code>Toto</code> should provide an
 * implementation of the {@link ApplicationService} interface named (with the {@link
 * javax.inject.Named} annotation) <code>totoService</code>.
 * @author mmoquillon
 */
@Provider
@Singleton
public class ApplicationServiceProvider {

  private static final String CACHE_PREFIX_KEY =
      ApplicationServiceProvider.class.getSimpleName() + "#";

  /**
   * The suffix to use when naming a service satisfying the {@link ApplicationService} interface
   * with the {@link javax.inject.Named} annotation.
   */
  public static final String SERVICE_NAME_SUFFIX = "Service";

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
   * @return optionally the transverse service that is defined for the type of applications the
   * given component instance is of. If no such application service exists, then nothing is
   * returned.
   */
  public Optional<ApplicationService> getApplicationServiceById(final String appId) {
    SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
    String appName = SilverpeasComponentInstance.getComponentName(appId);
    if (StringUtil.isNotDefined(appName)) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(cache.computeIfAbsent(CACHE_PREFIX_KEY + appName,
          ApplicationService.class,
          () -> ServiceProvider.getService(appId, SERVICE_NAME_SUFFIX)));
    } catch (SilverpeasRuntimeException e) {
      SilverLogger.getLogger(this).warn(e.getMessage());
      return Optional.empty();
    }
  }
}
