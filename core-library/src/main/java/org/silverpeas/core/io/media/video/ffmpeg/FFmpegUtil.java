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
      double position) {
    Map<String, File> files = new HashMap<>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("ffmpeg");
    // Time of extract in seconds
    commandLine.addArgument("-ss", false);
    commandLine.addArgument(Double.toString(position), false);
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

  /**
   * Extracts a frame of the specified video at the given position and stores the obtained image
   * into the specified output file. The type of the image is get from the output file extension.
   * @param videoFile the file containing the video.
   * @param outputFile the file into which will be registered the extracted thumbnail.
   * @param position the position of the frame in the video from which a thumbnail will be
   * extracted. Note that in most formats it is not possible to seek exactly, so the extraction
   * will seek to the closest seek point before the given position.
   * @return a {@link List} of console lines written by the image extraction command.
   */
  public static List<String> extractVideoThumbnail(File videoFile, File outputFile,
      double position) {
    CommandLine cmd = buildFFmpegThumbnailExtractorCommandLine(videoFile, outputFile, position);
    try {
      return exec(cmd);
    } catch (ExternalExecutionException e) {
      throw new VideoThumbnailExtractorException(e);
    }
  }
}
