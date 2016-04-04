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

package org.silverpeas.core.silvertrace;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

/**
 * Layouts available for Silvertrace.
 * This class is deprecated as the Silver Trace API is now replaced by the Silverpeas Logger API.
 * @author ehugonnet
 */
@Deprecated
public enum SilverTraceLayout {
  /**
   * Short layout : Display "Time / Priority / Message"
   */
  LAYOUT_SHORT("%-5p : %m%n"),
  /**
   * HTML layout : Display "Time / Thread / Priority / Category / Message" into a TABLE
   */
  LAYOUT_HTML(""),
  /**
   * Detailed layout : Display "Time / Priority / Calling Class and module / Message"
   */
  LAYOUT_DETAILED("%d{dd/MM/yy-HH:mm:ss,SSS} - %-5p : %m%n"),
  /**
   * Fully detailed layout : Display "Tic count / Time / Priority / Thread / Calling Class and
   * module / Message"
   */
  LAYOUT_FULL_DEBUG("%-15.15r [%-26.26t] - %d{dd/MM/yy-HH:mm:ss,SSS} - %-5p : %m%n"),
  /**
   * Fully detailed layout : Display "Time / Priority / Thread / Calling Class and module / Message"
   */
  LAYOUT_SPY("%d{dd/MM/yy-HH:mm:ss,SSS} : %m%n"),
  /**
   * A layout that can be defined by the user.
   */
  LAYOUT_CUSTOM("%-5p : %m%n");

  private String pattern;

  SilverTraceLayout(String pattern) {
    this.pattern = pattern;
  }

  /**
   * Update the layout pattern. This is only available to the LAYOUT_CUSTOM.
   * @param customPattern
   */
  public void updatePattern(String customPattern) {
    if (this == LAYOUT_CUSTOM) {
      this.pattern = customPattern;
    }

  }

  /**
   * Returns the pattern associated with this layout.
   * @return the pattern associated with this layout.
   */
  public String getPattern() {
    return this.pattern;
  }

  public static SilverTraceLayout findByPattern(String pattern) {
    if (LAYOUT_SHORT.getPattern().equals(pattern)) {
      return SilverTraceLayout.LAYOUT_SHORT;
    }
    if (LAYOUT_DETAILED.getPattern().equals(pattern)) {
      return SilverTraceLayout.LAYOUT_DETAILED;
    }
    if (LAYOUT_FULL_DEBUG.getPattern().equals(pattern)) {
      return SilverTraceLayout.LAYOUT_FULL_DEBUG;
    }
    if (LAYOUT_SPY.getPattern().equals(pattern)) {
      return SilverTraceLayout.LAYOUT_SPY;
    }
    return LAYOUT_CUSTOM;
  }

  public static Layout getLayout(String patternLayout) {
    SilverTraceLayout layout;
    try {
      layout = valueOf(patternLayout);
    } catch (IllegalArgumentException e) {
      LAYOUT_CUSTOM.updatePattern(patternLayout);
      layout = LAYOUT_CUSTOM;
    }
    return new PatternLayout(layout.getPattern());
  }

  /**
   * To be used instead of valueOf since it manages the case of a custom pattern.
   * @param name
   * @return
   */
  public static SilverTraceLayout getSilverTraceLayout(String name) {
    SilverTraceLayout layout;
    try {
      layout = valueOf(name);
    } catch (IllegalArgumentException e) {
      LAYOUT_CUSTOM.updatePattern(name);
      layout = LAYOUT_CUSTOM;
    }
    return layout;
  }
}
