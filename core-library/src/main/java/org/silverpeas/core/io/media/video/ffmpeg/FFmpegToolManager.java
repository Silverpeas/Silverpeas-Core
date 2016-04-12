/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.video.ffmpeg;

import org.apache.commons.exec.CommandLine;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecution.Config;
import org.silverpeas.core.initialization.Initialization;

import java.util.Map;

public class FFmpegToolManager implements Initialization {

  private static boolean isActivated = false;

  @Override
  public void init() throws Exception {

    // SwfTools settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equals(entry.getKey().toLowerCase())) {
        try {
          CommandLine commandLine = new CommandLine("ffmpeg");
          commandLine.addArgument("-version");
          ExternalExecution.exec(commandLine, Config.init().doNotDisplayErrorTrace());
          isActivated = true;
        } catch (final Exception e) {
          // FFmpeg is not installed
          System.err.println("ffmpeg is not installed");
        }
      }
    }
  }

  /**
   * Indicates if ffmpeg is actived
   * @return
   */
  public static boolean isActivated() {
    return isActivated;
  }

}
