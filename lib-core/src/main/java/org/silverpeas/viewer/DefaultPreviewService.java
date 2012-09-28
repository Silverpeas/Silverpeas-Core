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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
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
  public boolean isPreviewable(final File file) {
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
    if (!isPreviewable(physicalFile)) {
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

  /*
   * (non-Javadoc)
   * @see org.silverpeas.viewer.PreviewService#getDocument(java.lang.String, java.io.File)
   */
  @Override
  public List<PageView> getDocument(final String originalFileName, final File physicalFile) {

    // Checking
    if (!isPreviewable(physicalFile)) {
      throw new PreviewException("IT IS NOT POSSIBLE GETTING DOCUMENT PREVIEW");
    }

    // Save file instance to an generic local variable
    final List<File> resultFiles;

    // If the document is an Open Office one
    // 1 - converting it into PDF document
    // 2 - converting the previous result into PNG image
    if (FileUtil.isOpenOfficeCompatible(physicalFile.getName())) {
      final File pdfFile = toPdfView(physicalFile, generateTmpFile(FileType.PDF));
      resultFiles = toImageViews(pdfFile, changeTmpFileExtension(pdfFile, FileType.PNG));
      FileUtils.deleteQuietly(pdfFile);
    }

    // If the document is a PDF (or plain text)
    // 1 - convert it into PNG resized image.
    else if (FileUtil.isPdf(originalFileName) ||
        PLAIN_TEXT_MIME_TYPE.equals(FileUtil.getMimeType(physicalFile.getPath()))) {
      resultFiles = toImageViews(physicalFile, generateTmpFile(FileType.PNG));
    }

    // If the document is an image
    // 1 - convert it into JPG resized image.
    else {
      resultFiles = toImageViews(physicalFile, generateTmpFile(FileType.JPG));
    }

    // Returning the result
    final List<PageView> resultPageViews = new ArrayList<PageView>();
    for (final File file : resultFiles) {
      resultPageViews.add(new TemporaryPageView(originalFileName, file));
    }
    return resultPageViews;
  }

  /**
   * Convert into PDF
   * @param source
   * @return
   */
  private File toPdfView(final File source, final File destination) {
    DocumentFormatConverterFactory.getFactory().getToPDFConverter()
        .convert(source, destination, DocumentFormat.pdf);
    return destination;
  }

  /**
   * Convert into Image
   * @param source
   * @return
   */
  private List<File> toImageViews(final File source, final File destination) {

    // Create the operation, add images and operators/options
    final IMOperation op = new IMOperation();
    op.density(196);
    op.addImage(source.getPath());
    op.resample(72);
    op.trim();
    op.p_repage();
    op.bordercolor("white");
    op.border(3);
    op.addImage(destination.getPath());

    // Executing command
    try {
      new ConvertCmd().run(op);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    final List<File> result = new ArrayList<File>();
    String fileExtension = "." + FilenameUtils.getExtension(destination.getName());
    String baseFilePath =
        destination.getParent() + File.separator + FilenameUtils.getBaseName(destination.getName());
    File pageFile = new File(baseFilePath + fileExtension);
    if (pageFile.exists()) {
      result.add(pageFile);
    } else {
      int i = 0;
      // String[] imageWidthHeight;
      baseFilePath += "-";
      pageFile = new File(baseFilePath + i + fileExtension);
      while (pageFile.exists()) {
        // imageWidthHeight = ImageUtil.getWidthAndHeight(pageFile);
        result.add(pageFile);
        pageFile = new File(baseFilePath + (++i) + fileExtension);
      }
    }
    return result;
  }
}
