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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * <p>
 * This interface defines the manager of {@link Contribution} instances of a {@link
 * SilverpeasComponentInstance}.
 * </p>
 * <p>
 * Each component should implement this interface if its {@link Contribution} has to be managed by
 * centralized services.
 * </p>
 * <p>
 * Any application that requires to provide {@link Contribution} to core services
 * has to implement this interface and the implementation has to be qualified with the @{@link
 * javax.inject.Named} annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstanceContributionManager</code>. For example, for an application Kmelia,
 * the implementation must be qualified with <code>@Named("kmeliaInstanceContributionManager")
 * </code>
 * <p>
 * @author silveryocha
 */
public interface ComponentInstanceContributionManager {

  /**
   * Constants are predefined value used by a contribution manager to work with and that carries a
   * semantic that has to be shared by all the implementations of this interface.
   */
  class Constants {

    private Constants() {

    }

    /**
     * The predefined suffix that must compound the name of each implementation of this interface.
     * An implementation of this interface by a Silverpeas application named Kmelia must be named
     * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
     */
    public static final String NAME_SUFFIX = "InstanceContributionManager";
  }

  /**
   * Gets the {@link ComponentInstanceContributionManager} according to the given identifier of
   * component instance.
   * <p>
   * Instances of {@link ComponentInstanceContributionManager} are request scoped (or thread scoped
   * on backend treatments).
   * </p>
   * @param instanceId the identifier of a component instance from which the qualified name of the
   * implementation will be extracted.
   * @return a {@link ComponentInstanceContributionManager} implementation.
   */
  static ComponentInstanceContributionManager getByInstanceId(final String instanceId) {
    final SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    final String cacheKey = ComponentInstanceContributionManager.class.getName() + "###" + instanceId;

    final Mutable<ComponentInstanceContributionManager> componentInstanceManager =
        Mutable.ofNullable(cache.get(cacheKey, ComponentInstanceContributionManager.class));
    if (componentInstanceManager.isPresent()) {
      return componentInstanceManager.get();
    }

    try {
      componentInstanceManager.set(ServiceProvider
          .getServiceByComponentInstanceAndNameSuffix(instanceId, Constants.NAME_SUFFIX));
    } catch (IllegalStateException e) {
      throw new SilverpeasRuntimeException(MessageFormat
          .format("no ComponentInstanceContributionManager implementation for {0}", instanceId), e);
    }
    cache.put(cacheKey, componentInstanceManager.get());
    return componentInstanceManager.get();
  }

  /**
   * Gets the {@link Contribution} instance linked to the given contribution identifier.
   * @param contributionId the representation of contribution identifier.
   * @return the optional {@link Contribution} instance.
   * @throws IllegalStateException when the type of the contribution is not handled by the
   * implementation.
   */
  Optional<Contribution> getById(ContributionIdentifier contributionId);
}
