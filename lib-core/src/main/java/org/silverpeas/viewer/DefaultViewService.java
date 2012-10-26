/*
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
package org.silverpeas.viewer;

import static org.silverpeas.viewer.util.SwfUtil.SWF_DOCUMENT_EXTENSION;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.silverpeas.viewer.exception.PreviewException;
import org.silverpeas.viewer.flexpaper.TemporaryFlexPaperView;
import org.silverpeas.viewer.util.DocumentInfo;
import org.silverpeas.viewer.util.SwfUtil;

import com.silverpeas.annotation.Service;
import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.util.FileUtil;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultViewService extends AbstractViewerService implements ViewService {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.ViewService#isViewable(java.io.File)
   */
  @Override
  public boolean isViewable(final File file) {
    final String fileName = file.getPath();
    return (SwfUtil.isActivated() && file.exists() && (FileUtil.isPdf(fileName) || FileUtil
        .isOpenOfficeCompatible(fileName)));
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.ViewService#getDocumentView(java.lang.String, java.io.File)
   */
  @Override
  public DocumentView getDocumentView(final String originalFileName, final File physicalFile) {

    // Checking
    if (!isViewable(physicalFile)) {
      throw new PreviewException("IT IS NOT POSSIBLE GETTING DOCUMENT VIEW");
    }

    // Save file instance to an generic local variable
    final DocumentView documentView;

    // If the document is an Open Office one
    // 1 - converting it into PDF document
    // 2 - converting the previous result into PNG image
    if (FileUtil.isOpenOfficeCompatible(physicalFile.getName())) {
      final File pdfFile = toPdf(physicalFile, generateTmpFile(PDF_DOCUMENT_EXTENSION));
      documentView =
          toSwf(originalFileName, pdfFile, changeFileExtension(pdfFile, SWF_DOCUMENT_EXTENSION));
      FileUtils.deleteQuietly(pdfFile);
    }

    // If the document is a PDF (or plain text)
    // 1 - convert it into PNG resized image.
    else {
      documentView = toSwf(originalFileName, physicalFile, generateTmpFile(SWF_DOCUMENT_EXTENSION));
    }

    // Returning the result
    return documentView;
  }

  /**
   * Convert into PDF
   * @param source
   * @return
   */
  private File toPdf(final File source, final File destination) {
    DocumentFormatConverterFactory.getFactory().getToPDFConverter()
        .convert(source, destination, DocumentFormat.pdf);
    return destination;
  }

  /**
   * Convert into Swf file
   * @param source
   * @return
   */
  private DocumentView toSwf(final String originalFileName, final File source,
      final File destination) {

    // NbPages & max page width & max page height
    final DocumentInfo info = SwfUtil.getPdfDocumentInfo(source);

    // Create images
    SwfUtil.fromPdfToSwf(source, destination);

    // Files
    return new TemporaryFlexPaperView(originalFileName, destination, info);
  }
}
