/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.search;

import org.silverpeas.core.util.URLUtil;

/**
 * Class used to avoid and remove JSP scriptlet
 */
public class ResultSearchRendererUtil {

  /**
   * @return an HTML empty star icon img element
   */
  private static String getEmptyStarIcon() {
    return "<img src=\"" + URLUtil.getApplicationURL() +
    "/pdcPeas/jsp/icons/pdcPeas_emptyStar.gif\"/>";
  }

  /**
   * @return an HTML full star icon img element
   */
  private static String getFullStarIcon() {
    return "<img src=\"" + URLUtil.getApplicationURL() + "/pdcPeas/jsp/icons/starGreen.gif\"/>";
  }

  /**
   * @param score the relevant score
   * @return HTML relevant display
   */
  public static String displayPertinence(float score) {
    String fullStarSrc = getFullStarIcon();
    String emptyStarSrc = getEmptyStarIcon();

    StringBuilder stars = new StringBuilder();
    if (score <= 0.2) {
      for (int l = 0; l < 1; l++) {
        stars.append(fullStarSrc);
      }
      for (int k = 2; k <= 5; k++) {
        stars.append(emptyStarSrc);
      }
    } else if (score > 0.2 && score <= 0.4) {
      for (int l = 0; l < 2; l++) {
        stars.append(fullStarSrc);
      }
      for (int k = 3; k <= 5; k++) {
        stars.append(emptyStarSrc);
      }
    } else if (score > 0.4 && score <= 0.6) {
      for (int l = 0; l < 3; l++) {
        stars.append(fullStarSrc);
      }
      for (int k = 4; k <= 5; k++) {
        stars.append(emptyStarSrc);
      }
    } else if (score > 0.6 && score <= 0.8) {
      for (int l = 0; l < 4; l++) {
        stars.append(fullStarSrc);
      }
      stars.append(emptyStarSrc);
    } else if (score > 0.8) {
      for (int l = 0; l < 5; l++) {
        stars.append(fullStarSrc);
      }
    }
    return stars.toString();
  }

}
