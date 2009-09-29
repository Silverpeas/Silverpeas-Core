package com.stratelia.silverpeas.silverstatistics.model;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverStatisticsConfigException extends SilverpeasException {
  private String typeStats;

  public SilverStatisticsConfigException(String callingClass, int errorLevel,
      String message, String TypeStats) {
    super(callingClass, errorLevel, message);
    typeStats = TypeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel,
      String message, String TypeStats, String extraParams) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + TypeStats,
        extraParams);
    typeStats = TypeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel,
      String message, String TypeStats, Exception nested) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + TypeStats,
        nested);
    typeStats = TypeStats;
  }

  public SilverStatisticsConfigException(String callingClass, int errorLevel,
      String message, String TypeStats, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message + " TYPE STATS = " + TypeStats,
        extraParams, nested);
    typeStats = TypeStats;
  }

  public String getModule() {
    return "SilverStatistic";
  }

  public String getTypeStats() {
    return typeStats;
  }
}
