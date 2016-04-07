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
package org.silverpeas.core.util.logging;

import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * A factory of logger instances. It wraps in fact the implementation of the Silverpeas Logging
 * Engine by wrapping and hence using a true logging backend.
 * </p>
 * The factory isn't dedicated to be used by client code but by the
 * {@link org.silverpeas.core.util.logging.SilverLogger} class to obtain an instance of a logger
 * according to the actual active logging backend. By implementing this interface, the implementor
 * has the total control of any cache mechanism as well as of the loggers manufacture process.
 * </p>
 * The bind between the {@link org.silverpeas.core.util.logging.SilverLoggerFactory}
 * interface and its implementation is performed by the Java SPI (Java Service Provider Interface).
 * Only the first available logger factory implementation is loaded.
 * @author miguel
 */
public interface SilverLoggerFactory {

  /**
   * Gets an instance of the logger factory. The implementation is provided by the Java Service
   * Provider Interface and this implementation wraps the concrete logging mechanism used by
   * the Silverpeas Logging Engine.
   * @return an instance of a logger factory.
   */
  static SilverLoggerFactory get() {
    Iterator<SilverLoggerFactory> iterator =
        ServiceLoader.load(SilverLoggerFactory.class).iterator();
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      throw new RuntimeException(
          "No Silverpeas logger factory detected! At least one Silverpeas logger factory should " +
              "be available!");
    }
  }

  /**
   * Gets a {@link org.silverpeas.core.util.logging.SilverLogger} instance for the specified
   * namespace. If a logger has already been created with the given namespace it is returned,
   * otherwise a new logger is manufactured and initialized.
   * </p>
   * The logging level of the returned logger will be set according to the logging configuration
   * found for the given logger namespace. If no level setting is found from the configuration or
   * if there is no configuration found for the specified namespace, then the logger level is set
   * to null meaning it should inherit its level from its nearest ancestor with a specific
   * (non-null) level value. It is the responsibility of the implementation of the logger to take
   * care of the logging level inheritance and of the default log handlers/adapters used by
   * Silverpeas.
   * </p>
   * This method should not be invoked directly. It is dedicated to be used by the
   * {@link org.silverpeas.core.util.logging.SilverLogger#getLogger(String)} method or by the
   * implementation of the Silverpeas Logging Engine.
   * @param namespace the hierarchical dot-separated namespace of the logger mapping the
   * hierachical relationships between the loggers from the root one.
   * @return a Silverpeas logger instance.
   */
  default SilverLogger getLogger(String namespace) {
    LoggerConfiguration configuration =
        LoggerConfigurationManager.get().getLoggerConfiguration(namespace);
    return getLogger(namespace, configuration);
  }

  /**
   * Gets a {@link org.silverpeas.core.util.logging.SilverLogger} instance for the specified
   * namespace. If a logger has already been created with the given namespace it is returned,
   * otherwise a new logger is manufactured and initialized from the given logger configuration.
   * </p>
   * The logging level of the returned logger will be set according to the specified logging
   * configuration. If no level setting is found from the configuration or
   * if there is no configuration found for the specified namespace, then the logger level is set
   * to null meaning it should inherit its level from its nearest ancestor with a specific
   * (non-null) level value. It is the responsibility of the implementation of the logger to take
   * care of the logging level inheritance and of the default log handlers/adapters used by
   * Silverpeas.
   * </p>
   * This method should not be invoked directly. It is dedicated to be used by the
   * {@link org.silverpeas.core.util.logging.SilverLogger#getLogger(String)} method or by the
   * implementation of the Silverpeas Logging Engine.
   * @param namespace the hierarchical dot-separated namespace of the logger mapping the
   * hierachical relationships between the loggers from the root one.
   * @param configuration the logger configuration to use when initializing the manufactured
   * logger. If the logger already exists, the configuration won't be used in order to avoid
   * any replacement of the existing configuration. To update its configuration, please use
   * instead {@link org.silverpeas.core.util.logging.LoggerConfigurationManager}.
   * @return a Silverpeas logger instance.
   */
  SilverLogger getLogger(String namespace, LoggerConfiguration configuration);

}
