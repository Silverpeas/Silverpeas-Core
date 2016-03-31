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
package org.silverpeas.core.io.media.video;

import java.util.EnumSet;
import java.util.Set;


public enum ThumbnailPeriod {
  Thumbnail0(0, 0.1), Thumbnail1(1, 0.3), Thumbnail2(2, 0.5), Thumbnail3(3, 0.7), Thumbnail4(4, 0.9),
  ERROR(-1, 0);

  public static final Set<ThumbnailPeriod> ALL_VALIDS = EnumSet.allOf(ThumbnailPeriod.class);

  static {
    ALL_VALIDS.remove(ERROR);
  }


  public static final String VIDEO_THUMBNAIL_FILE_PREFIX = "img";
  public static final String VIDEO_THUMBNAIL_FILE_EXTENSION = ".jpg";

  private int index;
  private double percent;

  private ThumbnailPeriod(int index, double percent) {
    this.index = index;
    this.percent = percent;
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return the percent
   */
  public double getPercent() {
    return percent;
  }

  /**
   * @return the thumbnail file name
   */
  public String getFilename() {
    return VIDEO_THUMBNAIL_FILE_PREFIX + getIndex() + VIDEO_THUMBNAIL_FILE_EXTENSION;
  }

  public static ThumbnailPeriod fromIndex(String index) {
    if ("0".equals(index)) {
      return Thumbnail0;
    } else if ("1".equals(index)) {
      return Thumbnail1;
    } else if ("2".equals(index)) {
      return Thumbnail2;
    } else if ("3".equals(index)) {
      return Thumbnail3;
    } else if ("4".equals(index)) {
      return Thumbnail4;
    }
    return ERROR;
  }
}
