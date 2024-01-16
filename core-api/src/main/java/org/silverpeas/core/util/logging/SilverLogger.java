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
package org.silverpeas.core.util.logging;

import java.util.function.Supplier;

/**
 * The custom logger for Silverpeas. It abstracts the concrete logging mechanism that will be used
 * in runtime and it provides additional capabilities for the specific use in Silverpeas.
 * <p>
 * It is an adapter to a concrete underlying logging backend to use to log messages. The logging
 * backend can be the logging subsystem of the used application server or a tiers logging framework.
 * With the adapter it can then be easy to switch from an implementation to the other without
 * impacting the logging use in the code of Silverpeas.
 * <p>
 * An implementation of this interface must be a wrapper of a concrete logging mechanism and
 * instances of the implementation must be manufactured by the
 * {@link org.silverpeas.core.util.logging.SilverLoggerFactory} factory.
 * @author miguel
 */
public interface SilverLogger {

  /**
   * Gets the logger that is defined for the specified logging namespace. The logger getting is
   * delegated to a {@link SilverLoggerProvider} instance.
   * @param module a logging namespace. It is the name or a category under which the messages will
   * be log.
   * @see SilverLoggerProvider#getLogger(String)
   * @return a logger instance provided by a {@link SilverLoggerProvider} instance.
   */
  static SilverLogger getLogger(String module) {
    return SilverLoggerProvider.getLoggerProvider().getLogger(module);
  }

  /**
   * Gets a logger for the specified object. The logger getting is delegated to a
   * {@link SilverLoggerProvider} instance.
   * @param object the object from which the logging namespace is determined. The logging namespace
   * is found from the package name of the object. In Silverpeas, each logger namespace matches a
   * package name, starting from the * <code>silverpeas</code> subpackage: for a package
   * <code>org.silverpeas.core.io</code>, there is a logger with the namespace
   * <code>silverpeas.core.io</code>.
   * @see SilverLoggerProvider#getLogger(Object)
   * @return a logger instance provided by a {@link SilverLoggerProvider} instance.
   */
  static SilverLogger getLogger(Object object) {
    return SilverLoggerProvider.getLoggerProvider().getLogger(object);
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
   * @param error the cause of the error.
   */
  default void error(Throwable error) {
    log(Level.ERROR, error.getMessage(), new String[0], error);
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
   * Logs a warning message with the specified parameters.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to set to the message
   */
  default void warn(String message, Object... parameters) {
    log(Level.WARNING, message, parameters);
  }

  /**
   * Logs a warning message with the specified parameters.
   * <p> Only the message of the error will be logged.
   * @param error the cause of the error.
   */
  default void warn(Throwable error) {
    log(Level.WARNING, error.getMessage());
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
   * <p>
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

  /**
   * Logs silently the specified error. The message is actually wiped out. This method is mainly
   * dedicated to explicitly indicate the error is expected in some well-defined circumstances; it
   * is a computation branch in the nominal execution flow.
   * <p>
   * Be caution by using this method; it is
   * more efficient to use the if-else condition than the try-catch instruction to perform a
   * default treatment on error cases. This method is to be used when a method marked as throwing
   * an exception is invoked and for which the exception is, in the context of the invocation, can
   * be expected.
   * </p>
   * @param error the error to wipe out.
   * @return the logger itself.
   */
  default SilverLogger silent(Throwable error) {
    // nothing to log
    return this;
  }
}
