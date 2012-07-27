/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverstatistics.model;

import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * Class declaration
 * @author SLR
 */
public class StatisticsConfig {

  private static final String STATSKEYNAME = "StatsKeysName";
  private static final String STATSKEYTYPE = "StatsKeysType";
  private static final String STATSFAMILYTYPE = "StatsFamily";
  private static final String STATSSEPARATOR = "StatsSeparator";
  private static final String STATSKEYSCUMUL = "StatsKeysCumul";
  private static final String STATSTABLENAME = "StatsTableName";
  private static final String STATSRUN = "StatsRun";
  private static final String STATSASYNCHRON = "StatsAsynchron";
  private static final String STATSPURGE = "StatsPurgeInMonth";
  private static final String STATSMODECUMUL = "StatsModeCumul";
  private Map<StatType, TypeStatistics> allStatisticsConfig;
  private boolean initGood;

  /**
   * Constructor declaration
   * @see
   */
  public StatisticsConfig() {
    allStatisticsConfig = new HashMap<StatType, TypeStatistics>();
  }

  public void initialize(ResourceBundle resource) throws SilverStatisticsConfigException {
    String configTypeName = "";
    try {
      initGood = true;
      String tokenSeparator = resource.getString(STATSSEPARATOR);
      StringTokenizer stTypeStats = new StringTokenizer(resource.getString(STATSFAMILYTYPE),
          tokenSeparator);
      while (stTypeStats.hasMoreTokens()) {
        TypeStatistics currentType = new TypeStatistics();
        currentType.setName(stTypeStats.nextToken());
        configTypeName = currentType.getName();
        currentType.setTableName(resource.getString(STATSTABLENAME + currentType.getName()));
        try {
          currentType.setIsRun(
              StringUtil.getBooleanValue(resource.getString(STATSRUN + currentType.getName())));
        } catch (MissingResourceException e) {
          SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setIsRun", e);
        }
        try {
          currentType.setPurge(
              Integer.parseInt(resource.getString(STATSPURGE + currentType.getName())));
        } catch (SilverStatisticsTypeStatisticsException e) {
          SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setPurge", e);
        } catch (MissingResourceException e) {
          SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setPurge", e);
        }

        try {
          currentType.setIsAsynchron(StringUtil.getBooleanValue(
              resource.getString(STATSASYNCHRON + currentType.getName())));
        } catch (MissingResourceException e) {
          SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setIsAsynchron", e);
        }

        try {
          currentType.setModeCumul(StatisticMode.valueOf(resource.getString(STATSMODECUMUL +
              currentType.
              getName())));
        } catch (MissingResourceException e) {
          SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setModeCumul", e);
        }

        StringTokenizer stKeyName =
            new StringTokenizer(resource.getString(STATSKEYNAME + currentType.
            getName()), tokenSeparator);
        StringTokenizer stKeyType =
            new StringTokenizer(resource.getString(STATSKEYTYPE + currentType.
            getName()), tokenSeparator);
        StringTokenizer stKeyCumul =
            new StringTokenizer(resource.getString(STATSKEYSCUMUL + currentType.
            getName()), tokenSeparator);

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
      throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.FATAL,
          "silverstatistics.MSG_CONFIG_FILE", configTypeName, e);
    } catch (MissingResourceException e) {
      initGood = false;
      throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.FATAL,
          "silverstatistics.MSG_CONFIG_FILE_KEY_MISSING", configTypeName, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws SilverStatisticsConfigException
   * @see
   */
  public void init() throws SilverStatisticsConfigException {
    ResourceBundle resource = FileUtil.loadBundle(
        "com.stratelia.silverpeas.silverstatistics.SilverStatistics", Locale.getDefault());
    initialize(resource);
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @return
   * @see
   */
  public Collection<String> getAllKeys(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getAllKeys();
    }
    return new ArrayList<String>();
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @param keyName
   * @return
   * @see
   */
  public String getKeyType(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getKeyType(keyName).name();
    }
    return null;
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @return
   * @see
   */
  public String getTableName(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getTableName();
    }
    return null;
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @return
   * @see
   */
  public StatisticMode getModeCumul(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getModeCumul();
    }
    return null;
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @return
   * @see
   */
  public boolean isRun(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).isRun();
    }
    return false;
  }

  public boolean isAsynchron(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).isAsynchron();
    }
    return false;
  }

  public int getNumberOfStatsType() {
    return allStatisticsConfig.size();
  }

  /**
   * Method declaration
   * @param idFamilyStats
   * @return
   * @see
   */
  public boolean isExist(StatType idFamilyStats) {
    return allStatisticsConfig.containsKey(idFamilyStats);
  }

  /**
   * Method declaration
   * @param idFamilyStats
   * @return
   * @see
   */
  public int getNumberOfKeys(StatType idFamilyStats) {
    if (allStatisticsConfig.containsKey(idFamilyStats)) {
      return allStatisticsConfig.get(idFamilyStats).numberOfKeys();
    }
    return 0;
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @param keyName
   * @return
   * @see
   */
  public boolean isCumulKey(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).isCumulKey(keyName);
    }
    return false;
  }

  /**
   * Method declaration
   * @param StatsType
   * @param keyName
   * @return
   * @see
   */
  public int indexOfKey(StatType typeOfStats, String keyName) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).indexOfKey(keyName);
    }
    return -1;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection<StatType> getAllTypes() {
    return allStatisticsConfig.keySet();
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @return
   * @see
   */
  public int getPurge(StatType typeOfStats) {
    if (allStatisticsConfig.containsKey(typeOfStats)) {
      return allStatisticsConfig.get(typeOfStats).getPurge();
    }
    return 0;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isValidConfigFile() {
    boolean valueReturn = true;
    for (StatType typeCurrent : allStatisticsConfig.keySet()) {
      if (!initGood) {
        valueReturn = false;
      }
      if (!this.isExist(typeCurrent)) {
        valueReturn = false;
      }
      if (!allStatisticsConfig.get(typeCurrent).hasGoodCumulKey()) {
        valueReturn = false;
      }
      if (!allStatisticsConfig.get(typeCurrent).isDateStatKeyExist()) {
        valueReturn = false;
      }
    }
    return valueReturn;
  }

  /**
   * Method declaration
   * @param typeOfStats
   * @param dataArray
   * @return
   * @see
   */
  public boolean isGoodDatas(StatType typeOfStats, List<?> dataArray) {
    return initGood && this.isExist(typeOfStats) && allStatisticsConfig.get(typeOfStats).
        hasGoodCumulKey() && allStatisticsConfig.get(typeOfStats).isDateStatKeyExist() &&
        dataArray.
        size() == this.getNumberOfKeys(typeOfStats);
  }
}
