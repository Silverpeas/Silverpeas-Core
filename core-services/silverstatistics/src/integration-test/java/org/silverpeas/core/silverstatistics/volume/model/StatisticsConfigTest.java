/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.silverstatistics.volume.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class StatisticsConfigTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(StatisticsConfigTest.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.silverstatistics");
          war.addAsResource("org/silverpeas/silverstatistics/SilverStatisticsTest.properties");
          war.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
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
    assertNotNull(keys);
    assertThat(keys, hasSize(4));
    assertThat(keys, hasItems("dateStat", "userId", "countConnection", "duration"));
  }

  /**
   * Test of getKeyType method, of class StatisticsConfig.
   */
  @Test
  public void testGetKeyType() throws Exception {
    String result = instance.getKeyType(StatType.Connexion, "userId");
    assertEquals(StatDataType.INTEGER.name(), result);
    result = instance.getKeyType(StatType.Connexion, "dateStat");
    assertEquals(StatDataType.VARCHAR.name(), result);
  }

  /**
   * Test of getTableName method, of class StatisticsConfig.
   */
  @Test
  public void testGetTableName() throws Exception {
    String result = instance.getTableName(StatType.Connexion);
    assertEquals("SB_Stat_Connection", result);
    result = instance.getTableName(StatType.Volume);
    assertEquals("SB_Stat_Volume", result);
  }

  /**
   * Test of getModeCumul method, of class StatisticsConfig.
   */
  @Test
  public void testGetModeCumul() throws Exception {
    StatType typeOfStats = StatType.Connexion;
    StatisticMode result = instance.getModeCumul(typeOfStats);
    assertEquals(StatisticMode.Add, result);
    typeOfStats = StatType.Size;
    result = instance.getModeCumul(typeOfStats);
    assertEquals(StatisticMode.Replace, result);
  }

  /**
   * Test of isRun method, of class StatisticsConfig.
   */
  @Test
  public void testIsRun() throws Exception {
    boolean result = instance.isRun(StatType.Connexion);
    assertTrue(result);
    result = instance.isRun(StatType.Access);
    assertFalse(result);
  }

  /**
   * Test of isAsynchron method, of class StatisticsConfig.
   */
  @Test
  public void testIsAsynchron() throws Exception {
    boolean result = instance.isAsynchron(StatType.Connexion);
    assertTrue(result);
    result = instance.isAsynchron(StatType.Volume);
    assertFalse(result);
  }

  /**
   * Test of getNumberOfStatsType method, of class StatisticsConfig.
   */
  @Test
  public void testGetNumberOfStatsType() {
    int expResult = 4;
    int result = instance.getNumberOfStatsType();
    assertEquals(expResult, result);
  }

  /**
   * Test of isExist method, of class StatisticsConfig.
   */
  @Test
  public void testIsExist() {
    StatType idFamilyStats = StatType.Connexion;
    boolean result = instance.isExist(idFamilyStats);
    assertTrue(result);
    idFamilyStats = StatType.Volume;
    result = instance.isExist(idFamilyStats);
    assertTrue(result);
  }

  /**
   * Test of getNumberOfKeys method, of class StatisticsConfig.
   */
  @Test
  public void testGetNumberOfKeys() {
    StatType idFamilyStats = StatType.Connexion;
    int expResult = 4;
    int result = instance.getNumberOfKeys(idFamilyStats);
    assertEquals(expResult, result);
    idFamilyStats = StatType.Access;
    expResult = 6;
    result = instance.getNumberOfKeys(idFamilyStats);
    assertEquals(expResult, result);
  }

  /**
   * Test of isCumulKey method, of class StatisticsConfig.
   */
  @Test
  public void testIsCumulKey() {
    StatType typeOfStats = StatType.Connexion;
    String cumulKeyName = "countConnection";
    boolean result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertTrue(result);
    cumulKeyName = "duration";
    result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertTrue(result);
    String notCumulKeyName = "userId";
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertFalse(result);
    notCumulKeyName = RandomStringUtils.random(32, true, true);
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertFalse(result);
  }

  /**
   * Test of indexOfKey method, of class StatisticsConfig.
   */
  @Test
  public void testIndexOfKey() {
    StatType typeOfStats = StatType.Connexion;
    String existingKeyName = "countConnection";
    int result = instance.indexOfKey(typeOfStats, existingKeyName);
    assertEquals(2, result);
    String notExistingKeyName = RandomStringUtils.random(32, true, true);
    result = instance.indexOfKey(typeOfStats, notExistingKeyName);
    assertEquals(-1, result);
  }

  /**
   * Test of getAllTypes method, of class StatisticsConfig.
   */
  @Test
  public void testGetAllTypes() throws Exception {
    assertNotNull(instance);
    Collection<StatType> types = instance.getAllTypes();
    assertNotNull(types);
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
    assertEquals(expResult, result);
    typeOfStats = StatType.Volume;
    expResult = 80;
    result = instance.getPurge(typeOfStats);
    assertEquals(expResult, result);
  }

  /**
   * Test of isValidConfigFile method, of class StatisticsConfig.
   */
  @Test
  public void testIsValidConfigFile() {
    assertTrue(instance.isValidConfigFile());
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
    assertFalse("Should not have only 3 data elements",
        instance.isGoodDatas(typeOfStats, dataArray));
    typeOfStats = StatType.Connexion;
    assertTrue(instance.isGoodDatas(typeOfStats, dataArray));
  }
}
