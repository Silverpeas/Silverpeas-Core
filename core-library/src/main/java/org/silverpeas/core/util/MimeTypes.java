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
package org.silverpeas.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface MimeTypes {

  String SERVLET_HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
  String DEFAULT_MIME_TYPE = "application/octet-stream";
  String RTF_MIME_TYPE = "application/rtf";
  String PDF_MIME_TYPE = "application/pdf";
  String WORD_MIME_TYPE = "application/msword";
  String EXCEL_MIME_TYPE1 = "application/x-msexcel";
  String EXCEL_MIME_TYPE2 = "application/vnd.ms-excel";
  String POWERPOINT_MIME_TYPE1 = "application/powerpoint";
  String POWERPOINT_MIME_TYPE2 = "application/vnd.ms-powerpoint";
  String SPINFIRE_MIME_TYPE = "application/xview3d-3d";
  String HTML_MIME_TYPE = "text/html";
  String XML_MIME_TYPE = "text/xml";
  String XHTML_MIME_TYPE = "application/xhtml+xml";
  String PLAIN_TEXT_MIME_TYPE = "text/plain";
  String ARCHIVE_MIME_TYPE = "application/x-zip-compressed";
  String SHORT_ARCHIVE_MIME_TYPE = "application/zip";
  String JAVA_ARCHIVE_MIME_TYPE = "application/java-archive";
  String JSP_MIME_TYPE = "text/x-jsp";
  String PHP_MIME_TYPE = "text/x-php";
  String BZ2_ARCHIVE_MIME_TYPE = "application/x-bzip";
  String GZ_ARCHIVE_MIME_TYPE = "application/x-gzip";
  String GUNZIP_ARCHIVE_MIME_TYPE = "application/x-gunzip";
  String TARGZ_ARCHIVE_MIME_TYPE = "application/x-tar-gz";
  String WORD_2007_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  String WORD_2007_TEMPLATE_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
  String WORD_2007_EXTENSION = "application/vnd.ms-word";
  String EXCEL_2007_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  String EXCEL_2007_TEMPLATE_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
  String EXCEL_2007_EXTENSION = "application/vnd.ms-excel";
  String POWERPOINT_2007_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
  String POWERPOINT_2007_TEMPLATE_MIME_TYPE
      = "application/vnd.openxmlformats-officedocument.presentationml.template";
  String POWERPOINT_2007_EXTENSION = "application/vnd.ms-powerpoint";
  String RSS_MIME_TYPE = "application/rss+xml";
  String JAR_EXTENSION = "jar";
  String WAR_EXTENSION = "war";
  String EAR_EXTENSION = "ear";
  String RTF_EXTENSION = "rtf";
  String JSP_EXTENSION = "jsp";
  String PHP_EXTENSION = "php";
  // Extension .odt (Texte)
  String MIME_TYPE_OO_FORMATTED_TEXT = "application/vnd.oasis.opendocument.text";
  // Extension .ods (Tableur)
  String MIME_TYPE_OO_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
  // Extension .odp (Presentation)
  String MIME_TYPE_OO_PRESENTATION = "application/vnd.oasis.opendocument.presentation";
  // Extension .odg (Dessin)
  String MIME_TYPE_OO_GRAPHICS = "application/vnd.oasis.opendocument.graphics";
  // Extension .odc (Diagramme)
  String MIME_TYPE_OO_DIAGRAM = "application/vnd.oasis.opendocument.chart";
  // Extension .odf (Formule)
  String MIME_TYPE_OO_FORMULA = "application/vnd.oasis.opendocument.formula";
  // Extension .odb (Base de donnees )
  String MIME_TYPE_OO_DB = "application/vnd.oasis.opendocument.database";
  // Extension .odi (Image)
  String MIME_TYPE_OO_IMAGE = "application/vnd.oasis.opendocument.image";
  // Extension .odm (Document principal)
  String MIME_TYPE_OO_MASTER = "application/vnd.oasis.opendocument.text-master";
  Set<String> MS_OFFICE_MIME_TYPES = new HashSet<String>(Arrays
      .asList(
          new String[]{WORD_MIME_TYPE, EXCEL_MIME_TYPE1, EXCEL_MIME_TYPE2, POWERPOINT_MIME_TYPE1,
            POWERPOINT_MIME_TYPE2}));
  Set<String> OPEN_OFFICE_MIME_TYPES = new HashSet<String>(Arrays.asList(
      new String[]{WORD_MIME_TYPE, WORD_2007_MIME_TYPE, WORD_2007_TEMPLATE_MIME_TYPE,
        EXCEL_MIME_TYPE1, EXCEL_MIME_TYPE2, EXCEL_2007_MIME_TYPE, EXCEL_2007_TEMPLATE_MIME_TYPE,
        POWERPOINT_MIME_TYPE1, POWERPOINT_MIME_TYPE2, POWERPOINT_2007_MIME_TYPE,
        POWERPOINT_2007_TEMPLATE_MIME_TYPE, MIME_TYPE_OO_FORMATTED_TEXT, MIME_TYPE_OO_SPREADSHEET,
        MIME_TYPE_OO_PRESENTATION, MIME_TYPE_OO_GRAPHICS, MIME_TYPE_OO_DIAGRAM, MIME_TYPE_OO_FORMULA,
        MIME_TYPE_OO_DB, MIME_TYPE_OO_IMAGE, MIME_TYPE_OO_MASTER}));
  Set<String> ARCHIVE_MIME_TYPES = new HashSet<String>(Arrays.asList(
      new String[]{BZ2_ARCHIVE_MIME_TYPE, GZ_ARCHIVE_MIME_TYPE, GUNZIP_ARCHIVE_MIME_TYPE,
        TARGZ_ARCHIVE_MIME_TYPE, ARCHIVE_MIME_TYPE, SHORT_ARCHIVE_MIME_TYPE, JAVA_ARCHIVE_MIME_TYPE}));
}
