/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Optional;

/**
 * <p>
 * It is a process implied within the deletion of a component instance in Silverpeas. Usually such
 * process is about the deletion of some specific resources related to the component instance that
 * is being deleted. This process is invoked before any cleaning up of the transverses data used
 * by the component instance and before the effective deletion of the component instance. This
 * interface is dedicated to be implemented by the Silverpeas applications.
 * </p>
 * <p>
 * When a component instance is being deleted, the resources that are specific to this component
 * instance should be deleted first before going further in the deletion. That's why this interface
 * is for. Once done, the transverses resources that were used by the component instance are then
 * deleted and finally the component instance is unregistered from Silverpeas. As the generic
 * component instance deletion mechanism is unaware of the specific resources that are managed
 * by the component instance, it delegates the task to delete them to the implementation of this
 * interface that should be provided by the Silverpeas application. In order to be found by the
 * deletion mechanism, the implementation has to be qualified by a name satisfying the following
 * convention: <code>[COMPONENT NAME]InstancePreDestruction</code>
 * </p>
 * Any application that requires specific actions when a component instance is being deleted
 * has to implement this interface and the implementation has to be qualified with the @Named
 * annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstancePreDestruction</code>. For example, for an application Kmelia,
 * the implementation must be qualified with <code>@Named("kmeliaInstancePreDestruction")</code>
 * @author Yohann Chastagnier
 */
public interface ComponentInstancePreDestruction {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface.
   * An implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  String NAME_SUFFIX = "InstancePreDestruction";

  /**
   * Each workflow is an application but all of them uses the same pre destruction process.<br/>
   * So, when the name of a workflow component is detected, the pre destruction implementation
   * retrieved will be the one named like this constant value.
   */
  String WORKFLOW_PRE_DESTRUCTION = "processManager" + NAME_SUFFIX;

  /**
   * Gets the implementation of this interface with the specified qualified name.
   * @param destructionName the qualified name of the implementation as specified by a
   * <code>@Named</code> annotation.
   * @return either an implementation of this interface or nothing.
   */
  @SuppressWarnings("Duplicates")
  static Optional<ComponentInstancePreDestruction> get(String destructionName) {
    try {
      final String name;
      if (WAComponent.get(destructionName).get().isWorkflow()) {
        name = WORKFLOW_PRE_DESTRUCTION;
      } else {
        name = destructionName.substring(0, 1).toLowerCase() + destructionName.substring(1) +
            NAME_SUFFIX;
      }
      return Optional.of(ServiceProvider.getService(name));
    } catch (IllegalStateException ex) {
      return Optional.empty();
    }
  }

  /**
   * Performs pre destruction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  void preDestroy(String componentInstanceId);
}
