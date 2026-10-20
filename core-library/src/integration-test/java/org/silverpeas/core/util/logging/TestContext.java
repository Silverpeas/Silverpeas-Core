/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.logging.SilverLoggerProvider;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.silverpeas.kernel.logging.SilverLoggerProvider.ROOT_NAMESPACE;

/**
 * Context for the tests on the logging in Silverpeas
 */
public class TestContext {

  public void setLoggerLevel(Level level) {
    setConsoleHandler();
    // the level is set first in the configuration of the root logger, otherwise it would be lost
    // at the next initialization of the root logger from its configuration
    SilverLoggerProvider.getInstance()
        .getConfigurationManager()
        .getLoggerConfiguration(ROOT_NAMESPACE)
        .setLevel(level);
    SilverLogger.getLogger(ROOT_NAMESPACE).setLevel(level);
  }

  private void setConsoleHandler() {
    Logger rootLogger = Logger.getLogger(ROOT_NAMESPACE);
    rootLogger.setUseParentHandlers(false);
    if (Arrays.stream(rootLogger.getHandlers()).noneMatch(ConsoleHandler.class::isInstance)) {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setFormatter(new SimpleFormatter());
      // the records are filtered by the level of the root logger
      handler.setLevel(java.util.logging.Level.ALL);
      rootLogger.addHandler(handler);
    }
  }
}
