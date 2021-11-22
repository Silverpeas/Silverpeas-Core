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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.model.SpacePath;
import org.silverpeas.core.util.ResourcePath;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * The path of a component instance in the Silverpeas resources organizational tree.
 * @author mmoquillon
 */
public class ComponentInstPath extends ArrayList<ComponentInstLight>
    implements ResourcePath<ComponentInstLight> {

  /**
   * Gets the path of the specified component instance.
   * @param instanceId the unique identifier of a component instance.
   * @return a {@link ComponentInstPath} instance.
   */
  public static ComponentInstPath getPath(final String instanceId) {
    ComponentInstPath path = new ComponentInstPath();
    ComponentInstLight compInstLight =
        OrganizationController.get().getComponentInstLight(instanceId);
    path.add(compInstLight);
    return path;
  }

  private ComponentInstPath() {
    super(1);
  }

  /**
   * Formats this path as a textual value in which each segment is separated by the specified
   * separator token. Each path segment is the name of the spaces (and at the end of the component
   * instance) in this path expressed in the given language. If only a relative path is asked,
   * then the path starts with name of the space that contains the component instance targeted by
   * this path. Otherwise, the path starts with the root space for which the targeted component
   * instance is a descendent. If this path is empty, then an empty String is returned.
   * @param language the language in which the name should be expressed in the path.
   * @param absolutePath if false, only a relative path is returned. If true, an absolute path
   * is returned.
   * @param pathSep the path separator to use instead of the default one.
   * @return the String representation of this path by using the given path separator and in which
   * each path segment is the element's name in the specified language.
   */
  @Override
  public String format(final String language, final boolean absolutePath, final String pathSep) {
    if (isEmpty()) {
      return "";
    }
    ComponentInstLight compInst = get(0);
    String spaceId = compInst.getSpaceId();
    SpacePath spacePath = SpacePath.getPath(spaceId);
    return spacePath.format(language, absolutePath, pathSep) + pathSep +
        stream().map(s -> s.getTranslation(language).getName())
            .collect(Collectors.joining(pathSep));
  }
}
  