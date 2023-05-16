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
package org.silverpeas.core.test.util;

import org.silverpeas.core.SilverpeasRuntimeException;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public class SilverProperties extends Properties {

  private final Class baseClass;

  /**
   * Loads properties of given property file path from a class.<br>
   * Property file has to exist into resources of the project.
   * @param fromClass the class from which the properties are requested.
   * @param propertyFilePaths the paths of files that contains the aimed properties.
   * @return an instance of {@link SilverProperties} that contains requested properties.
   */
  public static SilverProperties load(Class fromClass, String... propertyFilePaths) {
    SilverProperties properties = new SilverProperties(fromClass);
    return properties.load(propertyFilePaths);
  }

  private SilverProperties(final Class baseClass) {
    this.baseClass = baseClass;
  }

  /**
   * Loads properties of given property file path from a class and add them to the currents.<br>
   * Property file has to exist into resources of the project.
   * @param propertyFilePaths the paths of files that contains the aimed properties.
   * @return an instance of {@link SilverProperties} that contains requested properties.
   */
  public SilverProperties load(String... propertyFilePaths) {
    for (String propertyFilePath : propertyFilePaths) {
      try (InputStream is = baseClass.getClassLoader().getResourceAsStream(propertyFilePath)) {
        load(is);
      } catch (Exception ex) {
        throw new SilverpeasRuntimeException(ex);
      }
    }
    return this;
  }
}
