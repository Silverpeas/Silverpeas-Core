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
package org.silverpeas.viewer;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.silverpeas.viewer.exception.PreviewException;
import org.silverpeas.viewer.flexpaper.TemporaryFlexPaperView;

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
    return (SwfToolManager.isActivated() && file.exists() && (FileUtil.isPdf(fileName) || FileUtil
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
      documentView = toSwf(originalFileName, pdfFile, changeTmpFileExtension(pdfFile, "swf"));
      FileUtils.deleteQuietly(pdfFile);
    }

    // If the document is a PDF (or plain text)
    // 1 - convert it into PNG resized image.
    else {
      documentView = toSwf(originalFileName, physicalFile, generateTmpFile("swf"));
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

    // NbPages
    final List<String> pageInfoOutputLines =
        exec(new StringBuilder().append("pdf2swf -qq ").append(source.getPath()).append(" --info")
            .toString());

    // Create images
    final StringBuilder pdf2SwfCommand = new StringBuilder();
    pdf2SwfCommand.append("pdf2swf ");
    pdf2SwfCommand.append(source.getPath());
    pdf2SwfCommand.append(" -o ");
    pdf2SwfCommand.append(destination.getPath());
    pdf2SwfCommand.append(" -f -T 9 -t -s storeallcharacters");
    exec(pdf2SwfCommand.toString());

    // Width & height (max)
    final PageInfo info = new PageInfo();
    for (final String pageInfoOutputLine : pageInfoOutputLines) {
      info.copyInfosIfGreater(parsePageInfo(pageInfoOutputLine));
    }

    // Files
    return new TemporaryFlexPaperView(originalFileName, destination, pageInfoOutputLines.size(),
        info.width, info.height);
  }

  /**
   * Parsing Swftool output line
   * @param outputLine
   * @return
   */
  private PageInfo parsePageInfo(final String outputLine) {
    final PageInfo pageInfo = new PageInfo();
    for (final String info : outputLine.split(" ")) {
      if (info.indexOf("width") >= 0) {
        pageInfo.width =
            Integer.valueOf(info.replaceAll("width=", "").replaceAll("\\.[0-9]{1,}", ""));
      } else if (info.indexOf("height") >= 0) {
        pageInfo.height =
            Integer.valueOf(info.replaceAll("height=", "").replaceAll("\\.[0-9]{1,}", ""));
      }
    }
    return pageInfo;
  }

  /**
   * @author Yohann Chastagnier
   */
  private class PageInfo {
    public int width = 0;
    public int height = 0;

    public void copyInfosIfGreater(final PageInfo pageInfo) {
      width = Math.max(width, pageInfo.width);
      height = Math.max(height, pageInfo.height);
    }
  }
}
