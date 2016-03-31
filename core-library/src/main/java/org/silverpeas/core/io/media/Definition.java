/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.io.media;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A container to handle resolution informations.
 * @author: Yohann Chastagnier
 */
public class Definition {

  public static final Definition NULL = of(0, 0);

  private int width = 0;
  private int height = 0;

  private Definition(final int width, final int height) {
    widthOf(width).heightOf(height);
  }

  /**
   * Gets an instance with width and height set to zero.
   * @return
   */
  public static Definition fromZero() {
    return new Definition(0, 0);
  }

  /**
   * Gets an instance initialized with given sizes.
   * @param width
   * @param height
   * @return
   */
  public static Definition of(final int width, final int height) {
    return new Definition(width, height);
  }

  /**
   * Indicates if one one dimension is defined.
   * @return
   */
  public boolean isDefined() {
    return isWidthDefined() || isHeightDefined();
  }

  /**
   * Indicates if width is defined.
   * @return
   */
  public boolean isWidthDefined() {
    return getWidth() > 0;
  }

  /**
   * Indicates if height is defined.
   * @return
   */
  public boolean isHeightDefined() {
    return getHeight() > 0;
  }

  /**
   * Gets the width.
   * @return
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the width.
   * @param width
   */
  public Definition widthOf(final int width) {
    this.width = width;
    return this;
  }

  /**
   * Gets the height.
   * @return
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the height.
   * @param height
   */
  public Definition heightOf(final int height) {
    this.height = height;
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (super.equals(obj)) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Definition other = (Definition) obj;
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getWidth(), other.getWidth());
    matcher.append(getHeight(), other.getHeight());
    return matcher.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getWidth());
    hash.append(getHeight());
    return hash.toHashCode();
  }
}
