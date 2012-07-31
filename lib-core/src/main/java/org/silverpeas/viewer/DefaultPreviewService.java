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
import com.silverpeas.util.FileType;
import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultPreviewService implements PreviewService {

  private final ResourceLocator settings = new ResourceLocator("org.silverpeas.viewer.viewer", "");

  @Inject
  private ImageTool imageTool;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.PreviewService#isItPossibleGettingPreview(java.io.File)
   */
  @Override
  public boolean isItPossibleGettingPreview(final File file) {
    final String fileName = file.getPath();
    return imageTool.isActived() &&
        file.exists() &&
        (FileType.decode(file).is(FileType.IMAGE_COMPATIBLE_WITH_IMAGETOOL) ||
            FileUtil.isPdf(fileName) || FileUtil.isOpenOfficeCompatible(fileName) || PLAIN_TEXT_MIME_TYPE
              .equals(FileUtil.getMimeType(fileName)));
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.PreviewService#getPreview(java.lang.String, java.io.File)
   */
  @Override
  public Preview getPreview(final String originalFileName, final File physicalFile) {

    // Checking
    if (!isItPossibleGettingPreview(physicalFile)) {
      throw new PreviewException("IT IS NOT POSSIBLE GETTING DOCUMENT PREVIEW");
    }

    // Save file instance to an generic local variable
    final File resultFile;

    // If the document is an Open Office one
    // 1 - converting it into PDF document
    // 2 - converting the previous result into PNG image
    if (FileUtil.isOpenOfficeCompatible(physicalFile.getName())) {
      final File pdfFile = toPdf(physicalFile, generateTmpFile(FileType.PDF));
      resultFile = toImage(pdfFile, changeTmpFileExtension(pdfFile, FileType.PNG));
      FileUtils.deleteQuietly(pdfFile);
    }

    // If the document is a PDF (or plain text)
    // 1 - convert it into PNG resized image.
    else if (FileUtil.isPdf(originalFileName) ||
        PLAIN_TEXT_MIME_TYPE.equals(FileUtil.getMimeType(physicalFile.getPath()))) {
      resultFile = toImage(physicalFile, generateTmpFile(FileType.PNG));
    }

    // If the document is an image
    // 1 - convert it into JPG resized image.
    else {
      resultFile = toImage(physicalFile, generateTmpFile(FileType.JPG));
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
  protected File generateTmpFile(final FileType fileType) {
    return new File(FileRepositoryManager.getTemporaryPath() + System.nanoTime() + "." +
        fileType.getExtension());
  }

  /**
   * Changes the extension of a file
   * @param fileExtension
   * @return
   */
  protected File changeTmpFileExtension(final File file, final FileType fileType) {
    return new File(getFullPath(file.getPath()) + getBaseName(file.getPath()) + "." +
        fileType.getExtension());
  }
}
