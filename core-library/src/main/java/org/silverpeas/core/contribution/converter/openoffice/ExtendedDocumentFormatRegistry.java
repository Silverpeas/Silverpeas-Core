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
package org.silverpeas.core.contribution.converter.openoffice;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentFamily;
import com.artofsolving.jodconverter.DocumentFormat;
import org.silverpeas.core.util.MimeTypes;

/**
 * @author Yohann Chastagnier
 */
public class ExtendedDocumentFormatRegistry extends DefaultDocumentFormatRegistry {

  public ExtendedDocumentFormatRegistry() {
    super();

    final DocumentFormat docx =
        new DocumentFormat("Microsoft Word", DocumentFamily.TEXT, MimeTypes.WORD_2007_MIME_TYPE,
            "docx");
    docx.setExportFilter(DocumentFamily.TEXT, "MS Word 2007");
    if (getFormatByFileExtension(docx.getFileExtension()) == null) {
      addDocumentFormat(docx);
    }

    final DocumentFormat xlsx =
        new DocumentFormat("Microsoft Excel", DocumentFamily.SPREADSHEET,
            MimeTypes.EXCEL_2007_MIME_TYPE, "xlsx");
    xlsx.setExportFilter(DocumentFamily.SPREADSHEET, "MS Excel 2007");
    if (getFormatByFileExtension(xlsx.getFileExtension()) == null) {
      addDocumentFormat(xlsx);
    }

    final DocumentFormat pptx =
        new DocumentFormat("Microsoft PowerPoint", DocumentFamily.PRESENTATION,
            MimeTypes.POWERPOINT_2007_MIME_TYPE, "pptx");
    pptx.setExportFilter(DocumentFamily.PRESENTATION, "MS PowerPoint 2007");
    if (getFormatByFileExtension(pptx.getFileExtension()) == null) {
      addDocumentFormat(pptx);
    }

    final DocumentFormat sql =
        new DocumentFormat("Plain Text", DocumentFamily.TEXT, "text/plain", "sql");
    sql.setImportOption("FilterName", "Text");
    sql.setExportFilter(DocumentFamily.TEXT, "Text");
    if (getFormatByFileExtension(sql.getFileExtension()) == null) {
      addDocumentFormat(sql);
    }
  }
}
