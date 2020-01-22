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
package org.silverpeas.core.io.media.image.imagemagick;

import org.apache.commons.io.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.MogrifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.io.media.image.AbstractImageTool;
import org.silverpeas.core.io.media.image.ImageInfoType;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.ImageToolDirective;
import org.silverpeas.core.io.media.image.option.*;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.silverpeas.core.io.media.image.ImageInfoType.*;

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
    final boolean sourceIsDestination = source.equals(destination);

    // Create the operation, add images and operators/options
    final IMOperation op = new IMOperation();

    // Source file
    if (!sourceIsDestination) {
      setSource(op, source, directives);
    }

    try {
      // Additional options
      orientation(op, source, options, directives);
      transparencyColor(op, options);
      background(op, options);
      resize(op, options, directives);
      watermarkText(op, source, options);
      watermarkImage(op, source, options);

      // Destination file
      setDestination(op, destination);

      // Executing command
      if (sourceIsDestination) {
        new MogrifyCmd().run(op);
      } else {
        new ConvertCmd().run(op);
      }
    } catch (NoWorkToDo e) {
      if (!sourceIsDestination) {
        try {
          FileUtils.copyFile(source, destination);
        } catch (IOException ex) {
          throw new SilverpeasException(ex);
        }
      }
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

  private void setDestination(final IMOperation op, final File destination) {
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
   * Centralizes orientation handling
   * @param op
   * @param options
   */
  private void orientation(final IMOperation op, final File source,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final Set<ImageToolDirective> directives) throws NoWorkToDo {

    // Getting orientation option
    final OrientationOption option = getOption(options, OrientationOption.class);
    if (option != null) {
      checkIftImageMustBeProcessed(source, option, options, directives);
      if (option.getOrientation() == Orientation.AUTO) {
        op.autoOrient();
      } else {
        op.orient(option.getOrientation().getToolName());
      }
    }
  }

  private void checkIftImageMustBeProcessed(final File source, final OrientationOption option,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final Set<ImageToolDirective> directives) throws NoWorkToDo {
    if (option.isModifyingImageOnlyIfNecessary() && options.size() == 1 && directives.isEmpty()) {
      final Mutable<Orientation> currentOrientation = Mutable.empty();
      try {
        Stream.of(ImageTool.get().getImageInfo(source, ORIENTATION))
            .map(Orientation::decode)
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(currentOrientation::set);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
      if (!currentOrientation.isPresent()
          || (option.getOrientation() == Orientation.AUTO && currentOrientation.get() == Orientation.TOP_LEFT)
          || option.getOrientation() == currentOrientation.get()) {
        throw new NoWorkToDo();
      }
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
    final TransparencyColorOption transparencyColor = getOption(options,
        TransparencyColorOption.class);
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
      final Integer[] imageInfo = getWidthAndHeight(source, options, false);
      int width = imageInfo[0];
      int height = imageInfo[1];
      int[] margins = computeMargins(width, 0.01, height, 0.1, watermarkText);

      int pointSize = (int) Math.rint((width * 0.02) + Math.log(width));
      pointSize = Math.min((int) (height * 0.2), pointSize);

      int minX = margins[0];
      int minY = margins[1];
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
        "gravity " + anchoringPosition.getToolName() + " fill " + color + " text " + x + "," + y +
            " '" + text.replace("'", "\\\'") + "'";
    op.draw(drawSb);
  }

  /**
   * Centralizes image watermarking operation.
   * @param op the IM operations.
   * @param source the image source.
   * @param options the image options.
   */
  private void watermarkImage(final IMOperation op, final File source,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options)
      throws SilverpeasException {
    WatermarkImageOption watermarkImage = getOption(options, WatermarkImageOption.class);
    if (watermarkImage != null) {
      final Integer[] srcInfo = getWidthAndHeight(source, options, false);
      int srcWidth = srcInfo[0];
      int srcHeight = srcInfo[1];
      int[] margins = computeMargins(srcWidth, 0.01, srcHeight, 0.1, watermarkImage);
      int marginX = margins[0];
      int marginY = margins[1];
      srcWidth -= (marginX * 2);
      srcHeight -= (marginY * 2);

      final File watermark = watermarkImage.getImage();
      final Integer[] wInfo = getWidthAndHeight(watermark, options, true);
      float wWidth = (float) wInfo[0];
      float wHeight = (float) wInfo[1];

      int width = 0;
      int height = 0;
      if (srcWidth < wWidth) {
        width = srcWidth;
        height = Math.round(((float) srcWidth) / (wWidth / wHeight));
      }
      if (srcHeight < wHeight) {
        final int widthFromHeight = Math.round(((float) srcHeight) * (wWidth / wHeight));
        if (widthFromHeight < srcWidth) {
          width = widthFromHeight;
          height = srcHeight;
        }
      }

      final AnchoringPosition anchoringPosition = watermarkImage.getAnchoringPosition();
      if (anchoringPosition == AnchoringPosition.TILE) {
        op.tile();
      } else {
        op.gravity(anchoringPosition.getToolName());
      }
      op.draw(MessageFormat
          .format("image {0} {1},{2} {3},{4} ''{5}''",
              watermarkImage.getCompositionOperation().getToolName(),
              String.valueOf(marginX),
              String.valueOf(marginY),
              String.valueOf(width),
              String.valueOf(height),
              watermark.getPath()));
    }
  }

  private int[] computeMargins(final int width, final double wFactor, final int height,
      final double hFactor, final Margins margins) {
    int marginOffset = (int) Math.rint((width * wFactor) + Math.log(width));
    marginOffset = Math.min((int) (height * hFactor), marginOffset);
    int marginX = margins.getMarginX() != null ? margins.getMarginX() : marginOffset;
    int marginY = margins.getMarginY() != null ? margins.getMarginY() : marginOffset;
    return new int[]{marginX, marginY};
  }

  /**
   * Gets width and height of the source by taking care about dimension and orientation options.
   * @param source the source.
   * @param options the options.
   * @param skipOptions true to skip the options.
   * @return array containing width and height.
   * @throws SilverpeasException in case of technical error.
   */
  private Integer[] getWidthAndHeight(final File source,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final boolean skipOptions)
      throws SilverpeasException {
    final Orientation orientation = Orientation.decode(getImageInfo(source, ORIENTATION)[0]);
    final ImageInfoType[] imageInfoTypes =
        (skipOptions || orientation == null || orientation.ordinal() <= 3)
        ? new ImageInfoType[]{WIDTH_IN_PIXEL, HEIGHT_IN_PIXEL}
        : new ImageInfoType[]{HEIGHT_IN_PIXEL, WIDTH_IN_PIXEL};
    final Integer[] imageInfo = Stream.of(getImageInfo(source, imageInfoTypes))
                                      .map(Integer::parseInt)
                                      .toArray(Integer[]::new);
    final DimensionOption dimension = skipOptions
        ? null
        : getOption(options, DimensionOption.class);
    if (dimension != null) {
      int width = imageInfo[0];
      int height = imageInfo[1];
      float ratio = ((float) width) / ((float) height);
      if (dimension.getWidth() != null && dimension.getWidth() < width) {
        width = dimension.getWidth();
        height = Math.round(((float) dimension.getWidth()) / ratio);
      }
      if (dimension.getHeight() != null && dimension.getHeight() < height) {
        width = Math.round(((float) dimension.getHeight()) * ratio);
        height = dimension.getHeight();
      }
      imageInfo[0] = width;
      imageInfo[1] = height;
    }
    return imageInfo;
  }

  /**
   * Indicates the processing can be skipped as there is no work to perform on the image.
   */
  private static class NoWorkToDo extends Exception {
    private static final long serialVersionUID = -3695880835319345867L;
  }
}
