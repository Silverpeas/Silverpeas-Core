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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.service;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Optional;

/**
 * In charge of providing silverpeas component instance.
 * @author Yohann Chastagnier
 */
public interface SilverpeasComponentInstanceProvider {

  /**
   * Gets the instance of the implementation of the interface.
   * @return an implementation of {@link SilverpeasComponentInstanceProvider}.
   */
  static SilverpeasComponentInstanceProvider get() {
    return ServiceProvider.getSingleton(SilverpeasComponentInstanceProvider.class);
  }

  /**
   * Gets a any silverpeas component instance from the specified identifier.
   * @param componentInstanceId a component instance identifier as string.
   * @return an optional silverpeas component instance of {@link SilverpeasComponentInstance}.
   */
  Optional<SilverpeasComponentInstance> getById(String componentInstanceId);

  /**
   * Gets a shared silverpeas component instance from the specified identifier.
   * @param sharedComponentInstanceId a shared component instance identifier as string.
   * @return an optional silverpeas shared component instance of {@link
   * SilverpeasPersonalComponentInstance}.
   */
  Optional<SilverpeasSharedComponentInstance> getSharedById(String sharedComponentInstanceId);

  /**
   * Gets a personal silverpeas component instance from the specified identifier.
   * @param personalComponentInstanceId a personal component instance identifier as string.
   * @return an optional silverpeas personal component instance of {@link
   * SilverpeasPersonalComponentInstance}.
   */
  Optional<SilverpeasPersonalComponentInstance> getPersonalById(String personalComponentInstanceId);

  /**
   * Gets the name of the component from which the specified instance was spawn. The component
   * instance identifier is made up of the name of the component with a local identifier that
   * is peculiar to the type of the component. This method is a centralized way to get the component
   * name from a component instance identifier.
   * @param componentInstanceId the unique identifier of the component instance.
   * @return
   */
  String getComponentName(String componentInstanceId);
}
