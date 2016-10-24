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
package org.silverpeas.image;

import java.io.File;
import java.util.Set;

import org.silverpeas.image.option.AbstractImageToolOption;

/**
 * @author Yohann Chastagnier
 */
public interface ImageTool {

  /**
   * Indicates if image tools are available
   * @return
   */
  boolean isActivated();

  /**
   * Gets aimed information from the given image file.
   * @param source the image file.
   * @param options the following options:
   * <ul>
   * <li>%b - file size of image read in</li>
   * <li>%c - comment meta-data property</li>
   * <li>%d - directory component of path</li>
   * <li>%e - filename extension or suffix</li>
   * <li>%f - filename (including suffix)</li>
   * <li>%g - layer canvas page geometry (equivalent to "%Wx%H%X%Y")</li>
   * <li>%h - current image height in pixels</li>
   * <li>%i - image filename (note: becomes output filename for "info:")</li>
   * <li>%k - CALCULATED: number of unique colors</li>
   * <li>%l - label meta-data property</li>
   * <li>%m - image file format (file magic)</li>
   * <li>%n - number of images in current image sequence</li>
   * <li>%o - output filename (used for delegates)</li>
   * <li>%p - index of image in current image list</li>
   * <li>%q - quantum depth (compile-time constant)</li>
   * <li>%r - image class and colorspace</li>
   * <li>%s - scene number (from input unless re-assigned)</li>
   * <li>%t - filename without directory or extension (suffix)</li>
   * <li>%u - unique temporary filename (used for delegates)</li>
   * <li>%w - current width in pixels</li>
   * <li>%x - x resolution (density)</li>
   * <li>%y - y resolution (density)</li>
   * <li>%z - image depth (as read in unless modified, image save depth)</li>
   * <li>%A - image transparency channel enabled (true/false)</li>
   * <li>%C - image compression type</li>
   * <li>%D - image GIF dispose method</li>
   * <li>%G - original image size (%wx%h; before any resizes)</li>
   * <li>%H - page (canvas) height</li>
   * <li>%M - Magick filename (original file exactly as given, including read mods)</li>
   * <li>%O - page (canvas) offset ( = %X%Y )</li>
   * <li>%P - page (canvas) size ( = %Wx%H )</li>
   * <li>%Q - image compression quality ( 0 = default )</li>
   * <li>%S - ?? scenes ??</li>
   * <li>%T - image time delay (in centi-seconds)</li>
   * <li>%U - image resolution units</li>
   * <li>%W - page (canvas) width</li>
   * <li>%X - page (canvas) x offset (including sign)</li>
   * <li>%Y - page (canvas) y offset (including sign)</li>
   * <li>%Z - unique filename (used for delegates)</li>
   * <li>%@ - CALCULATED: trim bounding box (without actually trimming)</li>
   * <li>%# - CALCULATED: 'signature' hash of image values</li>
   * </ul>
   * @return
   */
  String[] getImageInfo(File source, String... options) throws Exception;

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
