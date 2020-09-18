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
package org.silverpeas.core.viewer.service;

import org.apache.commons.exec.CommandLine;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecution.Config;
import org.silverpeas.core.initialization.Initialization;

import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@Service
public class JsonPdfToolManager implements Initialization {

  private static boolean isActivated = false;

  @Override
  public void init() throws Exception {

    // pdf2json settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equals(entry.getKey().toLowerCase())) {
        try {
          CommandLine commandLine = new CommandLine("pdf2json");
          commandLine.addArgument("-v");
          ExternalExecution.exec(commandLine,
              Config.init().successfulExitStatusValueIs(1).doNotDisplayErrorTrace());
          isActivated = true;
        } catch (final Exception e) {
          // pdf2json is not installed
          System.err.println("pdf2json is not installed");
        }
      }
    }
  }

  /**
   * Indicates if im4java is activated
   * @return
   */
  public static boolean isActivated() {
    return isActivated;
  }
}
