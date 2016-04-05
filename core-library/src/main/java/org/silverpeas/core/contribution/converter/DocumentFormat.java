/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.converter;

import org.silverpeas.core.util.MimeTypes;

/**
 * Enumeration of the different formats of documents supported in Silverpeas.
 */
public enum DocumentFormat {

  /**
   * The Portable Document Format aka PDF. ISO 32000-1:2008 standard format.
   */
  pdf(MimeTypes.PDF_MIME_TYPE),
  /**
   * The MS-Word 97/2000/XP format.
   */
  doc(MimeTypes.WORD_MIME_TYPE),
  /**
   * The Microsoft's Rich Text Format aka RTF.
   */
  rtf(MimeTypes.RTF_MIME_TYPE),
  /**
   * The OpenDocument format for text. ISO 26300:2006 standard format.
   */
  odt(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT),
  /**
   * The HTML format.
   */
  html(MimeTypes.HTML_MIME_TYPE);

  /**
   * A helper method to improve readability in method calls with a document format as argument.
   * @param format a document format.
   * @return the document format passed as argument.
   */
  public static DocumentFormat inFormat(final DocumentFormat format) {
    return format;
  }

  /**
   * A helper method to both improve the readability in method calls and to encode a string
   * representation of a document format into the corresponding instance.
   * @param format the format of the document.
   * @return an instance of DocumentFormat corresponding to the specified format.
   */
  public static DocumentFormat inFormat(String format) {
    return DocumentFormat.valueOf(format);
  }

  /**
   * Gets the MIME type corrsponding to this document format.
   * @return the MIME type of this document format.
   */
  public String getMimeType() {
    return this.mimeType;
  }

  private DocumentFormat(final String mimeType) {
    this.mimeType = mimeType;
  }

  private String mimeType;
}
