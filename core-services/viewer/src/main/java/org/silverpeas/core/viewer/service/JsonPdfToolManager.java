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
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecution.Config;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.Map;

/**
 * A manager of the PDF to JSON toolkit. Its aim is to check the pdf2json tool is installed on
 * the host and it works fine in order to be used by the Viewer service of Silverpeas.
 * <p>
 * The goal of the tool is to split PDF files into fragments in JSON, ready to be processed by
 * the viwer service.
 * </p>
 * @author Yohann Chastagnier
 */
@Technical
@Service
@Singleton
public class JsonPdfToolManager implements Initialization {

  public static JsonPdfToolManager get() {
    return ServiceProvider.getSingleton(JsonPdfToolManager.class);
  }

  private boolean isActivated = false;

  @Override
  public void init() throws Exception {

    // pdf2json settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equalsIgnoreCase(entry.getKey())) {
        try {
          CommandLine commandLine = new CommandLine("pdf2json");
          commandLine.addArgument("-v");
          ExternalExecution.exec(commandLine,
              Config.init().successfulExitStatusValueIs(1).doNotDisplayErrorTrace());
          isActivated = true;
        } catch (final Exception e) {
          // pdf2json is not installed
          SilverLogger.getLogger(this).error("pdf2json is not installed");
        }
      }
    }
  }

  /**
   * If the PDF to JSON conversion activated?
   * @return true if the PDF to JSON toolkit is installed and works fine on the host. False
   * otherwise.
   */
  public boolean isActivated() {
    return isActivated;
  }
}
