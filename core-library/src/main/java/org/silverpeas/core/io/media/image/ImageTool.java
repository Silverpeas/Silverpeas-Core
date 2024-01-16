/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.io.media.image;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.io.media.image.option.AbstractImageToolOption;
import org.silverpeas.core.util.ServiceProvider;

import java.io.File;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public interface ImageTool {

  static ImageTool get() {
    return ServiceProvider.getSingleton(ImageTool.class);
  }

  /**
   * Indicates if image tools are available
   * @return
   */
  boolean isActivated();

  /**
   * Gets aimed information from the given image file.
   * @param source the image file.
   * @param infoTypes the aimed types of info.
   * @return
   */
  String[] getImageInfo(File source, ImageInfoType... infoTypes) throws SilverpeasException;

  /**
   * Converts an image with some directives
   * @param source
   * @param destination
   * @param directives
   */
  void convert(File source, File destination, ImageToolDirective... directives);

  /**
   * Converts and resizes an image with dimensions and directives
   * @param source
   * @param destination
   * @param option
   * @param directives
   */
  void convert(File source, File destination, AbstractImageToolOption option,
      ImageToolDirective... directives);

  /**
   * Converts and resizes an image with dimensions and directives
   * @param source
   * @param destination
   * @param options
   * @param directives
   */
  void convert(File source, File destination, Set<AbstractImageToolOption> options,
      ImageToolDirective... directives);
}
