/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.text.MessageFormat;

/**
 * @author silveryocha
 */
@Singleton
class DefaultComponentInstanceContributionManagerByInstance
    implements ComponentInstanceContributionManagerByInstance {
  private static String CACHE_KEY_PREFIX =
      ComponentInstanceContributionManager.class.getName() + "###";

  @Override
  public ComponentInstanceContributionManager getByInstanceId(final String instanceId) {
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    String cacheKey = CACHE_KEY_PREFIX + instanceId;

    final Mutable<ComponentInstanceContributionManager> componentInstanceManager =
        Mutable.ofNullable(cache.get(cacheKey, ComponentInstanceContributionManager.class));
    if (componentInstanceManager.isPresent()) {
      return componentInstanceManager.get();
    }

    try {
      SilverpeasComponent component = SilverpeasComponent.getByInstanceId(instanceId).orElseThrow(
          () -> new IllegalArgumentException(
              MessageFormat.format("no component exists for {0}", instanceId)));
      final String name;
      if (component.isWorkflow()) {
        name = ComponentInstanceContributionManagerByInstance.WORKFLOW_ROUTING_NAME;
      } else {
        final String componentName = component.getName();
        name = componentName.substring(0, 1).toLowerCase() + componentName.substring(1) +
            ComponentInstanceContributionManagerByInstance.NAME_SUFFIX;
      }
      componentInstanceManager.set(ServiceProvider.getService(name));
    } catch (IllegalStateException e) {
      throw new SilverpeasRuntimeException(MessageFormat
          .format("no ComponentInstanceContributionManager implementation for {0}", instanceId), e);
    }
    cache.put(cacheKey, componentInstanceManager.get());
    return componentInstanceManager.get();
  }
}
