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

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.ServiceProvider;

/**
 * Provides the manager of {@link Contribution} of a {@link SilverpeasComponentInstance}.
 * @author silveryocha
 */
public interface ComponentInstanceContributionManagerByInstance {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface.
   * An implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  String NAME_SUFFIX = "InstanceContributionManager";

  /**
   * Each workflow is an application but all of them uses the same routing map.<br>
   * So, when the name of a workflow component is detected, the routing map implementation
   * retrieved will be the one named like this constant value.
   */
  String WORKFLOW_ROUTING_NAME = "processManager" + NAME_SUFFIX;

  static ComponentInstanceContributionManagerByInstance get() {
    return ServiceProvider.getService(ComponentInstanceContributionManagerByInstance.class);
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
  ComponentInstanceContributionManager getByInstanceId(String instanceId);
}
