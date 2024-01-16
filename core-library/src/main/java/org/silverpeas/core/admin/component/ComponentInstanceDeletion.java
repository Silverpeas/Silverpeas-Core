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
package org.silverpeas.core.admin.component;

/**
 * It is a process implied within the deletion of a component instance. Usually such process is
 * about the deletion of some transverses resources that are used by the component instance being
 * deleted. To perform some treatments related specifically to the component instance that is being
 * deleted, the implementation of the
 * {@code org.silverpeas.core.admin.component.ComponentInstancePreDestruction} instance is preferred.
 * <p>
 * Each implementation of this interface is invoked by the generic instance deletion
 * process to perform their specific task. Usually, this interface should be implemented by each
 * transverse services in Silverpeas Core.
 * </p>
 * <p>
 * In each component instance, whatever it is, different kind of transverses resources can be
 * created and managed. When such an instance is then deleted in Silverpeas, it is necessary to
 * clean up also those resources. Nevertheless, both the generic instance deletion process and the
 * component instance itself aren't usually aware of them. It is then the responsibility of the
 * services behind the different kinds of resources to take in charge the freed of the resources
 * related to the component instance being deleted. For doing, they have to implement this
 * interface.
 * </p>
 * @author mmoquillon
 */
public interface ComponentInstanceDeletion {

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  void delete(String componentInstanceId);
}
