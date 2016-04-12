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
 * It is a process implied within the manufacturing of a new component instance in Silverpeas.
 * Usually such process is about allocating some specific resources for the component instance
 * being created.
 * </p>
 * When an application component is instantiated, it is first created and then registered into
 * Silverpeas. In some circumstances, according to the application, some actions have to be
 * performed in the behalf of the new application instance. The application instantiation is
 * unaware of these circumstances and it cannot know what actions to perform; It is the
 * responsibility of the application to perform such actions. This is why an implementation of this
 * interface qualified by a name that satisfies the following convention
 * <code>[COMPONENT NAME]InstancePostConstruction</code> is looked for by the instantiation process
 * and then invoked if it is has been found.
 * </p>
 * Any application that requires specific actions when a new application component is instantiated
 * has to implement this interface and the implementation has to be qualified with the @Named
 * annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstancePostConstruction</code>. For example, for an application Kmelia,
 * the implementation must be qualified with <code>@Named("kmeliaInstancePostConstruction")</code>
 * @author mmoquillon
 */
public interface ComponentInstancePostConstruction {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface.
   * An implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  String NAME_SUFFIX = "InstancePostConstruction";

  /**
   * Each workflow is an application but all of them uses the same post construction process.<br/>
   * So, when the name of a workflow component is detected, the post constructor implementation
   * retrieved will be the one named like this constant value.
   */
  String WORKFLOW_POST_CONSTRUCTION = "processManager" + NAME_SUFFIX;

  /**
   * Gets the implementation of this interface with the specified qualified name.
   * @param constructionName the qualified name of the implementation as specified by a
   * <code>@Named</code> annotation.
   * @return either an implementation of this interface or nothing.
   */
  @SuppressWarnings("Duplicates")
  static Optional<ComponentInstancePostConstruction> get(String constructionName) {
    try {
      final String name;
      if (WAComponent.get(constructionName).get().isWorkflow()) {
        name = WORKFLOW_POST_CONSTRUCTION;
      } else {
        name = constructionName.substring(0, 1).toLowerCase() + constructionName.substring(1) +
            NAME_SUFFIX;
      }
      return Optional.of(ServiceProvider.getService(name));
    } catch (IllegalStateException ex) {
      return Optional.empty();
    }
  }

  /**
   * Performs post construction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  void postConstruct(String componentInstanceId);
}
