/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ImageUtil {

  public static String[] getWidthAndHeightByWidth(File image, int widthParam) {
    String[] result = new String[2];
    if (image != null && image.isFile()) {
      try {
        BufferedImage inputBuf = ImageIO.read(image);
        // calcul de la taille de la sortie
        double inputBufWidth;
        double inputBufHeight;
        double width;
        double ratio;
        double height;
        if (inputBuf.getWidth() > widthParam) {
          inputBufWidth = inputBuf.getWidth();
          inputBufHeight = inputBuf.getHeight();
          width = widthParam;
          ratio = inputBufWidth / width;
          height = inputBufHeight / ratio;
        } else {
          width = inputBuf.getWidth();
          height = inputBuf.getHeight();
        }
        String sWidth = Double.toString(width);
        String sHeight = Double.toString(height);

        result[0] = sWidth.substring(0, sWidth.indexOf("."));
        result[1] = sHeight.substring(0, sHeight.indexOf("."));

        return result;
      } catch (Exception e) {
        if (image != null) {
          SilverTrace.error("util", "ImageUtil.getWidthAndHeightByWidth", "root.MSG_GEN_ERROR",
              "File not found : " + image.getAbsolutePath());
        }
      }
    }
    result[0] = "";
    result[1] = "";
    return result;
  }

  public static String[] getWidthAndHeightByHeight(File image, int heightParam) {
    String[] result = new String[2];
    if (image != null && image.isFile()) {
      try {
        BufferedImage inputBuf = ImageIO.read(image);
        // calcul de la taille de la sortie
        double inputBufWidth;
        double inputBufHeight;
        double height;
        double ratio;
        double width;
        if (inputBuf.getHeight() > heightParam) {
          inputBufHeight = inputBuf.getHeight();
          inputBufWidth = inputBuf.getWidth();
          height = heightParam;
          ratio = inputBufHeight / height;
          width = inputBufWidth / ratio;
        } else {
          height = inputBuf.getHeight();
          width = inputBuf.getWidth();
        }
        String sWidth = Double.toString(width);
        String sHeight = Double.toString(height);

        result[0] = sWidth.substring(0, sWidth.indexOf("."));
        result[1] = sHeight.substring(0, sHeight.indexOf("."));

        return result;
      } catch (Exception e) {
        if (image != null) {
          SilverTrace.error("util", "ImageUtil.getWidthAndHeightByHeight", "root.MSG_GEN_ERROR",
              "File not found : " + image.getAbsolutePath());
        }
      }
    }
    result[0] = "";
    result[1] = "";
    return result;
  }

  public static String[] getWidthAndHeight(File image) {
    String[] result = new String[2];
    if (image != null && image.isFile()) {
      try {
        BufferedImage inputBuf = ImageIO.read(image);
        // calcul de la taille de la sortie
        double inputBufWidth = inputBuf.getWidth();
        double inputBufHeight = inputBuf.getHeight();
        String sWidth = Double.toString(inputBufWidth);
        String sHeight = Double.toString(inputBufHeight);

        result[0] = sWidth.substring(0, sWidth.indexOf("."));
        result[1] = sHeight.substring(0, sHeight.indexOf("."));

        return result;
      } catch (Exception e) {
        if (image != null) {
          SilverTrace.error("util", "ImageUtil.getWidthAndHeight", "root.MSG_GEN_ERROR",
              "File not found : " + image.getAbsolutePath());
        }
      }
    }
    result[0] = "";
    result[1] = "";
    return result;
  }

}
