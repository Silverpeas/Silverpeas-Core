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
import org.silverpeas.core.io.media.image.AbstractImageTool;
import org.silverpeas.core.io.media.image.ImageToolDirective;
import org.silverpeas.core.io.media.image.option.AbstractImageToolOption;
import org.silverpeas.core.io.media.image.option.BackgroundOption;
import org.silverpeas.core.io.media.image.option.DimensionOption;

import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.Set;

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

  /*
   * (non-Javadoc)
   * @see AbstractImageTool#convert(java.io.File, java.io.File, java.util.Map,
   * java.util.Set)
   */
  @Override
  protected void convert(final File source, final File destination,
      final Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      final Set<ImageToolDirective> directives) throws Exception {

    // Create the operation, add images and operators/options
    final IMOperation op = new IMOperation();

    // Source file
    setSource(op, source, directives);

    // Additional options
    background(op, options);
    resize(op, options, directives);

    // Destination file
    setDestination(op, destination, directives);

    // Executing command
    new ConvertCmd().run(op);
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
}
