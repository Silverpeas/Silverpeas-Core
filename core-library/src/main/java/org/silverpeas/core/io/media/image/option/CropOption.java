/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.io.media.image.option;


/**
 * @author silveryocha
 */
public class CropOption extends AbstractImageToolOption {

  private final int width;
  private final int height;
  private int offsetX = 0;
  private int offsetY = 0;
  private boolean removePartsAroundCroppedZone = true;

  private CropOption(final Integer width, final Integer height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Creates a new option of cropping from the specified width and height in pixels.
   * @param width the width of crop in pixels.
   * @param height the height of crop in pixels.
   * @return an instance of {@code CropOption} type.
   */
  public static CropOption crop(final int width, int height) {
    return new CropOption(width, height);
  }

  /**
   * Indicating the offset from which the crop MUST be performed. The offset is computed from top
   * left of the image.
   * <p>
   *   By default, offset is [0,0] coordinate.
   * </p>
   * @param offsetX an integer on the horizontal axis.
   * @param offsetY an integer on the vertical axis.
   * @return the {@link CropOption} instance.
   */
  public CropOption withOffset(final int offsetX, final int offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    return this;
  }

  /**
   * By default, on a crop operation the parts of image around the cropped zone is removed. In
   * the case these parts are needed, just call this
   * method.
   * @return the {@link CropOption} instance.
   */
  public CropOption keepPartsAroundCroppedZone() {
    this.removePartsAroundCroppedZone = false;
    return this;
  }

  public Integer getWidth() {
    return width;
  }

  public Integer getHeight() {
    return height;
  }

  public Integer getOffsetX() {
    return offsetX;
  }

  public Integer getOffsetY() {
    return offsetY;
  }

  public boolean mustRemovePartsAroundCroppedZone() {
    return removePartsAroundCroppedZone;
  }
}
