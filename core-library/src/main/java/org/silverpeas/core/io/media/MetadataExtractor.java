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
package org.silverpeas.core.io.media;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.time.Duration;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;

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

  MetadataExtractor() {
  }

  @PostConstruct
  protected void initialize() {
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
    return apply(m -> getTikaService().parse(file, m));
  }

  /**
   * Extracts Metadata of a content provided by an input stream on the content of a document.
   * @param content an input stream on the content of a document.
   * @return a {@link MetaData} instance that handles the metadata extracted from the given document content. .
   */
  @SuppressWarnings("unused")
  public MetaData extractMetadata(InputStream content) {
    return apply(m -> getTikaService().parse(content, m));
  }

  private MetaData apply(MetadataFetcher fetcher) {
    Metadata metadata = new Metadata();
    try (@SuppressWarnings("unused") Reader reader = fetcher.fetch(metadata)) {
      return adjust(metadata);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).warn(ex);
      return new MetaData(new Metadata());
    }
  }

  /**
   * Adjusts the the given metadata by adding new ones or modifying extracted ones.
   * @param metaData the extracted metadata to adjust if necessary.
   * @return a {@link MetaData} instance that handles the metadata extracted from the given file.
   */
  private MetaData adjust(Metadata metaData) {
    adjustMediaDuration(metaData);
    return new MetaData(metaData);
  }


  /**
   * Adjusts media (sound or video) extracted duration.<br>
   * Indeed the sound and video parsers puts into metadata the duration in seconds whereas we are
   * expected the duration in milliseconds.
   * @param metadata the current extracted metadata.
   */
  private void adjustMediaDuration(Metadata metadata) {
    String durationData = metadata.get(XMPDM.DURATION);
    if (StringUtil.isDefined(durationData)) {
      try {
        Duration duration =
            UnitUtil.getDuration(new BigDecimal(durationData), TimeUnit.SECOND);
        metadata.set(XMPDM.DURATION, String.valueOf(duration.getTimeAsLong()));
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
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

  @FunctionalInterface
  private interface MetadataFetcher {
    Reader fetch(final Metadata metadata) throws IOException;
  }
}
