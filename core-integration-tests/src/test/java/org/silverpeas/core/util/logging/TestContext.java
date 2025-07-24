/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

import org.silverpeas.kernel.logging.Level;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.silverpeas.kernel.logging.SilverLoggerProvider.ROOT_NAMESPACE;

/**
 * Context for the tests on the logging in Silverpeas
 */
public class TestContext {

  public void setLoggerLevel(Level level) {
    final ConsoleHandler handler = new ConsoleHandler();
    setLoggerHandler(handler);
    handler.setFormatter(new SimpleFormatter());
    switch (level) {
      case INFO:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.INFO);
        handler.setLevel(java.util.logging.Level.INFO);
        break;
      case DEBUG:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.FINE);
        handler.setLevel(java.util.logging.Level.FINE);
        break;
      case WARNING:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.WARNING);
        handler.setLevel(java.util.logging.Level.WARNING);
        break;
      case ERROR:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.SEVERE);
        handler.setLevel(java.util.logging.Level.SEVERE);
        break;
    }
  }

  private void setLoggerHandler(final Handler handler) {
    java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setUseParentHandlers(false);
    if (Arrays.stream(java.util.logging.Logger.getLogger(ROOT_NAMESPACE).getHandlers())
        .noneMatch(h -> handler.getClass().isInstance(h))) {
      Logger.getLogger(ROOT_NAMESPACE).addHandler(handler);
    }
  }
}
