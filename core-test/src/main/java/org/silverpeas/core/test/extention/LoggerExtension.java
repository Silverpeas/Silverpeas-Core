/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.extention;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.silverpeas.core.util.logging.Level;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.silverpeas.core.util.logging.SilverLoggerProvider.ROOT_NAMESPACE;

/**
 * JUnit 5 extension to take care of the logger levels to set in a given unit test.
 * @author mmoquillon
 */
public class LoggerExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    final LoggerLevel levelForAll =
        context.getRequiredTestInstance().getClass().getAnnotation(LoggerLevel.class);
    if (levelForAll != null) {
      setLoggerLevel(levelForAll.value());
    }
    context.getElement().ifPresent(e -> {
      final LoggerLevel level = e.getAnnotation(LoggerLevel.class);
      if (level != null) {
        setLoggerLevel(level.value());
      }
    });
  }

  private void setLoggerLevel(Level level) {
    final ConsoleHandler handler = new ConsoleHandler();
    setLoggerHandler(handler);
    handler.setFormatter(new SimpleFormatter());
    switch (level) {
      case INFO:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.INFO);
        handler.setLevel(java.util.logging.Level.INFO);
        break;
      case DEBUG:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.FINE);
        handler.setLevel(java.util.logging.Level.FINE);
        break;
      case WARNING:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.WARNING);
        handler.setLevel(java.util.logging.Level.WARNING);
        break;
      case ERROR:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.SEVERE);
        handler.setLevel(java.util.logging.Level.SEVERE);
        break;
    }
  }

  private void setLoggerHandler(final Handler handler) {
    Logger.getLogger(ROOT_NAMESPACE).setUseParentHandlers(false);
    if (Arrays.stream(Logger.getLogger(ROOT_NAMESPACE).getHandlers())
        .filter(h -> handler.getClass().isInstance(h))
        .count() == 0) {
      Logger.getLogger(ROOT_NAMESPACE).addHandler(handler);
    }
  }
}
  