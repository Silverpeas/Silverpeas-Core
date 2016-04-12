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

import java.util.function.Supplier;

/**
 * The custom logger for Silverpeas. It abstracts the concrete logging mechanism that will be used
 * in runtime and it provides additional capabilities for the specific use in Silverpeas.
 * </p>
 * It is an adapter to a concrete underlying logging backend to use to log messages. The logging
 * backend can be the logging subsystem of the used application server or a tiers logging framework.
 * With the adapter it can then be easy to switch from an implementation to the other without
 * impacting the logging use in the code of Silverpeas.
 * </p>
 * An implementation of this interface must be a wrapper of a concrete logging mechanism and
 * instances of the implementation must be manufactured by the
 * {@link org.silverpeas.core.util.logging.SilverLoggerFactory} factory.
 * @author miguel
 */
public interface SilverLogger {

  /**
   * The namespace of the root logger in Silverpeas. This namespace is predefined and each logger
   * taken in charge by the Logging Engine should be a descendant of its logger.
   */
  String ROOT_NAMESPACE = "silverpeas";

  /**
   * Gets the logger that is defined for the specified logging namespace.
   * </p>
   * A logging namespace is the name or a category under which the messages will be log. The
   * messages will be logged only if the logging level satisfies the logger's level. If no level is
   * set for the logger, then the level from its nearest ancestor with a specific
   * (non-null) level value is taken into account.
   * </p>
   * The logger instance is obtained from the logging namespace by using the
   * {@link org.silverpeas.core.util.logging.SilverLoggerFactory} factory. The factory
   * has also the responsibility of the initialization mechanism of the logger from its logging
   * configuration parameters before returning it.
   * @return a logger instance, manufactured by a SilverLoggerFactory instance.
   */
  static SilverLogger getLogger(String module) {
    LoggerConfiguration configuration =
        LoggerConfigurationManager.get().getLoggerConfiguration(module);
    return SilverLoggerFactory.get().getLogger(configuration.getNamespace(), configuration);
  }

  /**
   * Gets a logger for the specified object. The logger is found from the package name of the
   * specified object.
   * </p>
   * In Silverpeas, each logger namespace matches a package name, starting from the
   * <code>silverpeas</code> subpackage: for a package <code>org.silverpeas.core.io</code>, there
   * is a logger with the namespace <code>silverpeas.core.io</code>.
   * </p>
   * @see SilverLogger#getLogger(String)
   * @return a logger instance, manufactured by a SilverLoggerFactory instance.
   */
  static SilverLogger getLogger(Object object) {
    Package pkg = (object instanceof Class ? ((Class) object).getPackage() :
        object.getClass().getPackage());
    String namespace = pkg.getName();
    if (namespace.startsWith("org.silverpeas")) {
      namespace = namespace.substring(namespace.indexOf('.') + 1);
    }
    Logger annotation = pkg.getAnnotation(Logger.class);
    if (annotation != null) {
      namespace = annotation.value();
    }
    return getLogger(namespace);
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
