/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apache.tika.msoffice;

import com.stratelia.webactiv.util.DateUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.Property;

/**
 * Outlook Message Parser.
 */
class SummaryExtractor {

  private static final String SUMMARY_INFORMATION =
      SummaryInformation.DEFAULT_STREAM_NAME;

  private static final String DOCUMENT_SUMMARY_INFORMATION =
      DocumentSummaryInformation.DEFAULT_STREAM_NAME;

  private final Metadata metadata;

  public SummaryExtractor(Metadata metadata) {
    this.metadata = metadata;
  }

  public void parseSummaries(POIFSFileSystem filesystem)
      throws IOException, TikaException {
    parseSummaryEntryIfExists(filesystem, SUMMARY_INFORMATION);
    parseSummaryEntryIfExists(filesystem, DOCUMENT_SUMMARY_INFORMATION);
  }

  private void parseSummaryEntryIfExists(
      POIFSFileSystem filesystem, String entryName)
      throws IOException, TikaException {
    try {
      DocumentEntry entry =
          (DocumentEntry) filesystem.getRoot().getEntry(entryName);
      PropertySet properties =
          new PropertySet(new DocumentInputStream(entry));
      if (properties.isSummaryInformation()) {
        parse(new SummaryInformation(properties));
      }
      if (properties.isDocumentSummaryInformation()) {
        parse(new DocumentSummaryInformation(properties));
      }
    } catch (FileNotFoundException e) {
      // entry does not exist, just skip it
    } catch (NoPropertySetStreamException e) {
      throw new TikaException("Not a HPSF document", e);
    } catch (UnexpectedPropertySetTypeException e) {
      throw new TikaException("Unexpected HPSF document", e);
    } catch (MarkUnsupportedException e) {
      throw new TikaException("Invalid DocumentInputStream", e);
    }
  }

  private void parse(SummaryInformation summary) {
    set(Metadata.TITLE, summary.getTitle());
    set(Metadata.AUTHOR, summary.getAuthor());
    set(Metadata.KEYWORDS, summary.getKeywords());
    set(Metadata.SUBJECT, summary.getSubject());
    set(Metadata.LAST_AUTHOR, summary.getLastAuthor());
    set(Metadata.COMMENTS, summary.getComments());
    set(Metadata.TEMPLATE, summary.getTemplate());
    set(Metadata.APPLICATION_NAME, summary.getApplicationName());
    set(Metadata.REVISION_NUMBER, summary.getRevNumber());
    set(Metadata.CREATION_DATE, summary.getCreateDateTime());
    set(Metadata.CHARACTER_COUNT, summary.getCharCount());
    set(Metadata.EDIT_TIME, summary.getEditTime());
    set(Metadata.LAST_SAVED, summary.getLastSaveDateTime());
    set(Metadata.PAGE_COUNT, summary.getPageCount());
    if (summary.getPageCount() > 0) {
      metadata.set(PagedText.N_PAGES, summary.getPageCount());
    }
    set(Metadata.SECURITY, summary.getSecurity());
    set(Metadata.WORD_COUNT, summary.getWordCount());
    set(Metadata.LAST_PRINTED, summary.getLastPrinted());
  }

  private void parse(DocumentSummaryInformation summary) {
    set(Metadata.COMPANY, summary.getCompany());
    set(Metadata.MANAGER, summary.getManager());
    set(Metadata.LANGUAGE, getLanguage(summary));
    set(Metadata.CATEGORY, summary.getCategory());
    set(Metadata.SLIDE_COUNT, summary.getSlideCount());
    if (summary.getSlideCount() > 0) {
      metadata.set(PagedText.N_PAGES, summary.getSlideCount());
    }
  }

  private String getLanguage(DocumentSummaryInformation summary) {
    CustomProperties customProperties = summary.getCustomProperties();
    if (customProperties != null) {
      Object value = customProperties.get("Language");
      if (value instanceof String) {
        return (String) value;
      }
    }
    return null;
  }

  private void set(String name, String value) {
    if (value != null) {
      metadata.set(name, value);
    }
  }

  private void set(Property property, Date value) {
    if (value != null) {
      metadata.set(property, DateUtil.ISO8601_FORMATTER.format(value));
    }
  }

  private void set(String name, Date value) {
    if (value != null) {
      metadata.set(name, DateUtil.ISO8601_FORMATTER.format(value));
    }
  }

  private void set(String name, long value) {
    if (value > 0) {
      metadata.set(name, Long.toString(value));
    }
  }

}
