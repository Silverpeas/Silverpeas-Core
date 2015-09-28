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
package org.silverpeas.util;

import java.util.Set;

/**
 * A Silverpeas bundle represents a bundle containing data required by the different features
 * of Silverpeas. Each data is defined by a pair of key-value.
 * @author miguel
 */
public interface SilverpeasBundle {

  /**
   * Gets a set of all keys defined in this bundle.
   * @return a set of keys.
   */
  Set<String> keySet();

  /**
   * Is this bundle contains the specified data
   * @param key the name of the data.
   * @return true if this bundle has a data with the specified key, false otherwise.
   */
  boolean containsKey(final String key);

  /**
   * What is the fully qualified name of this bundle.
   * @return the base bundle name (that is to say without the hierarchy path name that is handled
   * by {@code org.silverpeas.util.ResourceLocator}).
   */
  String getBaseBundleName();

  /**
   * Gets the value as a String of the data identified by the specified key.
   * @param key the unique name of the data in this bundle.
   * @return the value of the data as a string of characters.
   */
  String getString(String key);
}
