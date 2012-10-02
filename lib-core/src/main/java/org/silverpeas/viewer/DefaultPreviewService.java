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

import static com.silverpeas.util.MimeTypes.PLAIN_TEXT_MIME_TYPE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.silverpeas.image.ImageTool;
import org.silverpeas.image.ImageToolDirective;
import org.silverpeas.image.option.DimensionOption;
import org.silverpeas.viewer.exception.PreviewException;

import com.silverpeas.annotation.Service;
import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.converter.option.PageRangeFilterOption;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultPreviewService implements PreviewService {

  private final ResourceLocator settings = new ResourceLocator("org.silverpeas.viewer.viewer", "");
  private final static Set<String> imageMimeTypePreviewable = new HashSet<String>();
  static {
    for (final String imageExtension : new String[] { MimeTypes.BMP_IMAGE_EXTENSION,
        MimeTypes.GIF_IMAGE_EXTENSION, MimeTypes.JPG_IMAGE_EXTENSION,
        MimeTypes.PCD_IMAGE_EXTENSION, MimeTypes.PNG_IMAGE_EXTENSION,
        MimeTypes.TGA_IMAGE_EXTENSION, MimeTypes.TIF_IMAGE_EXTENSION }) {
      imageMimeTypePreviewable.add(FileUtil.getMimeType(new StringBuilder("file.").append(
          imageExtension).toString()));
    }
    imageMimeTypePreviewable.remove(MimeTypes.DEFAULT_MIME_TYPE);
  }

  @Inject
  private ImageTool imageTool;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.PreviewService#isItPossibleGettingPreview(java.io.File)
   */
  @Override
  public boolean isPreviewable(final File file) {
    final String fileName = file.getPath();
    if (imageTool.isActived() && file.exists()) {
      final String mimeType = FileUtil.getMimeType(fileName);
      return ((imageMimeTypePreviewable.contains(mimeType) || FileUtil.isPdf(fileName) ||
          FileUtil.isOpenOfficeCompatible(fileName) || PLAIN_TEXT_MIME_TYPE.equals(mimeType)));
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.PreviewService#getPreview(java.lang.String, java.io.File)
   */
  @Override
  public Preview getPreview(final String originalFileName, final File physicalFile) {

    // Checking
    if (!isPreviewable(physicalFile)) {
      throw new PreviewException("IT IS NOT POSSIBLE GETTING DOCUMENT PREVIEW");
    }

    // Save file instance to an generic local variable
    final File resultFile;

    // If the document is an Open Office one
    // 1 - converting it into PDF document
    // 2 - converting the previous result into PNG image
    if (FileUtil.isOpenOfficeCompatible(physicalFile.getName())) {
      final File pdfFile = toPdf(physicalFile, generateTmpFile(MimeTypes.PDF_DOCUMENT_EXTENSION));
      resultFile = toImage(pdfFile, changeTmpFileExtension(pdfFile, MimeTypes.PNG_IMAGE_EXTENSION));
      FileUtils.deleteQuietly(pdfFile);
    }

    // If the document is a PDF (or plain text)
    // 1 - convert it into PNG resized image.
    else if (FileUtil.isPdf(originalFileName) ||
        PLAIN_TEXT_MIME_TYPE.equals(FileUtil.getMimeType(physicalFile.getPath()))) {
      resultFile = toImage(physicalFile, generateTmpFile(MimeTypes.PNG_IMAGE_EXTENSION));
    }

    // If the document is an image
    // 1 - convert it into JPG resized image.
    else {
      resultFile = toImage(physicalFile, generateTmpFile(MimeTypes.JPG_IMAGE_EXTENSION));
    }

    // Returning the result
    return new TemporaryPreview(originalFileName, resultFile);
  }

  /**
   * Convert into PDF
   * @param source
   * @return
   */
  private File toPdf(final File source, final File destination) {
    DocumentFormatConverterFactory.getFactory().getToPDFConverter()
        .convert(source, destination, DocumentFormat.pdf, new PageRangeFilterOption("1"));
    return destination;
  }

  /**
   * Convert into Image
   * @param source
   * @return
   */
  private File toImage(final File source, final File destination) {
    imageTool.convert(
        source,
        destination,
        DimensionOption.widthAndHeight(settings.getInteger("preview.width.max", 500),
            settings.getInteger("preview.height.max", 500)), ImageToolDirective.PREVIEW_WORK,
        ImageToolDirective.GEOMETRY_SHRINK, ImageToolDirective.FIRST_PAGE_ONLY);
    return destination;
  }

  /**
   * Generate a tmp file
   * @param fileType
   * @return
   */
  protected File generateTmpFile(final String fileExtension) {
    return new File(FileRepositoryManager.getTemporaryPath() + System.nanoTime() + "." +
        fileExtension);
  }

  /**
   * Changes the extension of a file
   * @param fileExtension
   * @return
   */
  protected File changeTmpFileExtension(final File file, final String fileExtension) {
    return new File(getFullPath(file.getPath()) + getBaseName(file.getPath()) + "." + fileExtension);
  }
}
