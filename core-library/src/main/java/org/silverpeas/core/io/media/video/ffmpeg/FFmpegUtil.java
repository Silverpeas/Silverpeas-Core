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
import org.silverpeas.core.io.media.video.VideoThumbnailExtractorException;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecutionException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class wrapp FFmpeg command line execution.
 *
 * <pre>
 * ffmpeg -ss $i*$interval -i /path/to/video.mov -vframes 1 -filter:v scale="600:-1" /path/to/thumbnail_$i.jpg
 * </pre>
 * @author ebonnet
 */
public class FFmpegUtil extends ExternalExecution {

  public static boolean isActivated() {
    return FFmpegToolManager.isActivated();
  }

  static CommandLine buildFFmpegThumbnailExtractorCommandLine(File inputFile, File outputFile,
      int seconds) {
    Map<String, File> files = new HashMap<>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("ffmpeg");
    // Time of extract in seconds
    commandLine.addArgument("-ss", false);
    commandLine.addArgument(Integer.toString(seconds), false);
    commandLine.addArgument("-i", false);
    commandLine.addArgument("${inputFile}", false);
    // Only one frame
    commandLine.addArgument("-vframes", false);
    commandLine.addArgument("1", false);
    // Resize/scale of output picture keeping aspect ratio
    commandLine.addArgument("-vf", false);
    commandLine.addArgument("scale=600:-1", false);
    commandLine.addArgument("${outputFile}", false);
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }

  public static List<String> extractVideoThumbnail(File videoFile, File outputFile, int time) {
    CommandLine cmd = buildFFmpegThumbnailExtractorCommandLine(videoFile, outputFile, time);
    try {
      return exec(cmd);
    } catch (ExternalExecutionException e) {
      throw new VideoThumbnailExtractorException(e);
    }
  }
}
