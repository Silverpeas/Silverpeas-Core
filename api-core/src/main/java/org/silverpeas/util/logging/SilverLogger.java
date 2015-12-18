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

import org.silverpeas.util.SilverpeasModule;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * The custom logger for Silverpeas. It abstracts the concrete logging mechanism that will be used
 * in runtime.
 * </p>
 * It is an adapter to a concrete underlying logging backend to use to log messages. The logging
 * backend can be the logging subsystem of the used application server or a tiers logging framework.
 * With the adapter it can then be easy to switch from an implementation to the other without
 * impacting the logging use in the code of Silverpeas.
 * </p>
 * An implementation of this interface must be a wrapper of a concrete logging mechanism and
 * instances of the implementation must be manufactured by the
 * @{code org.silverpeas.util.logging.LoggerFactory} factory.
 * @author miguel
 */
public interface SilverLogger {

  /**
   * The namespace of the root logger in Silverpeas. This namespace is predefined and each logger
   * taken in charge by the Logging Engine should be a descendant of its logger.
   */
  public static final String ROOT_NAMESPACE = "Silverpeas";

  /**
   * Gets the logger that is defined for the specified Silverpeas module.
   * </p>
   * Each module should have a logging configuration that is managed by an instance of the
   * {@code org.silverpeas.util.logging.LoggerConfigurationManager} class. For each module, the
   * configuration should provide a logging namespace (the name or category under which the logging
   * text will be written) and optionally a logging level. If no logging configuration is defined
   * for the given Silverpeas module or if no namespace is set, then a default namespace is
   * computed from the given module name.
   * </p>
   *  The logger instance is obtained from the logging namespace mapped with the given Silverpeas
   *  module by using the {@code org.silverpeas.util.logging.LoggerFactory} factory. The factory
   *  should be managed by the Java Service Provider Interface. The instance of the first
   *  implementation of the LoggerFactory class returned by the Service Provider Interface will be
   *  used to manufactured the logger from a logging namespace.
   *  </p>
   * The logging level of the returned logger will be set according to the logging configuration
   * found for the given module. If no level setting is found from the configuration or if there is
   * no configuration found for the specified module, then the logger level is set to null meaning
   * it should inherit its level from its nearest ancestor with a specific (non-null)
   * level value. It is the responsibility of the implementation of the logger to take care of the
   * logging level inheritance and of the default log handlers/adapters used by Silverpeas.
   * @param module the name of a Silverpeas module.
   * @return a logger instance, manufactured by the first LoggerFactory instance found.
   */
  static SilverLogger getLogger(String module) {
    Iterator<SilverLoggerFactory> iterator = ServiceLoader.load(SilverLoggerFactory.class).iterator();
    if (iterator.hasNext()) {
      SilverLoggerFactory factory = iterator.next();
      LoggerConfigurationManager.LoggerConfiguration configuration =
          LoggerConfigurationManager.get().getLoggerConfiguration(module);
      SilverLogger logger = factory.getLogger(configuration.getNamespace());
      if (!configuration.getNamespace().equals(ROOT_NAMESPACE) ||
          configuration.getLevel() != null) {
        // we take care to not erase the root logger level
        logger.setLevel(configuration.getLevel());
      }
      return logger;
    } else {
      throw new RuntimeException(
          "No Silverpeas logger factory detected! At least one Silverpeas logger factory should " +
              "be available!");
    }
  }

  /**
   * Gets the logger for the specified object. The logger that is returned is the one mapped with
   * the Silverpeas module into which the object belongs.
   * @see SilverLogger#getLogger(String)
   */
  static SilverLogger getLogger(Object object) {
    return getLogger(SilverpeasModule.getModuleName(object));
  }

  /**
   * Gets the namespace of this logger. Each logger is defined by a unique dot separated namespace.
   * @return the namespace of this logger.
   */
  String getNamespace();

  /**
   * Gets the log Level of this Logger. If no level was explicitly set for his logger, its
   * effective level is then inherited from its parent.
   * @return the level of this logger.
   */
  Level getLevel();

  /**
   * Sets the log level specifying which message levels will be logged by this logger. Message
   * levels lower than this value will be discarded.
   * If the new level is null, it means that this node should inherit its level from its nearest
   * ancestor with a specific (non-null) level value.
   * @param level the new value for the log level (may be null).
   */
  void setLevel(Level level);

  /**
   * Checks if a message of the given level would actually be logged by this logger. This check is
   * based on the Loggers effective level, which may be inherited from its parent.
   * @param level a logging level.
   * @return true if a message of the given level would actually be logged. False otherwise.
   */
  boolean isLoggable(Level level);

  /**
   * Logs a message at the specified level, with the specified parameters if any and with a
   * Throwable object if any.
   * If the logger is currently enabled for the given message level then a corresponding logging
   * record is created and forwarded to all the registered output handlers objects.
   * @param level the level of the message to log.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to the message. Shouldn't be null.
   * @param error an error to log with the message.
   */
  void log(Level level, String message, Object[] parameters, Throwable error);

  /**
   * Logs a message at the specified level, with the specified parameters if any.
   * @param level the level of the message to log.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to the message.
   */
  void log(Level level, String message, Object... parameters);

  /**
   * Logs an error message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   */
  default void error(String message, Object... parameters) {
    log(Level.ERROR, message, parameters);
  }

  /**
   * Logs an error message with the specified parameters.
   * @param message the message to log.
   * @param error the cause of the error.
   */
  default void error(String message, Throwable error) {
    log(Level.ERROR, message, new String[0], error);
  }

  /**
   * Logs an error message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   * @param error the cause of the error.
   */
  default void error(String message, Object[] parameters, Throwable error) {
    log(Level.ERROR, message, parameters, error);
  }

  /**
   * Logs an error message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   */
  default void warn(String message, Object... parameters) {
    log(Level.WARNING, message, parameters);
  }

  /**
   * Logs an information message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   */
  default void info(String message, Object... parameters) {
    log(Level.INFO, message, parameters);
  }

  /**
   * Logs a debugging message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   */
  default void debug(String message, Object... parameters) {
    log(Level.DEBUG, message, parameters);
  }

  /**
   * Logs an debugging message with the specified parameters. The message is computed by a function
   * that will be invoked only if the actual logger level is higher or equal than the message
   * level.
   * </p>
   * Usually, debugging message are computed by executing codes that shouldn't be ran in a nominal
   * execution context. In order to avoid this code to be executed when the logger level is lower
   * than the debug level, it can be written within a function whose the invocation is left to
   * the logger responsibility.
   * @param messageSupplier a function that will computed the message to log.
   */
  default void debug(Supplier<String> messageSupplier) {
    if (isLoggable(Level.DEBUG)) {
      log(Level.DEBUG, messageSupplier.get(), null, null);
    }
  }
}
