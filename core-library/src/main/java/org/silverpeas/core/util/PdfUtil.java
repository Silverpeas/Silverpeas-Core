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

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: Yohann Chastagnier
 * Date: 08/07/13
 */
public class PdfUtil {

  private static final String PDF_FILE_ERROR_MSG = "The pdf source file doesn't exist";
  private static final String NOT_PDF_FILE_ERROR_MSG = "The source is not a pdf file";
  private static final String PDF_DESTINATION_ERROR_MSG = "The pdf destination file is unknown";

  private PdfUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Gets some document info from a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @return a {@link DocumentInfo} instance.
   */
  public static DocumentInfo getDocumentInfo(File pdfSource) {
    if (pdfSource == null || !pdfSource.isFile()) {
      throw new SilverpeasRuntimeException(PDF_FILE_ERROR_MSG);
    } else if (!FileUtil.isPdf(pdfSource.getPath())) {
      throw new SilverpeasRuntimeException(NOT_PDF_FILE_ERROR_MSG);
    }
    PdfReader reader = null;
    try (final InputStream pdfSourceIS = FileUtils.openInputStream(pdfSource)) {
      reader = new PdfReader(pdfSourceIS);
      final DocumentInfo documentInfo = new DocumentInfo();
      documentInfo.setNbPages(reader.getNumberOfPages());
      for (int i = 1; i <= documentInfo.getNbPages(); i++) {
        final Rectangle rectangle = reader.getPageSize(i);
        final int maxWidth = Math.round(rectangle.getWidth());
        final int maxHeight = Math.round(rectangle.getHeight());
        if (maxWidth > documentInfo.getMaxWidth()) {
          documentInfo.setMaxWidth(maxWidth);
        }
        if (maxHeight > documentInfo.getMaxHeight()) {
          documentInfo.setMaxHeight(maxHeight);
        }
      }
      return documentInfo;
    } catch (Exception e) {
      SilverLogger.getLogger(PdfUtil.class).error(e);
      throw new SilverpeasRuntimeException(
          "A problem has occurred during the reading of a pdf file", e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Converts the first page of a PDF file into a JPEG image.
   * @param pdfSource the source pdf file, this content is not modified by this method.
   * @param imageDestination the destination file of the image representing the first page.
   */
  public static void firstPageAsImage(File pdfSource, File imageDestination) {
    if (pdfSource == null || !pdfSource.isFile()) {
      throw new SilverpeasRuntimeException(PDF_FILE_ERROR_MSG);
    } else if (!FileUtil.isPdf(pdfSource.getPath())) {
      throw new SilverpeasRuntimeException(NOT_PDF_FILE_ERROR_MSG);
    }
    try (final PDDocument document = PDDocument.load(pdfSource)) {
      final PDFRenderer pdfRenderer = new PDFRenderer(document);
      final BufferedImage image = pdfRenderer.renderImage(0);
      ImageIO.write(image, "jpg", imageDestination);
    } catch (Exception e) {
      SilverLogger.getLogger(PdfUtil.class).error(e);
      throw new SilverpeasRuntimeException(
          "A problem has occurred during the adding of an image into a pdf file", e);
    }
  }

  /**
   * Add a image stamp on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param stampImage the stamp image file
   * @param pdfDestination the destination pdf file, with the stamp in its foreground
   */
  public static void stamp(File pdfSource, File stampImage, File pdfDestination) {
    addImageOnEachPage(pdfSource, stampImage, pdfDestination, false);
  }

  /**
   * Add a image stamp on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param stampImage the stamp image file
   * @param pdfDestination the destination pdf file, with the stamp in its foreground
   */
  public static void stamp(InputStream pdfSource, File stampImage, OutputStream pdfDestination) {
    addImageOnEachPage(pdfSource, stampImage, pdfDestination, false);
  }

  /**
   * Add a image watermark on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param watermarkImage the watermark image file
   * @param pdfDestination the destination pdf file, with the watermark in its background
   */
  public static void watermark(File pdfSource, File watermarkImage, File pdfDestination) {
    addImageOnEachPage(pdfSource, watermarkImage, pdfDestination, true);
  }

  /**
   * Add a image watermark on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param watermarkImage the watermark image file
   * @param pdfDestination the destination pdf file, with the watermark in its background
   */
  public static void watermark(InputStream pdfSource, File watermarkImage,
      OutputStream pdfDestination) {
    addImageOnEachPage(pdfSource, watermarkImage, pdfDestination, true);
  }

  /**
   * Add a image under or over content on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param image the image file
   * @param pdfDestination the destination pdf file, with the image under or over content
   * @param isBackground indicates if image is addes under or over the content of the pdf source
   * file
   */
  private static void addImageOnEachPage(File pdfSource, File image, File pdfDestination,
      final boolean isBackground) {
    if (pdfSource == null || !pdfSource.isFile()) {
      throw new SilverpeasRuntimeException(PDF_FILE_ERROR_MSG);
    } else if (!FileUtil.isPdf(pdfSource.getPath())) {
      throw new SilverpeasRuntimeException(NOT_PDF_FILE_ERROR_MSG);
    } else if (pdfDestination == null) {
      throw new SilverpeasRuntimeException(PDF_DESTINATION_ERROR_MSG);
    }

    FileInputStream pdfSourceIS = null;
    FileOutputStream pdfDestinationIS = null;
    try {
      pdfSourceIS = FileUtils.openInputStream(pdfSource);
      pdfDestinationIS = FileUtils.openOutputStream(pdfDestination);
      addImageOnEachPage(pdfSourceIS, image, pdfDestinationIS, isBackground);
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(
          "Pdf source file cannot be opened or pdf destination file cannot be created", e);
    } finally {
      IOUtils.closeQuietly(pdfSourceIS);
      IOUtils.closeQuietly(pdfDestinationIS);
    }
  }

  /**
   * Add a image under or over content on each page of a PDF file.
   * @param pdfSource the source pdf file, this content is not modified by this method
   * @param imageToAdd the image file
   * @param pdfDestination the destination pdf file, with the image under or over content
   * @param isBackground indicates if image is addes under or over the content of the pdf source
   * file
   */
  private static void addImageOnEachPage(InputStream pdfSource, File imageToAdd,
      OutputStream pdfDestination, final boolean isBackground) {

    // Verify given arguments
    if (imageToAdd == null || !imageToAdd.isFile()) {
      throw new SilverpeasRuntimeException("The image file doesn't exist");
    } else if (!FileUtil.isImage(imageToAdd.getPath())) {
      throw new SilverpeasRuntimeException("The picture to add is not an image file");
    }

    PdfReader reader = null;
    try {

      // Get a reader of PDF content
      reader = new PdfReader(pdfSource);

      // Obtain the total number of pages
      int pdfNbPages = reader.getNumberOfPages();
      PdfStamper stamper = new PdfStamper(reader, pdfDestination);

      // Load the image
      Image image = Image.getInstance(imageToAdd.getPath());
      float imageWidth = image.getWidth();
      float imageHeigth = image.getHeight();

      // Adding the image on each page of the PDF
      for (int i = 1; i <= pdfNbPages; i++) {

        // Page sizes
        Rectangle rectangle = reader.getPageSize(i);

        // Compute the scale of the image
        float scale = Math.min(100, (rectangle.getWidth() / imageWidth * 100));
        image.scalePercent(Math.min(scale, (rectangle.getHeight() / imageHeigth * 100)));

        // Setting the image position for the current page
        image.setAbsolutePosition(
            computeImageCenterPosition(rectangle.getWidth(), image.getScaledWidth()),
            computeImageCenterPosition(rectangle.getHeight(), image.getScaledHeight()));

        // Adding image
        PdfContentByte imageContainer =
            isBackground ? stamper.getUnderContent(i) : stamper.getOverContent(i);
        imageContainer.addImage(image);
      }

      // End of the treatment : closing the stamper
      stamper.close();

    } catch (Exception e) {
      SilverLogger.getLogger(PdfUtil.class).error(e);
      throw new SilverpeasRuntimeException(
          "A problem has occurred during the adding of an image into a pdf file", e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Compute the center position on an axis.
   * @param documentSize the document size.
   * @param imageSize the image size.
   * @return the position of the center of the image to include into document.
   */
  private static float computeImageCenterPosition(float documentSize, float imageSize) {

    // Compute the difference between the two sizes
    float differenceSize = documentSize - imageSize;

    // The center position is the half of the previous result
    return differenceSize / 2;
  }
}
