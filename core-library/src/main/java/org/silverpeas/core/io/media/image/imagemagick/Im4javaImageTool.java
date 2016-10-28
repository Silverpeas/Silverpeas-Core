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
package org.silverpeas.core.io.media.image.imagemagick;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.io.media.image.AbstractImageTool;
import org.silverpeas.core.io.media.image.ImageInfoType;
import org.silverpeas.core.io.media.image.ImageToolDirective;
import org.silverpeas.core.io.media.image.option.AbstractImageToolOption;
import org.silverpeas.core.io.media.image.option.AnchoringPosition;
import org.silverpeas.core.io.media.image.option.BackgroundOption;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.io.media.image.option.TransparencyColorOption;
import org.silverpeas.core.io.media.image.option.WatermarkTextOption;

import javax.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.silverpeas.core.io.media.image.ImageInfoType.HEIGHT_IN_PIXEL;
import static org.silverpeas.core.io.media.image.ImageInfoType.WIDTH_IN_PIXEL;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class Im4javaImageTool extends AbstractImageTool {

  private static final String FIRST_PAGE_ONLY = "[0]";
  private static final String GEOMETRY_SHRINK = ">";

  /*
   * (non-Javadoc)
   * @see ImageTool#isActived()
   */
  @Override
  public boolean isActivated() {
    return Im4javaManager.isActivated();
  }

  @Override
  public String[] getImageInfo(final File source, final ImageInfoType... infoTypes)
      throws SilverpeasException {
    return identify(source,
        Arrays.stream(infoTypes).map(ImageInfoType::getImOption).toArray(String[]::new));
  }

  private String[] identify(final File source, final String... infoTypes)
      throws SilverpeasException {
    try {
      IMOperation op = new IMOperation();
      op.format(String.join("|", (CharSequence[]) infoTypes));
      op.addImage(source.getPath());
      IdentifyCmd identifyCmd = new IdentifyCmd();
      ArrayListOutputConsumer result = new ArrayListOutputConsumer();
      identifyCmd.setOutputConsumer(result);
      identifyCmd.run(op);
      return result.getOutput().get(0).split("[|]");
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see AbstractImageTool#convert(java.io.File, java.io.File, java.util.Map, java.util.Set)
   */
  @Override
  protected void convert(final File source, final File destination,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final Set<ImageToolDirective> directives) throws SilverpeasException {

    // Create the operation, add images and operators/options
    final IMOperation op = new IMOperation();

    // Source file
    setSource(op, source, directives);

    // Additional options
    transparencyColor(op, options);
    background(op, options);
    resize(op, options, directives);
    watermarkText(op, source, options);

    // Destination file
    setDestination(op, destination, directives);

    // Executing command
    try {
      new ConvertCmd().run(op);
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  /**
   * Centralizes the source declaration
   * @param op
   * @param source
   * @param directives
   */
  private void setSource(final IMOperation op, final File source,
      final Set<ImageToolDirective> directives) {
    final StringBuilder sb = new StringBuilder(source.getPath());

    // Getting only the first page of the source
    if (directives.contains(ImageToolDirective.FIRST_PAGE_ONLY)) {
      sb.append(FIRST_PAGE_ONLY);
    }

    op.addImage(sb.toString());
  }

  private void setDestination(final IMOperation op, final File destination,
      final Set<ImageToolDirective> directives) {
    op.addImage(destination.getPath());
  }

  /**
   * Centralizes background handling
   * @param op
   * @param options
   */
  private void background(final IMOperation op,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options) {

    // Getting background option
    final BackgroundOption background = getOption(options, BackgroundOption.class);
    if (background != null) {
      op.background(background.getValue());
      op.flatten();
    }
  }

  /**
   * Centralizes background handling
   * @param op
   * @param options
   */
  private void transparencyColor(final IMOperation op,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options) {

    // Getting transparencyColor option
    final TransparencyColorOption transparencyColor =
        getOption(options, TransparencyColorOption.class);
    if (transparencyColor != null) {
      op.transparentColor(transparencyColor.getColor());
    }
  }

  /**
   * Centralizes resizing operation
   * @param op
   * @param options
   * @param directives
   */
  private void resize(final IMOperation op,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final Set<ImageToolDirective> directives) {

    // Getting dimension option
    final DimensionOption dimension = getOption(options, DimensionOption.class);
    if (dimension != null) {
      final StringBuilder specialDirective = new StringBuilder();

      // Shrinks images with dimension(s) larger than the corresponding width and/or height
      // dimension(s).
      if (directives.contains(ImageToolDirective.GEOMETRY_SHRINK)) {
        specialDirective.append(GEOMETRY_SHRINK);
      }

      // Performances
      if (directives.contains(ImageToolDirective.PREVIEW_WORK)) {
        op.thumbnail(dimension.getWidth(), dimension.getHeight(), specialDirective.toString());
      } else {
        op.resize(dimension.getWidth(), dimension.getHeight(), specialDirective.toString());
      }
    }
  }

  /**
   * Centralizes text watermarking operation
   * @param op
   * @param source
   * @param options
   * @throws SilverpeasException
   */
  private void watermarkText(final IMOperation op, final File source,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options)
      throws SilverpeasException {
    WatermarkTextOption watermarkText = getOption(options, WatermarkTextOption.class);
    if (watermarkText != null) {
      final String[] imageInfo;
      DimensionOption dimension = getOption(options, DimensionOption.class);
      if (dimension != null) {
        imageInfo = new String[2];
        imageInfo[0] = dimension.getWidth() != null ? String.valueOf(dimension.getWidth()) :
            getImageInfo(source, WIDTH_IN_PIXEL)[0];
        imageInfo[1] = dimension.getHeight() != null ? String.valueOf(dimension.getHeight()) :
            getImageInfo(source, HEIGHT_IN_PIXEL)[0];
      } else {
        imageInfo = getImageInfo(source, WIDTH_IN_PIXEL, HEIGHT_IN_PIXEL);
      }
      int width = Integer.valueOf(imageInfo[0]);
      int height = Integer.valueOf(imageInfo[1]);

      int pointSize = (int) Math.rint((width * 0.02) + Math.log(width));
      pointSize = Math.min((int) (height * 0.2), pointSize);

      int minX = (int) Math.max(1, (height * 0.015));
      int minY = (int) Math.max(1, (height * 0.025));
      int x = (int) (minX + Math.max(1, (pointSize / 2.5)));
      int y = (int) (minY + Math.max(1, (pointSize / 1.75)));

      final String text = watermarkText.getText();
      final AnchoringPosition anchoringPosition = watermarkText.getAnchoringPosition();

      op.font(watermarkText.getFont());
      op.pointsize(pointSize);

      String black = "rgba(0, 0, 0, 0.5)";
      String white = "rgba(255, 255, 255, 0.5)";
      drawText(op, black, text, anchoringPosition, x, y);
      drawText(op, white, text, anchoringPosition, minX, minY);
    }
  }

  private void drawText(final IMOperation op, final String color, final String text,
      final AnchoringPosition anchoringPosition, final int x, final int y) {
    final String drawSb =
        "gravity " + anchoringPosition.name() + " fill " + color + " text " + x + "," + y + " '" +
            text.replace("'", "\\\'") + "'";
    op.draw(drawSb);
  }
}
