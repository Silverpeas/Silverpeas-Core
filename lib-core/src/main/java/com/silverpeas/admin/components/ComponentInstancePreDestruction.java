/**
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.admin.components;

import org.silverpeas.util.ServiceProvider;

import java.util.Optional;

/**
 * <p>
 * It is a process implied within the deletion of a component instance in Silverpeas. Usually such
 * process is about deleting some specific resources of the component instance being deleted.
 * It could be also useful for component instances which some data deletion must be performed
 * before
 * the transverse ones.
 * </p>
 * <p>
 * When a component instance is deleted, the direct data are first deleted, then the decoration
 * data are deleted, and finally the component instance is unregistered from Silverpeas.
 * In some circumstances, according to the application, some actions have to be
 * performed in the behalf of the deleted application instance. The application instantiation is
 * unaware of these circumstances and it cannot know what actions to perform; It is the
 * responsibility of the application to perform such actions. This is why an implementation of this
 * interface qualified by a name that satisfies the following convention
 * <code>[COMPONENT NAME]InstancePreDestruction</code> is looked for by the component instance
 * deletion process and then invoked if it is has been found.
 * </p>
 * Any application that requires specific actions when a component instance is deleted
 * has to implement this interface and the implementation has to be qualified with the @Named
 * annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstancePreDestruction</code>. For example, for an application Kmelia,
 * the implementation must be qualified with <code>@Named("kmeliaInstancePreDestruction")</code>
 * @author Yohann Chastagnier
 */
public interface ComponentInstancePreDestruction {

  /**
   * Gets the implementation of this interface with the specified qualified name.
   * @param destructionName the qualified name of the implementation as specified by a
   * <code>@Named</code> annotation.
   * @return either an implementation of this interface or nothing.
   */
  static Optional<ComponentInstancePreDestruction> get(String destructionName) {
    try {
      String name = destructionName.substring(0, 1).toLowerCase() + destructionName.substring(1);
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
