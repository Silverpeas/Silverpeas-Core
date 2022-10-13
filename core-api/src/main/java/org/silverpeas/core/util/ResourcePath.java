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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.silverpeas.core.SilverpeasResource;

import java.util.List;

/**
 * The path of a resource in the organization tree of Silverpeas. The path is represented here by
 * a list of the resources that made up the path ordered from the upper parent down to the resource
 * (the last element). Each typeof resource in Silverpeas should implement this interface in order
 * to compute their path in the organizational tree of Silverpeas.
 * @author mmoquillon
 */
public interface ResourcePath<T extends SilverpeasResource> extends List<T> {

  /**
   * Constants
   */
  class Constants {
    private Constants() {
    }

    /**
     * The default path segments separator to use when building the path String.
     */
    public static final String DEFAULT_SEPARATOR = " > ";
  }

  /**
   * Formats an relative textual path with the name of each of the resources constitutive of the
   * path (and therefore in this list). The relativity of the path depends on the implementation
   * of this interface.
   * @param language the language in which the name should be expressed in the path.
   * @return a String representation of the path or an empty String if this path is empty.
   */
  default String format(final String language) {
    return format(language, false);
  }

  /**
   * Formats a textual path with the name of each of the resources constitutive of the
   * path (and therefore in this list). The relativity of the path depends on the implementation
   * of this interface. The absolute path must be computed from the root space to which the
   * resources in this path are a descendent.
   * @param language the language in which the name should be expressed in the path.
   * @param absolutePath if false, only a relative path is returned. If true, an absolute path
   * is returned.
   * @return a String representation of the path or an empty String if this path is empty.
   */
  default String format(final String language, final boolean absolutePath) {
    return format(language, absolutePath, Constants.DEFAULT_SEPARATOR);
  }

  /**
   * Formats a textual path with the name of each of the resources constitutive of the
   * path (and therefore in this list). The relativity of the path depends on the
   * implementation of this interface. The absolute path must be computed from the root space to
   * which the resources in this path are a descendent.
   * @param language the language in which the name should be expressed in the path.
   * @param absolutePath if false, only a relative path is returned. If true, an absolute path
   * is returned.
   * @param pathSep the path separator to use instead of the default one.
   * @return a String representation of the path or an empty String if this path is empty.
   */
  String format(final String language, final boolean absolutePath, final String pathSep);
}
