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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.util;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.file.FileUtil;

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
      throw new RuntimeException("The pdf source file doesn't exist");
    } else if (!FileUtil.isPdf(pdfSource.getPath())) {
      throw new RuntimeException("The source is not a pdf file");
    } else if (pdfDestination == null) {
      throw new RuntimeException("The pdf destination file is unknown");
    }

    FileInputStream pdfSourceIS = null;
    FileOutputStream pdfDestinationIS = null;
    try {
      pdfSourceIS = FileUtils.openInputStream(pdfSource);
      pdfDestinationIS = FileUtils.openOutputStream(pdfDestination);
      addImageOnEachPage(pdfSourceIS, image, pdfDestinationIS, isBackground);
    } catch (IOException e) {
      throw new RuntimeException(
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
      throw new RuntimeException("The image file doesn't exist");
    } else if (!FileUtil.isImage(imageToAdd.getPath())) {
      throw new RuntimeException("The picture to add is not an image file");
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
      SilverTrace.error("util", "PdfUtil.stamp", "EX_ERROR_PDF_ADD_WATERWARK", e);
      throw new RuntimeException(
          "A problem has occured during the adding of an image into a pdf file", e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Compute the center position on an axis.
   * @param documentSize
   * @param imageSize
   * @return
   */
  private static float computeImageCenterPosition(float documentSize, float imageSize) {

    // Compute the difference between the two sizes
    float differenceSize = documentSize - imageSize;

    // The center position is the half of the previous result
    return differenceSize / 2;
  }
}
