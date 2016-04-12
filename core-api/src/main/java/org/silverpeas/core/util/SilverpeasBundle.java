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
package org.silverpeas.core.util;

import java.util.MissingResourceException;
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
   * by {@code org.silverpeas.core.util.ResourceLocator}).
   */
  String getBaseBundleName();

  /**
   * Gets the value as a String of the data identified by the specified key. The key should exist
   * in the bundle otherwise a {@code java.util.MissingResourceException} exception is thrown.
   * @param key the unique name of the data in this bundle.
   * @return the value of the data as a string of characters.
   * @throws MissingResourceException if either the bundle doesn't exist or the key isn't defined
   * in the bundle.
   */
  String getString(String key) throws MissingResourceException;

  /**
   * The resource is a list of objects. Gets the value(s) of the specified property of one or more
   * objects in the list. The objects in the list are identified by their index in the list (from
   * 1 to n). The key used to find the asked value(s) is composed first by the list identifier,
   * an underscore separator, then by the index of the object in the list, a dot separator, and
   * finally ends with the object's property name.<br/>
   * For example :
   * <code>
   * <ul>
   * <li>User_1.Name=firstName</li>
   * <li>User_2.Name=lastName</li>
   * <li>...</li>
   * </ul>
   * </code>
   * If the computed key isn't defined in the bundle, then no {@link MissingResourceException}
   * exception is thrown (for compatibility reason with Silverpeas versions lesser than 6).
   * @param list the identifier of the list in the bundle.
   * @param property the object's property for which the value will be fetch.
   * iterated up to find an object whose the property is set. If max is >= 1 then the
   * specified property of the first max objects are read (if the property isn't set for an object,
   * it is set to an empty string).
   * @return an array of string with several values. If the property of an object to read exists but
   * isn't set, then an empty string is set in the array.
   * @throws MissingResourceException if the bundle doesn't exist.
   */
  default SilverpeasBundleList getStringList(String list, String property)
      throws MissingResourceException {
    return getStringList(list, property, -1);
  }

  /**
   * The resource is a list of objects. Gets the value(s) of the specified property of one or more
   * objects in the list. The objects in the list are identified by their index in the list (from
   * 1 to n). The key used to find the asked value(s) is composed first by the list identifier,
   * an underscore separator, then by the index of the object in the list, a dot separator, and
   * finally ends with the object's property name.<br/>
   * For example :
   * <code>
   *   <ul>
   *   <li>User_1.Name=firstName</li>
   *   <li>User_2.Name=lastName</li>
   *   <li>...</li>
   *   </ul>
   * </code>
   * If the computed key isn't defined in the bundle, then no {@link MissingResourceException}
   * exception is thrown (for compatibility reason with Silverpeas versions lesser than 6).
   * @param list the identifier of the list in the bundle.
   * @param property the object's property for which the value will be fetch.
   * @param max the maximum number of objects to read from 1 to n. If max is -1 then the list is
   * iterated up to find an object whose the property is set. If max is >= 1 then the
   * specified property of the first max objects are read (if the property isn't set for an object,
   * it is set to an empty string).
   * @return an array of string with one property value (if max is 1) or with several values (if
   * max = -1 or > 1). If the property of an object to read exists but isn't set, then an empty
   * string is set in the array.
   * @throws MissingResourceException if the bundle doesn't exist.
   */
  default SilverpeasBundleList getStringList(String list, String property, int max)
      throws MissingResourceException {
    int i = 1;
    SilverpeasBundleList finalList = SilverpeasBundleList.with();
    while ((i <= max) || (max == -1)) {
      try {
        String key = list + "_" + Integer.toString(i) + "." + property;
        String s = getString(key);
        if (s == null) {
          throw new MissingResourceException(getBaseBundleName(), getClass().getName(), key);
        }
        finalList.add(s);
      } catch (MissingResourceException ex) {
        if (ex.getKey() == null || ex.getKey().trim().isEmpty()) {
          throw ex;
        }
        if (max == -1) {
          max = i;
        } else {
          finalList.add("");
        }
      }
      i++;
    }
    return finalList;
  }

  /**
   * Is this bundle exists?
   * @return true if this bundle exists, false otherwise.
   */
  boolean exists();
}
