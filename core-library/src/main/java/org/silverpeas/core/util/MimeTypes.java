/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util;

import org.silverpeas.core.util.file.FileUtil;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.file.FileUtil.getMimeType;

public class MimeTypes {

  public static final String SERVLET_HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
  public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  public static final String RTF_MIME_TYPE = "application/rtf";
  public static final String PDF_MIME_TYPE = "application/pdf";
  public static final String WORD_MIME_TYPE = "application/msword";
  public static final String EXCEL_MIME_TYPE1 = "application/x-msexcel";
  public static final String EXCEL_MIME_TYPE2 = "application/vnd.ms-excel";
  public static final String POWERPOINT_MIME_TYPE1 = "application/powerpoint";
  public static final String POWERPOINT_MIME_TYPE2 = "application/vnd.ms-powerpoint";
  public static final String SPINFIRE_MIME_TYPE = "application/xview3d-3d";
  public static final String HTML_MIME_TYPE = "text/html";
  public static final String XML_MIME_TYPE = "text/xml";
  public static final String XHTML_MIME_TYPE = "application/xhtml+xml";
  public static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
  public static final String ARCHIVE_MIME_TYPE = "application/x-zip-compressed";
  public static final String SHORT_ARCHIVE_MIME_TYPE = "application/zip";
  public static final String JAVA_ARCHIVE_MIME_TYPE = "application/java-archive";
  public static final String JSP_MIME_TYPE = "text/x-jsp";
  public static final String PHP_MIME_TYPE = "text/x-php";
  public static final String BZ2_ARCHIVE_MIME_TYPE = "application/x-bzip";
  public static final String GZ_ARCHIVE_MIME_TYPE = "application/x-gzip";
  public static final String GUNZIP_ARCHIVE_MIME_TYPE = "application/x-gunzip";
  public static final String TARGZ_ARCHIVE_MIME_TYPE = "application/x-tar-gz";
  public static final String WORD_2007_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  public static final String WORD_2007_TEMPLATE_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
  public static final String WORD_2007_EXTENSION = "application/vnd.ms-word";
  public static final String EXCEL_2007_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static final String EXCEL_2007_TEMPLATE_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
  public static final String EXCEL_2007_EXTENSION = "application/vnd.ms-excel";
  public static final String POWERPOINT_2007_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.presentationml.presentation";
  public static final String POWERPOINT_2007_TEMPLATE_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.presentationml.template";
  public static final String POWERPOINT_2007_EXTENSION = "application/vnd.ms-powerpoint";
  public static final String RSS_MIME_TYPE = "application/rss+xml";
  public static final String JAR_EXTENSION = "jar";
  public static final String WAR_EXTENSION = "war";
  public static final String EAR_EXTENSION = "ear";
  public static final String JSP_EXTENSION = "jsp";
  public static final String PHP_EXTENSION = "php";
  // Extension .odt (Texte)
  public static final String MIME_TYPE_OO_FORMATTED_TEXT =
      "application/vnd.oasis.opendocument.text";
  // Extension .ods (Tableur)
  public static final String MIME_TYPE_OO_SPREADSHEET =
      "application/vnd.oasis.opendocument.spreadsheet";
  // Extension .odp (Presentation)
  public static final String MIME_TYPE_OO_PRESENTATION =
      "application/vnd.oasis.opendocument.presentation";
  // Extension .odg (Dessin)
  public static final String MIME_TYPE_OO_GRAPHICS = "application/vnd.oasis.opendocument.graphics";
  // Extension .odc (Diagramme)
  public static final String MIME_TYPE_OO_DIAGRAM = "application/vnd.oasis.opendocument.chart";
  // Extension .odf (Formule)
  public static final String MIME_TYPE_OO_FORMULA = "application/vnd.oasis.opendocument.formula";
  // Extension .odb (Base de donnees )
  public static final String MIME_TYPE_OO_DB = "application/vnd.oasis.opendocument.database";
  // Extension .odi (Image)
  public static final String MIME_TYPE_OO_IMAGE = "application/vnd.oasis.opendocument.image";
  // Extension .odm (Document principal)
  public static final String MIME_TYPE_OO_MASTER = "application/vnd.oasis.opendocument.text-master";
  //Extension .mpp and .mpt
  public static final String MSPROJECT_MIME_TYPE = "application/vnd.ms-project";
  public static final Set<String> OPEN_OFFICE_MIME_TYPES = Collections.unmodifiableSet(Stream.of(
        WORD_MIME_TYPE, WORD_2007_MIME_TYPE, WORD_2007_TEMPLATE_MIME_TYPE,
        EXCEL_MIME_TYPE1, EXCEL_MIME_TYPE2, EXCEL_2007_MIME_TYPE, EXCEL_2007_TEMPLATE_MIME_TYPE,
        POWERPOINT_MIME_TYPE1, POWERPOINT_MIME_TYPE2, POWERPOINT_2007_MIME_TYPE,
        POWERPOINT_2007_TEMPLATE_MIME_TYPE, MIME_TYPE_OO_FORMATTED_TEXT, MIME_TYPE_OO_SPREADSHEET,
        MIME_TYPE_OO_PRESENTATION, MIME_TYPE_OO_GRAPHICS, MIME_TYPE_OO_DIAGRAM,
        MIME_TYPE_OO_FORMULA, MIME_TYPE_OO_DB, MIME_TYPE_OO_IMAGE, MIME_TYPE_OO_MASTER,
        getMimeType(".ott"), getMimeType(".ots"), getMimeType(".otp"))
      .collect(Collectors.toSet()));
  public static final Set<String> ARCHIVE_MIME_TYPES = Collections.unmodifiableSet(Stream.of(
        BZ2_ARCHIVE_MIME_TYPE, GZ_ARCHIVE_MIME_TYPE, GUNZIP_ARCHIVE_MIME_TYPE,
        TARGZ_ARCHIVE_MIME_TYPE, ARCHIVE_MIME_TYPE, SHORT_ARCHIVE_MIME_TYPE,
        JAVA_ARCHIVE_MIME_TYPE)
      .collect(Collectors.toSet()));
  private MimeTypes() {
  }

  /**
   * Handles registry of mime type which is provided by different kind of sources.
   * <p>
   * To initialize the registry, a supplier of document extensions and default mime types are
   * required.
   * <ul>
   *   <li>If the supplier provides the value {@code deactivated}, then the registry is
   *   purged and contains no mime-type,</li>
   *   <li>If the supplier provides an empty value (or a not defined value), then the registry is
   *   loaded with the default given mime types,</li>
   *   <li>If the supplier provides a list of document extensions, then the registry is loaded
   *   with them and default given mime types are ignored,</li>
   *   <li>If the supplier provides a list with the prefix {@code (+)}, then the registry is
   *   loaded with them and also with the given default mime types.</li>
   * </ul>
   * </p>
   */
  public static class MimeTypeRegistry {
    static final String ADDITIONAL_PREFIX_CLAUSE = "(+)";
    private static final String DEACTIVATED = "deactivated";
    private static final Object OFFICE_MUTEX = new Object();
    private final Supplier<String> extensionProvider;
    private final Set<String> defaultMimeTypes;
    protected Set<String> currentMimeTypes = Collections.emptySet();
    private String currentExtensions = null;

    public MimeTypeRegistry(final Supplier<String> extensionProvider,
        final Set<String> defaultMimeTypes) {
      this.extensionProvider = extensionProvider;
      this.defaultMimeTypes = defaultMimeTypes;
    }

    public boolean contains(final String mimeType) {
      verifyAvailableMimeTypes();
      return currentMimeTypes.contains(mimeType.toLowerCase());
    }

    private void verifyAvailableMimeTypes() {
      final String extensions = extensionProvider.get().trim();
      if (currentExtensions == null || !currentExtensions.equals(extensions)) {
        synchronized (OFFICE_MUTEX) {
          if (DEACTIVATED.equals(extensions)) {
            currentMimeTypes = Collections.emptySet();
          } else {
            final boolean addToDefaults = extensions.startsWith(ADDITIONAL_PREFIX_CLAUSE);
            final String values = addToDefaults
                ? extensions.substring(ADDITIONAL_PREFIX_CLAUSE.length())
                : extensions;
            currentMimeTypes = Stream.of(values.replace(" ", "").split("[,;]"))
                .filter(e -> !e.isEmpty())
                .map(FileUtil::getMimeType)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            if (currentMimeTypes.isEmpty()) {
              currentMimeTypes = defaultMimeTypes.stream()
                  .map(String::toLowerCase)
                  .collect(Collectors.toSet());
            } else if (values.length() != extensions.length()) {
              currentMimeTypes = Stream
                  .concat(currentMimeTypes.stream(), defaultMimeTypes.stream().map(String::toLowerCase))
                  .collect(Collectors.toSet());
            }
          }
        }
        currentExtensions = extensions;
      }
    }
  }
}
