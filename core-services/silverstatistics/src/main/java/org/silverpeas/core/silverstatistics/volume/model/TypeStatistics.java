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

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author SLR
 */
public class TypeStatistics {

  public static final String STATISTIC_DATE_KEY = "dateStat";
  private String name;
  private String tableName;
  private int purgeInMonth;
  private List<String> allKeysName;
  private List<StatDataType> allKeysValue;
  private List<String> cumulKeysName;
  private boolean isRun;
  private boolean isAsynchron;
  private StatisticMode modeCumul;

  TypeStatistics() {
    allKeysName = new ArrayList<>();
    allKeysValue = new ArrayList<>();
    cumulKeysName = new ArrayList<>();
    name = "";
    tableName = "";
    purgeInMonth = 3; // use 3 as default
    isRun = true;
    modeCumul = StatisticMode.Add;
    isAsynchron = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) throws SilverStatisticsTypeStatisticsException {
    if (!StringUtil.isDefined(name)) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_NAME_STATS_EMPTY", name);
    }
    this.name = name;
  }

  public StatisticMode getModeCumul() {
    return modeCumul;
  }

  public void setModeCumul(StatisticMode mode) {
    modeCumul = mode;
  }

  public void setIsRun(boolean value) {
    isRun = value;
  }

  public boolean isRun() {
    return isRun;
  }

  public void setIsAsynchron(boolean value) {
    isAsynchron = value;
  }

  public boolean isAsynchron() {
    return isAsynchron;
  }

  public int getPurge() {
    return purgeInMonth;
  }

  public void setPurge(int purgeInMonth) throws SilverStatisticsTypeStatisticsException {
    if (purgeInMonth <= 0) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.ERROR,
          "silverstatistics.MSG_PURGE_BAD_VALUE", name);
    }
    this.purgeInMonth = purgeInMonth;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) throws SilverStatisticsTypeStatisticsException {
    if (!StringUtil.isDefined(tableName)) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_TABLE_NAME_STATS_EMPTY", tableName);
    }
    this.tableName = tableName;
  }

  public Collection<String> getAllKeys() {
    return allKeysName;
  }

  public void setAllKeys(Collection<String> allTags) {
    this.allKeysName.addAll(allTags);
  }

  public void addKey(String keyName, StatDataType keyType)
      throws SilverStatisticsTypeStatisticsException {
    if (!StringUtil.isDefined(keyName)) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_KEY_NAME_STATS_EMPTY", name);
    }
    if (keyType == null) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_TYPE_NAME_STATS_EMPTY", name);
    }
    allKeysName.add(keyName);
    allKeysValue.add(keyType);
  }

  public void addCumulKey(String keyName) throws SilverStatisticsTypeStatisticsException {
    if (!StringUtil.isDefined(keyName)) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_CUMULKEY_NAME_STATS_EMPTY", name);
    }
    if (!hasACumulType(keyName)) {
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics", SilverpeasException.FATAL,
          "silverstatistics.MSG_CUMULKEY_TYPE_STATS_BAD_VALUE", name, keyName);
    }
    cumulKeysName.add(keyName);
  }

  public int indexOfKey(String keyName) {
    return allKeysName.indexOf(keyName);
  }

  public boolean isCumulKey(String keyName) {
    return cumulKeysName.contains(keyName);
  }

  public boolean isDateStatKeyExist() {
    return allKeysName.contains(STATISTIC_DATE_KEY);
  }

  public boolean isAGoodType(String typeName) {
    try {
      StatDataType.valueOf(typeName);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public boolean hasACumulType(String cumulKeyName) {
    StatDataType type = allKeysValue.get(allKeysName.indexOf(cumulKeyName));
    return StatDataType.INTEGER == type || StatDataType.DECIMAL == type;
  }

  public boolean hasGoodCumulKey() {
    return !cumulKeysName.isEmpty();
  }

  public StatDataType getKeyType(String keyName) {
    return allKeysValue.get(allKeysName.indexOf(keyName));
  }

  public int numberOfKeys() {
    return allKeysName.size();
  }
}
