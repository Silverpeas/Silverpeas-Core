/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.io.media;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.TimeData;
import org.silverpeas.core.util.time.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Set;

/**
 * This tool is kind of interface between the Silverpeas callers which needs to get metadata from
 * files and the API used to extract them.
 */
@Singleton
public class MetadataExtractor {

  private Tika tika;

  public static MetadataExtractor get() {
    return ServiceProvider.getService(MetadataExtractor.class);
  }

  private static Set<MediaType> mp4ParserSupportedTypes =
      new MP4Parser().getSupportedTypes(new ParseContext());

  MetadataExtractor() {
  }

  @PostConstruct
  private void initialize() {
    tika = new Tika();
  }

  /**
   * Extracts Metadata of a file.
   * @param filePath the full path of a file.
   * @return a {@link MetaData} instance that handles the metadata extracted from a file
   * represented by the given full path.
   */
  public MetaData extractMetadata(String filePath) {
    return extractMetadata(new File(filePath));
  }

  /**
   * Extracts Metadata of a file.
   * @param file a file.
   * @return a {@link MetaData} instance that handles the metadata extracted from the given file.
   */
  public MetaData extractMetadata(File file) {
    Metadata metadata = new Metadata();
    try (InputStream inputStream = TikaInputStream.get(file, metadata)) {
      getTikaService().parse(inputStream, metadata).close();
      return adjust(file, metadata);
    } catch (Exception ex) {
      SilverTrace.warn("MetadataExtractor.getMetadata()", "SilverpeasException.WARNING",
          "util.EXE_CANT_GET_SUMMARY_INFORMATION" + ex.getMessage(), ex);
      return new MetaData(file, new Metadata());
    }
  }

  /**
   * Adjusts the the given metadata by adding new ones or modifying extracted ones.
   * @param file the file from which the metadata were extracted.
   * @param metaData the extracted metadata to adjust if necessary.
   * @return a {@link MetaData} instance that handles the metadata extracted from the given file.
   */
  private MetaData adjust(final File file, Metadata metaData) {
    String contentType = metaData.get(Metadata.CONTENT_TYPE);
    MediaType mediaType = MediaType.parse(contentType);
    adjustMp4Duration(metaData, mediaType);
    return new MetaData(file, metaData);
  }


  /**
   * Adjusts MP4 extracted duration.<br/>
   * Indeed {@link MP4Parser} puts into metadata the duration in seconds.<br/>
   * It can not be perfect all the time!
   * @param metadata the current extracted metadata.
   * @param mediaType the mediaType of current processed file.
   */
  private void adjustMp4Duration(Metadata metadata, MediaType mediaType) {
    if (mp4ParserSupportedTypes.contains(mediaType)) {
      try {
        TimeData duration =
            UnitUtil.getTimeData(new BigDecimal(metadata.get(XMPDM.DURATION)), TimeUnit.SEC);
        metadata.set(XMPDM.DURATION, String.valueOf(duration.getTimeAsLong()));
      } catch (Exception ignore) {
      }
    }
  }

  /**
   * Detects the media type of the given file. The type detection is
   * based on the document content and a potential known file extension.
   * @param file the file
   * @return detected media type
   * @throws IOException if the file can not be read
   */
  public String detectMimeType(final File file) throws IOException {
    return getTikaService().detect(file);
  }

  private Tika getTikaService() {
    return tika;
  }
}
