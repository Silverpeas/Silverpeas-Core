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

package org.silverpeas.core.web.mvc.route;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.SilverpeasWebResource;

import java.util.Optional;

/**
 * The default implementation of {@link ComponentInstanceRoutingMapProvider} interface.
 * @author silveryocha
 */
class DefaultComponentInstanceRoutingMapProvider implements ComponentInstanceRoutingMapProvider {

  private String instanceId;
  private String componentName;
  private AbstractComponentInstanceRoutingMap relativeRoutingMap;
  private AbstractComponentInstanceRoutingMap relativeToSilverpeasRoutingMap;
  private AbstractComponentInstanceRoutingMap absoluteRoutingMap;

  DefaultComponentInstanceRoutingMapProvider(final String instanceId) {
    this.instanceId = instanceId;
    this.componentName = SilverpeasComponentInstance.getComponentName(instanceId);
  }

  @Override
  public ComponentInstanceRoutingMap relative() {
    if (relativeRoutingMap == null) {
      relativeRoutingMap = newRoutingMap().init(instanceId, StringUtil.EMPTY,
          SilverpeasWebResource.BASE_PATH + getWebResourceBase());
    }
    return relativeRoutingMap;
  }

  @Override
  public ComponentInstanceRoutingMap relativeToSilverpeas() {
    if (relativeToSilverpeasRoutingMap == null) {
      relativeToSilverpeasRoutingMap = newRoutingMap().init(instanceId, URLUtil.getApplicationURL(),
          SilverpeasWebResource.getBasePath() + getWebResourceBase());
    }
    return relativeToSilverpeasRoutingMap;
  }

  @Override
  public ComponentInstanceRoutingMap absolute() {
    if (absoluteRoutingMap == null) {
      absoluteRoutingMap = newRoutingMap().init(instanceId, URLUtil.getAbsoluteApplicationURL(),
          SilverpeasWebResource.getAbsoluteBasePath() + getWebResourceBase());
    }
    return absoluteRoutingMap;
  }

  private String getWebResourceBase() {
    return "/" + Optional.ofNullable(componentName)
        .map(String::toLowerCase)
        .orElseGet(() -> instanceId.toLowerCase());
  }

  /**
   * Gets a new instance of a {@link ComponentInstanceRoutingMap} according to instance identifier.
   * @return a {@link ComponentInstanceRoutingMap} instance.
   */
  private AbstractComponentInstanceRoutingMap newRoutingMap() {
    final Mutable<AbstractComponentInstanceRoutingMap> componentRoutingMap = Mutable.empty();
    if (componentName != null) {
      try {
        componentRoutingMap.set(ServiceProvider
            .getServiceByComponentInstanceAndNameSuffix(componentName,
                ComponentInstanceRoutingMap.NAME_SUFFIX));
      } catch (IllegalStateException e) {
        SilverLogger.getLogger(ComponentInstanceRoutingMap.class).silent(e);
      }
    }
    if (!componentRoutingMap.isPresent()) {
      componentRoutingMap.set(ServiceProvider.getService(DefaultComponentInstanceRoutingMap.class));
    }
    return componentRoutingMap.get();
  }
}
