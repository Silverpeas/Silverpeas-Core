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
package org.silverpeas.core.io.media.image.option;


/**
 * @author Yohann Chastagnier
 */
public class DimensionOption extends AbstractImageToolOption {

  private final Integer width;
  private final Integer height;

  /**
   * Creates a new option on the dimension from the specified width and height in pixels.
   * @param width the width in pixels. If null, the height will only be taken into account
   * by ImageTool when resizing an image.
   * @param height the height in pixels. If null, the width will only be taken into account
   * by ImageTool when resizing an image.
   * @return an instance of {@code DimensionOption} type.
   */
  public static DimensionOption widthAndHeight(final Integer width, final Integer height) {
    return new DimensionOption(width, height);
  }

  /**
   * Creates a new option on the dimension from the specified width in pixels. The height will be
   * computed from the width to keep the same ratio than the original image.
   * @param width the width in pixels.
   * @return an instance of {@code DimensionOption} type.
   */
  public static DimensionOption width(final Integer width) {
    return new DimensionOption(width, null);
  }

  /**
   * Creates a new option on the dimension from the specified height in pixels. The width will be
   * computed from the height to keep the same ratio than the original image.
   * @param height the height in pixels.
   * @return an instance of {@code DimensionOption} type.
   */
  public static DimensionOption height(final Integer height) {
    return new DimensionOption(null, height);
  }

  /**
   * Default constructor
   * @param width
   * @param height
   */
  private DimensionOption(final Integer width, final Integer height) {
    this.width = width;
    this.height = height;
  }

  /**
   * @return the width
   */
  public Integer getWidth() {
    return width;
  }

  /**
   * @return the height
   */
  public Integer getHeight() {
    return height;
  }
}
