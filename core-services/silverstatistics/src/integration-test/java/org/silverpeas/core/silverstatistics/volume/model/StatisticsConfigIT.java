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

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.silverstatistics.test.WarBuilder4Statistics;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class StatisticsConfigIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Statistics.onWarForTestClass(StatisticsConfigIT.class)
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.silverstatistics");
          war.addAsResource("org/silverpeas/silverstatistics/SilverStatisticsTest.properties");
        }).build();
  }

  private StatisticsConfig instance;

  @Before
  public void initialiseConfig() throws Exception {
    instance = new StatisticsConfig();
    SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.silverstatistics.SilverStatisticsTest");
    instance.initialize(settings);
  }

  /**
   * Test of getAllKeys method, of class StatisticsConfig.
   */
  @Test
  public void testGetAllKeys() throws Exception {
    StatType typeOfStats = StatType.Connexion;
    Collection<String> keys = instance.getAllKeys(typeOfStats);
    assertThat(keys, notNullValue());
    assertThat(keys, hasSize(4));
    assertThat(keys, hasItems("dateStat", "userId", "countConnection", "duration"));
  }

  /**
   * Test of getKeyType method, of class StatisticsConfig.
   */
  @Test
  public void testGetKeyType() throws Exception {
    String result = instance.getKeyType(StatType.Connexion, "userId");
    assertThat(StatDataType.INTEGER.name(), is(result));
    result = instance.getKeyType(StatType.Connexion, "dateStat");
    assertThat(StatDataType.VARCHAR.name(), is(result));
  }

  /**
   * Test of getTableName method, of class StatisticsConfig.
   */
  @Test
  public void testGetTableName() throws Exception {
    String result = instance.getTableName(StatType.Connexion);
    assertThat("SB_Stat_Connection", is(result));
    result = instance.getTableName(StatType.Volume);
    assertThat("SB_Stat_Volume", is(result));
  }

  /**
   * Test of getModeCumul method, of class StatisticsConfig.
   */
  @Test
  public void testGetModeCumul() throws Exception {
    StatType typeOfStats = StatType.Connexion;
    StatisticMode result = instance.getModeCumul(typeOfStats);
    assertThat(StatisticMode.Add, is(result));
    typeOfStats = StatType.Size;
    result = instance.getModeCumul(typeOfStats);
    assertThat(StatisticMode.Replace, is(result));
  }

  /**
   * Test of isRun method, of class StatisticsConfig.
   */
  @Test
  public void testIsRun() throws Exception {
    boolean result = instance.isRun(StatType.Connexion);
    assertThat(result, is(true));
    result = instance.isRun(StatType.Access);
    assertThat(result, is(false));
  }

  /**
   * Test of isAsynchron method, of class StatisticsConfig.
   */
  @Test
  public void testIsAsynchron() throws Exception {
    boolean result = instance.isAsynchron(StatType.Connexion);
    assertThat(result, is(true));
    result = instance.isAsynchron(StatType.Volume);
    assertThat(result, is(false));
  }

  /**
   * Test of getNumberOfStatsType method, of class StatisticsConfig.
   */
  @Test
  public void testGetNumberOfStatsType() {
    int expResult = 4;
    int result = instance.getNumberOfStatsType();
    assertThat(expResult, is(result));
  }

  /**
   * Test of isExist method, of class StatisticsConfig.
   */
  @Test
  public void testIsExist() {
    StatType idFamilyStats = StatType.Connexion;
    boolean result = instance.isExist(idFamilyStats);
    assertThat(result, is(true));
    idFamilyStats = StatType.Volume;
    result = instance.isExist(idFamilyStats);
    assertThat(result, is(true));
  }

  /**
   * Test of getNumberOfKeys method, of class StatisticsConfig.
   */
  @Test
  public void testGetNumberOfKeys() {
    StatType idFamilyStats = StatType.Connexion;
    int expResult = 4;
    int result = instance.getNumberOfKeys(idFamilyStats);
    assertThat(expResult, is(result));
    idFamilyStats = StatType.Access;
    expResult = 6;
    result = instance.getNumberOfKeys(idFamilyStats);
    assertThat(expResult, is(result));
  }

  /**
   * Test of isCumulKey method, of class StatisticsConfig.
   */
  @Test
  public void testIsCumulKey() {
    StatType typeOfStats = StatType.Connexion;
    String cumulKeyName = "countConnection";
    boolean result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertThat(result, is(true));
    cumulKeyName = "duration";
    result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertThat(result, is(true));
    String notCumulKeyName = "userId";
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertThat(result, is(false));
    notCumulKeyName = RandomStringUtils.random(32, true, true);
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertThat(result, is(false));
  }

  /**
   * Test of indexOfKey method, of class StatisticsConfig.
   */
  @Test
  public void testIndexOfKey() {
    StatType typeOfStats = StatType.Connexion;
    String existingKeyName = "countConnection";
    int result = instance.indexOfKey(typeOfStats, existingKeyName);
    assertThat(2, is(result));
    String notExistingKeyName = RandomStringUtils.random(32, true, true);
    result = instance.indexOfKey(typeOfStats, notExistingKeyName);
    assertThat(-1, is(result));
  }

  /**
   * Test of getAllTypes method, of class StatisticsConfig.
   */
  @Test
  public void testGetAllTypes() throws Exception {
    assertThat(instance, notNullValue());
    Collection<StatType> types = instance.getAllTypes();
    assertThat(types, notNullValue());
    assertThat(types, hasSize(4));
    assertThat(types,
        hasItems(StatType.Connexion, StatType.Size, StatType.Access, StatType.Volume));
  }

  /**
   * Test of getPurge method, of class StatisticsConfig.
   */
  @Test
  public void testGetPurge() {
    StatType typeOfStats = StatType.Connexion;
    int expResult = 120;
    int result = instance.getPurge(typeOfStats);
    assertThat(expResult, is(result));
    typeOfStats = StatType.Volume;
    expResult = 80;
    result = instance.getPurge(typeOfStats);
    assertThat(expResult, is(result));
  }

  /**
   * Test of isValidConfigFile method, of class StatisticsConfig.
   */
  @Test
  public void testIsValidConfigFile() {
    assertThat(instance.isValidConfigFile(), is(true));
  }

  /**
   * Test of isGoodDatas method, of class StatisticsConfig.
   */
  @Test
  public void testIsGoodDatas() {
    StatType typeOfStats = StatType.Size;
    List<String> dataArray = new ArrayList<>(4);
    dataArray.add(RandomStringUtils.random(32, true, true));
    dataArray.add(RandomStringUtils.random(32, true, true));
    dataArray.add(RandomStringUtils.random(32, true, true));
    dataArray.add(RandomStringUtils.random(32, true, true));
    assertThat("Should not have only 3 data elements",
        instance.areGoodData(typeOfStats, dataArray), is(false));
    typeOfStats = StatType.Connexion;
    assertThat(instance.areGoodData(typeOfStats, dataArray), is(true));
  }
}
