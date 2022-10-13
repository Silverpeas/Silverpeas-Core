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
package org.silverpeas.core.viewer.service;

import org.apache.commons.exec.CommandLine;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecution.Config;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@Service
public class SwfToolManager implements Initialization {

  private static boolean isActivated = false;

  @Override
  public synchronized void init() {

    // SwfTools settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equalsIgnoreCase(entry.getKey())) {
        try {
          CommandLine commandLine = new CommandLine("pdf2swf");
          commandLine.addArgument("--version");
          ExternalExecution.exec(commandLine, Config.init().doNotDisplayErrorTrace());
          isActivated = true;
        } catch (final Exception e) {
          // SwfTool is not installed
          SilverLogger.getLogger(this).warn("pdf2swf is not installed");
        }
      }
    }
  }

  /**
   * Indicates if pdf2swf is activated
   * @return
   */
  public static boolean isActivated() {
    return isActivated;
  }
}
