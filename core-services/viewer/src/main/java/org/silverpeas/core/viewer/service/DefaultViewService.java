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
package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.ToPDFConverter;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.DocumentInfo;
import org.silverpeas.core.util.PdfUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.viewer.flexpaper.TemporaryFlexPaperView;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.model.TemporaryPdfView;
import org.silverpeas.core.viewer.util.JsonPdfUtil;
import org.silverpeas.core.viewer.util.SwfUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static java.util.Optional.of;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.viewer.model.ViewerSettings.*;
import static org.silverpeas.core.viewer.util.SwfUtil.SWF_DOCUMENT_EXTENSION;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultViewService extends AbstractViewerService implements ViewService {

  private static final String PROCESS_NAME = "VIEW";

  @Inject
  private ToPDFConverter toPDFConverter;

  private static boolean isSwfNeeded() {
    return isDefined(getLicenceKey()) || !pdfViewerEnabled();
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.ViewService#isViewable(java.io.File)
   */
  @Override
  public boolean isViewable(final File file) {
    final String filePath = file.getPath();
    final boolean toolsAvailable = !isSwfNeeded() || SwfUtil.isPdfToSwfActivated();
    return (toolsAvailable && file.exists() &&
        (FileUtil.isPdf(filePath) || toPDFConverter.isDocumentSupported(filePath)));
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.ViewService#getDocumentView(java.lang.String, java.io.File)
   */
  @Override
  public DocumentView getDocumentView(final ViewerContext viewerContext) {
    return process(PROCESS_NAME, new ViewerTreatment<DocumentView>() {
      @Override
      public DocumentView execute() {

        // Checking
        if (!isViewable(viewerContext.getOriginalSourceFile())) {
          throw new ViewerException("IT IS NOT POSSIBLE GETTING DOCUMENT VIEW");
        }

        final File pdfFile;

        /*
         * 1 - converting it into PDF document
         */

        // If the document is an Open Office one
        if (toPDFConverter.isDocumentSupported(viewerContext.getOriginalSourceFile().getPath())) {
          pdfFile = toPdf(viewerContext.getOriginalSourceFile(),
              generateTmpFile(viewerContext, PDF_DOCUMENT_EXTENSION));
        }

        // If the document is a PDF (or plain text)
        else {
          pdfFile = generateTmpFile(viewerContext, PDF_DOCUMENT_EXTENSION);
          try {
            FileUtils.copyFile(viewerContext.getOriginalSourceFile(), pdfFile);
          } catch (IOException e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }

        /*
         * 2 - converting the previous result into SWF file
         */
        final DocumentView documentView;
        if (isSwfNeeded()) {
          documentView = toSwfResult(viewerContext, pdfFile);
          FileUtils.deleteQuietly(pdfFile);
        } else {
          documentView = toPdfResult(viewerContext, pdfFile);
        }

        // Returning the result
        return documentView;
      }

      @Override
      public DocumentView performAfterSuccess(final DocumentView result) {
        if (isSilentConversionEnabled() && viewerContext.isProcessingCache() &&
            PreviewService.get().isPreviewable(viewerContext.getOriginalSourceFile())) {
          ManagedThreadPool.getPool().invoke(() -> {
            PreviewService.get().getPreview(viewerContext.copy());
          });
        }
        return ViewerTreatment.super.performAfterSuccess(result);
      }
    }).execute(viewerContext);
  }

  @Override
  public void removeDocumentView(final ViewerContext viewerContext) {
    of(viewerContext.fromInitializerProcessName(PROCESS_NAME).getWorkspace())
        .filter(TemporaryWorkspaceTranslation::exists)
        .ifPresent(TemporaryWorkspaceTranslation::remove);
  }

  /**
   * Convert into PDF
   * @param source the file source.
   * @return a {@link File} instance referencing a PDF file.
   */
  private File toPdf(final File source, final File destination) {
    toPDFConverter.convert(source, destination, DocumentFormat.pdf);
    return destination;
  }

  /**
   * Convert into Swf {@link DocumentView} result.
   *
   * @param viewerContext the view context.
   * @param pdfSource the pdf source.
   * @return the initialized {@link DocumentView} instance.
   */
  private DocumentView toSwfResult(final ViewerContext viewerContext, final File pdfSource) {

    // NbPages & max page width & max page height
    final DocumentInfo info = PdfUtil.getDocumentInfo(pdfSource);

    File swfFile = changeFileExtension(pdfSource, SWF_DOCUMENT_EXTENSION);
    swfFile.getParentFile().mkdirs();

    boolean jsonConversion = JsonPdfToolManager.isActivated();
    boolean splitMode = isSplitStrategyEnabled();

    // Create SWF data
    if (!splitMode) {
      try {
        SwfUtil.fromPdfToSwf(pdfSource, swfFile, false);
        jsonConversion = false;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
        splitMode = true;
      }
    }

    if (splitMode) {
      swfFile = new File(swfFile.getParentFile(), "page." + SWF_DOCUMENT_EXTENSION);
      SwfUtil.fromPdfToSwf(pdfSource, swfFile, true);
      if (jsonConversion) {
        try {
          JsonPdfUtil.convert(pdfSource, swfFile);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
          jsonConversion = false;
        }
      }
    }

    // Files
    TemporaryFlexPaperView documentView = new TemporaryFlexPaperView(viewerContext, swfFile, info);
    documentView.markDocumentSplit(splitMode);
    documentView.markSearchDataComputed(jsonConversion);
    return documentView;
  }

  /**
   * Convert into Pdf {@link DocumentView} result.
   *
   * @param viewerContext the view context.
   * @param pdfSource the pdf source.
   * @return the initialized {@link DocumentView} instance.
   */
  private DocumentView toPdfResult(final ViewerContext viewerContext, final File pdfSource) {

    // NbPages & max page width & max page height
    final DocumentInfo info = PdfUtil.getDocumentInfo(pdfSource);

    final File pdfFile = new File(pdfSource.getPath());
    return new TemporaryPdfView(viewerContext, pdfFile, info);
  }
}
