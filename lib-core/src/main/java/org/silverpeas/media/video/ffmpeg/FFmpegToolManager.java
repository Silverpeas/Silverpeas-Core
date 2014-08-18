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
package org.silverpeas.media.video.ffmpeg;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("ffmpegToolManager")
@Singleton
public class FFmpegToolManager {

  private static boolean isActivated = false;

  @PostConstruct
  public void initialize() throws Exception {

    // SwfTools settings
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      if ("path".equals(entry.getKey().toLowerCase())) {
        Process process = null;
        try {
          final StringBuilder ffmpegCommand = new StringBuilder();
          ffmpegCommand.append("ffmpeg -version");
          process = Runtime.getRuntime().exec(ffmpegCommand.toString());
          isActivated = true;
        } catch (final Exception e) {
          // FFmpeg is not installed
        } finally {
          if (process != null) {
            process.destroy();
          }
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
