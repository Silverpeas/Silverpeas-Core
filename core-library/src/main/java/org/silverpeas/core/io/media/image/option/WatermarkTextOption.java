/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.io.media.image.option;


/**
 * @author Yohann Chastagnier
 */
public class WatermarkTextOption extends AbstractImageToolOption implements Margins {

  private final String text;
  private String font = "Arial";
  private AnchoringPosition anchoringPosition = AnchoringPosition.SOUTH_EAST;
  private Integer marginX = null;
  private Integer marginY = null;

  /**
   * Default constructor
   * @param text
   */
  private WatermarkTextOption(final String text) {
    this.text = text;
  }

  public static WatermarkTextOption text(final String text) {
    return new WatermarkTextOption(text);
  }

  /**
   * @return the value
   */
  public String getText() {
    return text;
  }

  public String getFont() {
    return font;
  }

  public WatermarkTextOption withFont(final String font) {
    this.font = font;
    return this;
  }

  public AnchoringPosition getAnchoringPosition() {
    return anchoringPosition;
  }

  public WatermarkTextOption withAnchoringPosition(final AnchoringPosition anchoringPosition) {
    this.anchoringPosition = anchoringPosition;
    return this;
  }

  @Override
  public Integer getMarginX() {
    return marginX;
  }

  @Override
  public Integer getMarginY() {
    return marginY;
  }

  @Override
  public WatermarkTextOption withMargins(final int x, final int y) {
    this.marginX = x;
    this.marginY = y;
    return this;
  }
}
