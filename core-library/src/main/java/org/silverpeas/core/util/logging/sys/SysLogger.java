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
package org.silverpeas.core.util.logging.sys;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;

import java.text.MessageFormat;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * This logger implementation uses the Java logging system to log actually the messages.
 * @author miguel
 */
public class SysLogger implements SilverLogger {

  private static final Logger ROOT_LOGGER = Logger.getLogger(ROOT_NAMESPACE);

  private final Logger logger;
  private volatile SilverLogger parent; // to keep strong ref to the parent and hence its config
                                        // with logging level and handlers

  private static SilverLogger getLoggerByNamespace(String namespace) {
    return SilverLoggerFactory.get().getLogger(namespace);
  }

  protected SysLogger(String namespace) {
    this.logger = Logger.getLogger(namespace);
    if (this.logger.getParent() != null && !namespace.equals(ROOT_NAMESPACE)) {
      this.parent = getLoggerByNamespace(this.logger.getParent().getName());
    }
  }

  /**
   * Gets the namespace of this logger. Each logger is defined by a unique dot separated namespace.
   * @return the namespace of this logger.
   */
  @Override
  public String getNamespace() {
    return this.logger.getName();
  }

  /**
   * Gets the log Level of this Logger. If no level was explicitly set for his logger, its
   * effective level is then inherited from its parent.
   * @return the level of this logger.
   */
  @Override
  public Level getLevel() {
    java.util.logging.Level level = this.logger.getLevel();
    if (level == null && this.parent != null) {
      return this.parent.getLevel();
    }
    if (level == java.util.logging.Level.FINE || level == java.util.logging.Level.FINEST ||
        level == java.util.logging.Level.FINER) {
      return Level.DEBUG;
    } else if (level == java.util.logging.Level.WARNING) {
      return Level.WARNING;
    } else if (level == java.util.logging.Level.SEVERE) {
      return Level.ERROR;
    } else {
      return Level.INFO;
    }
  }

  /**
   * Sets the log level specifying which message levels will be logged by this logger. Message
   * levels lower than this value will be discarded.
   * If the new level is null, it means that this node should inherit its level from its nearest
   * ancestor with a specific (non-null) level value.
   * @param level the new value for the log level (may be null).
   */
  @Override
  public void setLevel(final Level level) {
    this.logger.setLevel(fromLoggingLevel(level));
    if (!this.getNamespace().equals(ROOT_NAMESPACE)) {
      if (level == null && this.logger.getHandlers().length > 0) {
        this.logger.setUseParentHandlers(true);
        Handler[] silverpeasRootHandlers = ROOT_LOGGER.getHandlers();
        for (Handler handler : silverpeasRootHandlers) {
          this.logger.removeHandler(handler);
        }
      } else if (level != null && this.logger.getHandlers().length == 0) {
        this.logger.setUseParentHandlers(false);
        Handler[] silverpeasRootHandlers = ROOT_LOGGER.getHandlers();
        for (Handler handler : silverpeasRootHandlers) {
          this.logger.addHandler(handler);
        }
      }
    }
  }

  /**
   * Checks if a message of the given level would actually be logged by this logger. This check is
   * based on the Loggers effective level, which may be inherited from its parent.
   * @param level a logging level.
   * @return true if a message of the given level would actually be logged. False otherwise.
   */
  @Override
  public boolean isLoggable(final Level level) {
    return this.logger.isLoggable(fromLoggingLevel(level));
  }

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
  @Override
  public void log(final Level level, final String message, final Object[] parameters,
      final Throwable error) {
    String text = (parameters != null && parameters.length > 0 ?
        MessageFormat.format(message, parameters) : message);
    this.logger.log(fromLoggingLevel(level), text, error);
  }

  /**
   * Logs a message at the specified level, with the specified parameters if any.
   * @param level the level of the message to log.
   * @param message the message to log.
   * @param parameters zero, one or more parameters to the message.
   */
  @Override
  public void log(final Level level, final String message, final Object... parameters) {
    this.logger.log(fromLoggingLevel(level), message, parameters);
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
  @Override
  public void debug(final Supplier<String> messageSupplier) {
    this.logger.log(fromLoggingLevel(Level.DEBUG), messageSupplier);
  }

  private java.util.logging.Level fromLoggingLevel(Level level) {
    java.util.logging.Level sysLevel = null;
    if (level != null) {
      switch (level) {
        case INFO:
          sysLevel = java.util.logging.Level.INFO;
          break;
        case DEBUG:
          sysLevel = java.util.logging.Level.FINE;
          break;
        case WARNING:
          sysLevel = java.util.logging.Level.WARNING;
          break;
        case ERROR:
          sysLevel = java.util.logging.Level.SEVERE;
          break;
      }
    }
    return sysLevel;
  }
}
