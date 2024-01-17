/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.silverstatistics.volume.model;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.*;

/**
 * Class declaration
 * @author SLR
 */
public class StatisticsConfig {

  private static final String STATS_KEYS_NAME = "StatsKeysName";
  private static final String STATS_KEYS_TYPE = "StatsKeysType";
  private static final String STATS_FAMILY = "StatsFamily";
  private static final String STATS_SEPARATOR = "StatsSeparator";
  private static final String STATS_KEYS_CUMUL = "StatsKeysCumul";
  private static final String STATS_TABLE_NAME = "StatsTableName";
  private static final String STATS_RUN = "StatsRun";
  private static final String STATS_ASYNCHRON = "StatsAsynchron";
  private static final String STATS_PURGE_IN_MONTH = "StatsPurgeInMonth";
  private static final String STATS_MODE_CUMUL = "StatsModeCumul";
  private final Map<StatType, TypeStatistics> allStatisticsConfig;
  private boolean initGood;

  /**
   * Constructor declaration
   */
  public StatisticsConfig() {
    allStatisticsConfig = new EnumMap<>(StatType.class);
  }

  public void initialize(SettingBundle settings) throws SilverStatisticsConfigException {
    String configTypeName = "";
    try {
      initGood = true;
      String tokenSeparator = settings.getString(STATS_SEPARATOR);
      StringTokenizer stTypeStats =
          new StringTokenizer(settings.getString(STATS_FAMILY), tokenSeparator);
      while (stTypeStats.hasMoreTokens()) {
        TypeStatistics currentType = new TypeStatistics();
        currentType.setName(stTypeStats.nextToken());
        configTypeName = currentType.getName();
        initTypeStatistics(settings, currentType);

        StringTokenizer stKeyName =
            new StringTokenizer(settings.getString(STATS_KEYS_NAME + currentType.getName()),
                tokenSeparator);
        StringTokenizer stKeyType =
            new StringTokenizer(settings.getString(STATS_KEYS_TYPE + currentType.getName()),
                tokenSeparator);
        StringTokenizer stKeyCumul =
            new StringTokenizer(settings.getString(STATS_KEYS_CUMUL + currentType.getName()),
                tokenSeparator);

        while (stKeyName.hasMoreTokens()) {
          currentType.addKey(stKeyName.nextToken(), StatDataType.valueOf(stKeyType.nextToken()));
        }
        while (stKeyCumul.hasMoreTokens()) {
          currentType.addCumulKey(stKeyCumul.nextToken());
        }
        allStatisticsConfig.put(StatType.valueOf(currentType.getName()), currentType);
      }
    } catch (SilverStatisticsTypeStatisticsException e) {
      initGood = false;
      throw new SilverStatisticsConfigException("Error with config file", configTypeName, e);
    } catch (MissingResourceException e) {
      initGood = false;
      throw new SilverStatisticsConfigException("setting missing in config file", configTypeName, e);
    }
  }

  private static void initTypeStatistics(SettingBundle settings, TypeStatistics currentType) throws SilverStatisticsTypeStatisticsException {
    currentType.setTableName(settings.getString(STATS_TABLE_NAME + currentType.getName()));
    try {
      currentType.setIsRun(settings.getBoolean(STATS_RUN + currentType.getName()));
      currentType.setPurge(settings.getInteger(STATS_PURGE_IN_MONTH + currentType.getName()));
      currentType.setIsAsynchron(settings.getBoolean(STATS_ASYNCHRON + currentType.getName()));
      currentType
          .setModeCumul(StatisticMode.valueOf(settings.getString(STATS_MODE_CUMUL + currentType.
              getName())));
    } catch (SilverStatisticsTypeStatisticsException | MissingResourceException e) {
      // nothing to do
    }
  }

  public void init() throws SilverStatisticsConfigException {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.silverstatistics.SilverStatistics");
    initialize(settings);
  }

  public List<String> getAllKeys(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getAllKeys();
    }
    return new ArrayList<>();
  }

  public String getKeyType(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getKeyType(keyName).name();
    }
    return null;
  }

  /**
   * Gets the table name of the specified statistics type.
   * @param typeOfStats type of statistics
   * @return table name which is linked to this type of statistics
   */
  public String getTableName(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getTableName();
    }
    return null;
  }

  public StatisticMode getModeCumul(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getModeCumul();
    }
    return null;
  }

  public boolean isRun(StatType typeOfStats) {
    return allStatisticsConfig.containsKey(typeOfStats) &&
        allStatisticsConfig.get(typeOfStats).isRun();
  }

  public boolean isAsynchron(StatType typeOfStats) {
    return allStatisticsConfig.containsKey(typeOfStats) &&
        allStatisticsConfig.get(typeOfStats).isAsynchron();
  }

  public int getNumberOfStatsType() {
    return allStatisticsConfig.size();
  }

  public boolean isExist(StatType idFamilyStats) {
    return allStatisticsConfig.containsKey(idFamilyStats);
  }

  public int getNumberOfKeys(StatType idFamilyStats) {
    if (allStatisticsConfig.containsKey(idFamilyStats)) {
      return allStatisticsConfig.get(idFamilyStats).numberOfKeys();
    }
    return 0;
  }

  public boolean isCumulKey(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).isCumulKey(keyName);
    }
    return false;
  }

  public int indexOfKey(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).indexOfKey(keyName);
    }
    return -1;
  }

  public Collection<StatType> getAllTypes() {
    return allStatisticsConfig.keySet();
  }

  public int getPurge(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getPurge();
    }
    return 0;
  }

  /**
   * Is this configuration valid?
   * @return true if configuration file is valid, false else if
   */
  public boolean isValidConfigFile() {
    boolean valueReturn = true;
    for (var currentType : allStatisticsConfig.entrySet()) {
      if (!initGood) {
        valueReturn = false;
      }
      if (!this.isExist(currentType.getKey())) {
        valueReturn = false;
      }
      if (!currentType.getValue().hasGoodCumulKey()) {
        valueReturn = false;
      }
      if (!currentType.getValue().isDateStatKeyExist()) {
        valueReturn = false;
      }
    }
    return valueReturn;
  }

  /**
   * Are the data well formed for the specified type of statistics.
   * @param typeOfStats the type of statistics.
   * @param data the data to check.
   * @return true if data are well formed, false else if
   */
  public boolean areGoodData(StatType typeOfStats, List<String> data) {
    return initGood && this.isExist(typeOfStats) &&
        allStatisticsConfig.get(typeOfStats).hasGoodCumulKey() &&
        allStatisticsConfig.get(typeOfStats).isDateStatKeyExist() &&
        data.size() == this.getNumberOfKeys(typeOfStats);
  }
}
