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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.silverpeas.exec.ExternalExecution;
import org.silverpeas.exec.ExternalExecutionException;
import org.silverpeas.media.video.VideoThumbnailExtractorException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * This class wrapp FFmpeg command line execution.
 *
 * <pre>
 * ffmpeg -ss $i*$interval -i /path/to/video.mov -vframes 1 /path/to/thumbnail_$i.jpg
 * </pre>
 * @author ebonnet
 */
public class FFmpegUtil extends ExternalExecution {

  public static boolean isActivated() {
    return FFmpegToolManager.isActivated();
  }

  static CommandLine buildFFmpegThumbnailExtractorCommandLine(File inputFile, File outputFile,
      int seconds) {
    Map<String, File> files = new HashMap<String, File>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("ffmpeg");
    commandLine.addArgument("-ss", false);
    commandLine.addArgument(Integer.toString(seconds), false);
    commandLine.addArgument("-i", false);
    commandLine.addArgument("${inputFile}", false);
    commandLine.addArgument("-vframes", false);
    commandLine.addArgument("1", false);
    commandLine.addArgument("${outputFile}", false);
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }

  public static List<String> extractVideoThumbnail(File videoFile, File outputFile, int time) {
    SilverTrace.debug("VideoTool", "FFmpegUtil.extractVideoThumbnail",
        "extract video thumbnail at " + time);
    CommandLine cmd = buildFFmpegThumbnailExtractorCommandLine(videoFile, outputFile, time);
    List<String> result = null;
    try {
      result = exec(cmd);
    } catch (ExternalExecutionException e) {
      throw new VideoThumbnailExtractorException(e);
    }
    return result;
  }

  public static int extractVideoThumb(File videoFile, File outputFile, int time) {
    CommandLine cmd = buildFFmpegThumbnailExtractorCommandLine(videoFile, outputFile, time);
    return execAlternative(cmd);
  }

}
