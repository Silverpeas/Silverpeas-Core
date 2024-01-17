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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.space.model;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.ResourcePath;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The path of a space in the Silverpeas resources organizational tree.
 * @author mmoquillon
 */
public class SpacePath extends ArrayList<SpaceInstLight> implements ResourcePath<SpaceInstLight> {

  /**
   * Gets the path of the specified collaborative space.
   * @param spaceId the unique identifier of a collaborative space.
   * @return a {@link SpacePath} instance.
   */
  public static SpacePath getPath(final String spaceId) {
    return new SpacePath(OrganizationController.get().getPathToSpace(spaceId));
  }

  private SpacePath(@Nonnull final Collection<? extends SpaceInstLight> c) {
    super(c);
  }

  /**
   * Formats this path as a textual value in which each segments are separated by the specified
   * separator token. Each path segment is the name of the spaces in this path expressed in the
   * given language. If only a relative path is asked, then it is the deeper space name in the path
   * that is returned (the name of the last space in the list). If this path is empty, then an
   * empty String is returned.
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
    if (absolutePath) {
      return stream().map(s -> s.getTranslation(language).getName())
          .collect(Collectors.joining(pathSep));
    } else {
      return get(size() - 1).getTranslation(language).getName();
    }
  }
}
  