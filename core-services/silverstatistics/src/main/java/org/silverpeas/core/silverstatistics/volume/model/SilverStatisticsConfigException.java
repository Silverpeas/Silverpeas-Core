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

package org.silverpeas.core.silverstatistics.volume.model;

import org.silverpeas.core.exception.SilverpeasException;

public class SilverStatisticsConfigException extends SilverpeasException {

  private static final long serialVersionUID = 1149703989302775518L;
  private String typeStats;

  public SilverStatisticsConfigException(String callingClass, int errorLevel, String message,
      String typeStats) {
    super(callingClass, errorLevel, message);
    this.typeStats = typeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel, String message,
      String typeStats, String extraParams) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + typeStats, extraParams);
    this.typeStats = typeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel, String message,
      String typeStats, Exception nested) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + typeStats, nested);
    this.typeStats = typeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel, String message,
      String typeStats, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + typeStats, extraParams, nested);
    this.typeStats = typeStats;
  }

  public String getModule() {
    return "SilverStatistic";
  }

  public String getTypeStats() {
    return typeStats;
  }
}
