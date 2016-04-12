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

import org.silverpeas.core.io.media.video.ThumbnailPeriod;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.time.TimeData;

import java.io.File;

/**
 * This class extract 5 thumbnails from a video file using FFmpeg
 * @author ebonnet
 */
public class FFmpegThumbnailExtractor implements VideoThumbnailExtractor {

  @Override
  public boolean isActivated() {
    return FFmpegUtil.isActivated();
  }

  @Override
  public void generateThumbnailsFrom(File video) {
    if (video.exists() && video.isFile()) {
      MetaData metadata = MetadataExtractor.get().extractMetadata(video);
      TimeData timeData = metadata.getDuration();
      if (timeData != null) {
        File thumbnailDir = video.getParentFile();
        for (ThumbnailPeriod thumbPeriod : ThumbnailPeriod.ALL_VALIDS) {
          double timePeriod = thumbPeriod.getPercent() * timeData.getTimeAsLong() / 1000;
          FFmpegUtil.extractVideoThumbnail(video,
              new File(thumbnailDir, thumbPeriod.getFilename()), (int) timePeriod);
        }
      } else {
        SilverLogger.getLogger(this)
            .warn("Problem to retrieve video duration, process video thumbnails has failed");
      }
    }
  }

  private FFmpegThumbnailExtractor() {
  }
}
