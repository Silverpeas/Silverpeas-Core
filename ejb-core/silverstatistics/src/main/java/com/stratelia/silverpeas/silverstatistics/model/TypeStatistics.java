/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverstatistics.model;

import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 * @author SLR
 */

public class TypeStatistics {

  private String name;
  private String tableName;
  private int purgeInMonth;
  private ArrayList allKeysName;
  private ArrayList allKeysValue;
  private ArrayList cumulKeysName;
  private boolean isRun;
  private boolean isAsynchron;
  private String modeCumul;

  public static String MODEADD = new String("Add");
  public static String MODEREPLACE = new String("Replace");

  TypeStatistics() {
    allKeysName = new ArrayList();
    allKeysValue = new ArrayList();
    cumulKeysName = new ArrayList();
    name = "";
    tableName = "";
    purgeInMonth = 3; // use 3 as default
    isRun = true;
    modeCumul = MODEADD;
    isAsynchron = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name)
      throws SilverStatisticsTypeStatisticsException {
    if (name == null)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL, "silverstatistics.MSG_NAME_STATS_NULL",
          name);
    if (name.equals(""))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL, "silverstatistics.MSG_NAME_STATS_EMPTY",
          name);
    this.name = name;
  }

  public String getModeCumul() {
    return modeCumul;
  }

  public void setModeCumul(String mode)
      throws SilverStatisticsTypeStatisticsException {
    // if (mode == null) throw new
    // SilverStatisticsTypeStatisticsException("TypeStatistics",
    // SilverpeasException.FATAL, "silverstatistics.MSG_NAME_STATS_NULL", name);
    // if (mode.equals("")) throw new
    // SilverStatisticsTypeStatisticsException("TypeStatistics",
    // SilverpeasException.FATAL, "silverstatistics.MSG_NAME_STATS_EMPTY",
    // name);
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

  public void setPurge(int purgeInMonth)
      throws SilverStatisticsTypeStatisticsException {
    if (purgeInMonth <= 0)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.ERROR, "silverstatistics.MSG_PURGE_BAD_VALUE",
          name);
    this.purgeInMonth = purgeInMonth;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String name)
      throws SilverStatisticsTypeStatisticsException {
    if (name == null)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_TABLE_NAME_STATS_NULL", name);
    if (name.equals(""))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_TABLE_NAME_STATS_EMPTY", name);
    this.tableName = name;
  }

  public Collection getAllKeys() {
    return allKeysName;
  }

  public void setAllKeys(Collection allTags) {
    this.allKeysName.addAll(allTags);
  }

  public void addKey(String keyName, String keyType)
      throws SilverStatisticsTypeStatisticsException {
    if (keyName == null)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_KEY_NAME_STATS_NULL", name);
    if (keyName.equals(""))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_KEY_NAME_STATS_EMPTY", name);
    if (keyType == null)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_TYPE_NAME_STATS_NULL", name);
    if (keyType.equals(""))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_TYPE_NAME_STATS_EMPTY", name);
    if (!isAGoodType(keyType))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_TYPE_NAME_STATS_NOT_EXISTS", name, keyType);
    allKeysName.add(keyName);
    allKeysValue.add(keyType);
  }

  public void addCumulKey(String keyName)
      throws SilverStatisticsTypeStatisticsException {
    if (keyName == null)
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_CUMULKEY_NAME_STATS_NULL", name);
    if (keyName.equals(""))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_CUMULKEY_NAME_STATS_EMPTY", name);
    if (!hasACumulType(keyName))
      throw new SilverStatisticsTypeStatisticsException("TypeStatistics",
          SilverpeasException.FATAL,
          "silverstatistics.MSG_CUMULKEY_TYPE_STATS_BAD_VALUE", name, keyName);
    cumulKeysName.add(keyName);
  }

  public int indexOfKey(String keyName) {
    return allKeysName.indexOf(keyName);
  }

  public boolean isCumulKey(String keyName) {
    return cumulKeysName.contains(keyName);
  }

  public boolean isDateStatKeyExist() {
    return allKeysName.contains("dateStat");
  }

  public boolean isAGoodType(String typeName) {
    if (typeName.equals("INTEGER"))
      return true;
    if (typeName.equals("DECIMAL"))
      return true;
    if (typeName.equals("VARCHAR"))
      return true;

    return false;
  }

  public boolean hasACumulType(String cumulKeyName) {
    if (((String) (allKeysValue.get(allKeysName.indexOf(cumulKeyName))))
        .equals("INTEGER"))
      return true;
    if (((String) (allKeysValue.get(allKeysName.indexOf(cumulKeyName))))
        .equals("DECIMAL"))
      return true;

    return false;
  }

  public boolean hasGoodCumulKey() {
    boolean valueReturn = true;

    if (cumulKeysName.size() > 0)
      valueReturn = true;
    else
      valueReturn = false;

    return valueReturn;
  }

  public String getKeyType(String keyName) {
    return (String) (allKeysValue.get(allKeysName.indexOf(keyName)));
  }

  public int numberOfKeys() {
    return allKeysName.size();
  }
}
