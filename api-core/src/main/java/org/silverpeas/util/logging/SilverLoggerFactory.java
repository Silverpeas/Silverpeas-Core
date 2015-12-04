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
package org.silverpeas.util.logging;

/**
 * A factory of logger instances. It wraps the implementation of the
 * {@code org.silverpeas.util.logging.Logger} interface.
 * </p>
 * The bind between the {@code org.silverpeas.util.logging.LoggerFactory}
 * interface and its implementation is performed by the Java SPI (Java Service Provider Interface).
 * Only the first available logger factory implementation is loaded by the
 * {@code org.silverpeas.util.logging.Logger} when a logger object has to be get.
 * @author miguel
 */
public interface SilverLoggerFactory {

  /**
   * Get a {@code org.silverpeas.util.logging.Logger} instance for the specified namespace.
   * If a logger has already been created with the given namespace it is returned, otherwise a new
   * logger is manufactured.
   * </p>
   * This method should not be invoked directly. It is dedicated to be used by the
   * {@code org.silverpeas.util.logging.Logger#getLogger(String)} method.
   * @param namespace the hierarchical dot-separated namespace of the logger mapping the
   * hierachical relationships between the loggers from the root one.
   * @return a Silverpeas logger instance.
   */
  SilverLogger getLogger(String namespace);

}
