/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataExtractor {

  private MetadataExtractor() {
  }

  private static final Pattern VIDEO_ADDITIONAL_METADATA_PATTERN =
      Pattern.compile("(?i)/[x\\-ms]*(m4v|mp4|quicktime)$");


  /**
   * Return Metadata of a document.
   * @param fileName
   * @return Metadata
   */
  public MetaData extractMetadata(String fileName) {
    return extractMetadata(new File(fileName));
  }

  public MetaData extractMetadata(File file) {
    InputStream inputStream = null;
    try {
      Metadata metadata = new Metadata();
      inputStream = TikaInputStream.get(file, metadata);
      new Tika().parse(inputStream, metadata).close();
      additionalExtractions(file, metadata);
      return new MetaData(file, metadata);
    } catch (IOException ex) {
      SilverTrace.warn("MetadataExtractor.getMetadata()", "SilverpeasException.WARNING",
          "util.EXE_CANT_GET_SUMMARY_INFORMATION" + ex.getMessage(), ex);
      return new MetaData(file, new Metadata());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Additional extractions. After each tika upgrade, please verify if this treatment is necessary.
   * Only apply for mp4 or quicktime video (mp4|quicktime).
   * @param file
   * @param metadata
   */
  private void additionalExtractions(File file, Metadata metadata) throws IOException {
    String contentType = metadata.get(Metadata.CONTENT_TYPE);
    Matcher videoMatcher = VIDEO_ADDITIONAL_METADATA_PATTERN.matcher(contentType);
    if (videoMatcher.find()) {

      // The technique is taken from MP4Parser implementation
      IsoFile isoFile;
      TikaInputStream tstream = null;
      try {
        tstream = TikaInputStream.get(file, metadata);
        isoFile = new IsoFile(tstream.getFileChannel());
      } finally {
        IOUtils.closeQuietly(tstream);
      }

      // For DEBUG
      // Map<String, List<Box>> filledBoxes = new HashMap<String, List<Box>>();
      // getFilledBoxes(isoFile.getBoxes(), filledBoxes);

      MovieBox movieBox = isoFile.getMovieBox();
      if (movieBox != null) {
        MovieHeaderBox movieHeaderBox = movieBox.getMovieHeaderBox();
        if (movieHeaderBox != null) {
          computeMp4Duration(metadata, movieHeaderBox);
          computeMp4Dimension(metadata, movieBox);
        }
      }
    }
  }

  private void computeMp4Duration(Metadata metadata, MovieHeaderBox movieHeaderBox) {
    BigDecimal duration = BigDecimal.valueOf(movieHeaderBox.getDuration());
    if (duration.intValue() > 0) {
      BigDecimal divisor = BigDecimal.valueOf(movieHeaderBox.getTimescale());

      // Duration
      duration = duration.divide(divisor, 10, BigDecimal.ROUND_HALF_DOWN);
      // get duration in ms
      duration = duration.multiply(BigDecimal.valueOf(1000));
      metadata.add(XMPDM.DURATION, duration.toString());
    }
  }

  private void computeMp4Dimension(Metadata metadata, MovieBox movieBox) {
    // If duration is set, it exists a TrackBox with right width and height definition.
    List<TrackBox> trackBoxes = movieBox.getBoxes(TrackBox.class);
    if (trackBoxes.size() > 0) {
      TrackHeaderBox trackHeader = null;
      for (TrackBox trackBox : trackBoxes) {
        boolean isWidthExisting = trackBox.getTrackHeaderBox().getWidth() > 0;
        if (isWidthExisting || trackHeader == null) {
          trackHeader = trackBox.getTrackHeaderBox();
          if (isWidthExisting) {
            break;
          }
        }
      }
      if (trackHeader != null) {
        metadata.set(Metadata.IMAGE_WIDTH, (int) trackHeader.getWidth());
        metadata.set(Metadata.IMAGE_LENGTH, (int) trackHeader.getHeight());
      }
    }
  }

  /**
   * For now, this method is just for MP4 media debug...
   * ...so please do not delete this method.
   * @param boxes
   * @param filledBoxes
   */
  private Map<String, List<Box>> getFilledBoxes(List<Box> boxes,
      Map<String, List<Box>> filledBoxes) {
    Map<String, List<Box>> result =
        (filledBoxes != null) ? filledBoxes : new HashMap<String, List<Box>>();
    if (CollectionUtil.isNotEmpty(boxes)) {
      for (Box box : boxes) {
        if (box instanceof ContainerBox) {
          getFilledBoxes(((ContainerBox) box).getBoxes(), result);
        } else {
          MapUtil.putAddList(result, box.getType(), box);
        }
      }
    }
    return result;
  }
}
