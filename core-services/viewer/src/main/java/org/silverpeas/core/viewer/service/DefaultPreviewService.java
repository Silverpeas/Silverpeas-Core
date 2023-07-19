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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.ToPDFConverter;
import org.silverpeas.core.contribution.converter.option.SinglePageSelection;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.ImageToolDirective;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.io.media.image.option.OrientationOption;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.PdfUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.model.TemporaryPreview;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.silverpeas.core.util.CollectionUtil.asSet;
import static org.silverpeas.core.util.ImageUtil.*;
import static org.silverpeas.core.util.MimeTypes.PLAIN_TEXT_MIME_TYPE;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.viewer.model.ViewerSettings.*;

/**
 * Default implementation of the preview service.
 * @author Yohann Chastagnier
 */
@Service
@Singleton
public class DefaultPreviewService extends AbstractViewerService implements PreviewService {

  private static final String PROCESS_NAME = "PREVIEW";

  // Extension of pdf document file
  private static final Set<String> imageMimeTypePreviewable = new HashSet<>();

  @Inject
  private ToPDFConverter toPDFConverter;

  @Inject
  private ImageTool imageTool;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.PreviewService#isPreviewable(java.io.File)
   */
  @Override
  public boolean isPreviewable(final File file) {
    final String filePath = file.getPath();
    if (imageTool.isActivated() && file.exists()) {
      final String mimeType = FileUtil.getMimeType(filePath);
      return imageMimeTypePreviewable.contains(mimeType) || FileUtil.isPdf(filePath) ||
          toPDFConverter.isDocumentSupported(filePath) || PLAIN_TEXT_MIME_TYPE.equals(mimeType);
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.PreviewService#getPreview(java.lang.String, java.io.File)
   */
  @Override
  public Preview getPreview(final ViewerContext viewerContext) {
    return process(PROCESS_NAME, new ViewerTreatment<Preview>() {
      @Override
      public Preview execute() {

        // Checking
        if (!isPreviewable(viewerContext.getOriginalSourceFile())) {
          throw new ViewerException("IT IS NOT POSSIBLE GETTING DOCUMENT PREVIEW");
        }

        // Save file instance to an generic local variable
        final File resultFile;

        // If the document is an Open Office one
        // 1 - converting it into PDF document
        // 2 - converting the previous result into PNG image
        if (toPDFConverter.isDocumentSupported(viewerContext.getOriginalSourceFile().getPath())) {
          final File pdfFile = convertToPdf(viewerContext.getOriginalSourceFile(),
              generateTmpFile(viewerContext, PDF_DOCUMENT_EXTENSION));
          resultFile = convertToImage(pdfFile, changeFileExtension(pdfFile, PNG_IMAGE_EXTENSION));
          deleteQuietly(pdfFile);
        }

        // If the document is a PDF (or plain text)
        // 1 - convert it into PNG resized image.
        else if (FileUtil.isPdf(viewerContext.getOriginalFileName()) || PLAIN_TEXT_MIME_TYPE
            .equals(FileUtil.getMimeType(viewerContext.getOriginalSourceFile().getPath()))) {
          resultFile = convertToImage(viewerContext.getOriginalSourceFile(),
              generateTmpFile(viewerContext, PNG_IMAGE_EXTENSION));
        }

        // If the document is an image
        // 1 - convert it into JPG resized image.
        else {
          resultFile = convertToImage(viewerContext.getOriginalSourceFile(),
              generateTmpFile(viewerContext, defaultStringIfNotDefined(
                  getExtension(viewerContext.getOriginalSourceFile().getName()),
                  JPG_IMAGE_EXTENSION)));
        }

        // Returning the result
        return new TemporaryPreview(viewerContext, resultFile);
      }

      @Override
      public Preview performAfterSuccess(final Preview result) {
        if (isSilentConversionEnabled() && viewerContext.isProcessingCache() &&
            ViewService.get().isViewable(viewerContext.getOriginalSourceFile()))
          ManagedThreadPool.getPool().invoke((Runnable) () ->
            ViewService.get().getDocumentView(viewerContext.copy()));
        return ViewerTreatment.super.performAfterSuccess(result);
      }
    }).execute(viewerContext);
  }

  @Override
  public void removePreview(final ViewerContext viewerContext) {
    of(viewerContext.fromInitializerProcessName(PROCESS_NAME).getWorkspace())
        .filter(TemporaryWorkspaceTranslation::exists)
        .ifPresent(TemporaryWorkspaceTranslation::remove);
  }

  private File convertToPdf(final File source, final File destination) {
    toPDFConverter.convert(source, destination, DocumentFormat.pdf, new SinglePageSelection(1));
    return destination;
  }

  private File convertToImage(File source, File destination) {
    boolean deleteSource = false;
    if (FileUtil.isPdf(source.getPath())) {
      PdfUtil.firstPageAsImage(source, destination);
      source = destination;
      destination = changeFileExtension(destination, JPG_IMAGE_EXTENSION);
      deleteSource = !source.equals(destination);
    }
    imageTool.convert(source, destination,
        asSet(DimensionOption.widthAndHeight(getPreviewMaxWidth(), getPreviewMaxHeight()),
              OrientationOption.auto()),
        ImageToolDirective.PREVIEW_WORK, ImageToolDirective.GEOMETRY_SHRINK,
        ImageToolDirective.FIRST_PAGE_ONLY);
    if (deleteSource) {
      deleteQuietly(source);
    }
    return destination;
  }

  static {
    for (final String imageExtension : new String[] { BMP_IMAGE_EXTENSION, GIF_IMAGE_EXTENSION,
        JPG_IMAGE_EXTENSION, PCD_IMAGE_EXTENSION, PNG_IMAGE_EXTENSION, TGA_IMAGE_EXTENSION,
        TIF_IMAGE_EXTENSION, WEBP_IMAGE_EXTENSION }) {
      imageMimeTypePreviewable.add(FileUtil.getMimeType("file." + imageExtension));
    }
    imageMimeTypePreviewable.remove(MimeTypes.DEFAULT_MIME_TYPE);
  }
}
