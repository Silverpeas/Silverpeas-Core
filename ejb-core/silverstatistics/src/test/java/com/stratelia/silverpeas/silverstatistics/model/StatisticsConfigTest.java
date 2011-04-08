/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.silverstatistics.model;

import java.util.List;
import com.silverpeas.jcrutil.RandomGenerator;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class StatisticsConfigTest {

  private StatisticsConfig instance;

  public StatisticsConfigTest() {
  }

  @Before
  public void initialiseConfig() throws Exception {
    instance = new StatisticsConfig();
    instance.init();
  }

  /**
   * Test of getAllKeys method, of class StatisticsConfig.
   */
  @Test
  public void testGetAllKeys() throws Exception {
    String typeOfStats = "Connexion";
    Collection<String> keys = instance.getAllKeys(typeOfStats);
    assertNotNull(keys);
    assertThat(keys, hasSize(4));
    assertThat(keys, hasItems("dateStat", "userId", "countConnection", "duration"));

    typeOfStats = "toto";
    keys = instance.getAllKeys(typeOfStats);
    assertNotNull(keys);
    assertThat(keys, hasSize(0));
  }

  /**
   * Test of getKeyType method, of class StatisticsConfig.
   */
  @Test
  public void testGetKeyType() throws Exception {
    String result = instance.getKeyType("Connexion", "userId");
    assertEquals(StatDataType.INTEGER.name(), result);
    result = instance.getKeyType("Connexion", "dateStat");
    assertEquals(StatDataType.VARCHAR.name(), result);
  }

  /**
   * Test of getTableName method, of class StatisticsConfig.
   */
  @Test
  public void testGetTableName() throws Exception {
    String result = instance.getTableName("Connexion");
    assertEquals("SB_Stat_Connection", result);
    result = instance.getTableName("Volume");
    assertEquals("SB_Stat_Volume", result);
  }

  /**
   * Test of getModeCumul method, of class StatisticsConfig.
   */
  @Test
  public void testGetModeCumul() throws Exception {
    String typeOfStats = "Connexion";
    StatisticMode result = instance.getModeCumul(typeOfStats);
    assertEquals(StatisticMode.Add, result);
    typeOfStats = "Size";
    result = instance.getModeCumul(typeOfStats);
    assertEquals(StatisticMode.Replace, result);
  }

  /**
   * Test of isRun method, of class StatisticsConfig.
   */
  @Test
  public void testIsRun() throws Exception {
    boolean result = instance.isRun("Connexion");
    assertTrue(result);
    result = instance.isRun("Access");
    assertFalse(result);
    result = instance.isRun("toto");
    assertFalse(result);
  }

  /**
   * Test of isAsynchron method, of class StatisticsConfig.
   */
  @Test
  public void testIsAsynchron() throws Exception {
    boolean result = instance.isAsynchron("Connexion");
    assertTrue(result);
    result = instance.isAsynchron("Volume");
    assertFalse(result);
    result = instance.isAsynchron("toto");
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
    String idFamilyStats = "Connexion";
    boolean result = instance.isExist(idFamilyStats);
    assertTrue(result);
    idFamilyStats = "Volume";
    result = instance.isExist(idFamilyStats);
    assertTrue(result);
    idFamilyStats = RandomGenerator.getRandomString();
    result = instance.isExist(idFamilyStats);
    assertFalse(result);
  }

  /**
   * Test of getNumberOfKeys method, of class StatisticsConfig.
   */
  @Test
  public void testGetNumberOfKeys() {
    String idFamilyStats = "Connexion";
    int expResult = 4;
    int result = instance.getNumberOfKeys(idFamilyStats);
    assertEquals(expResult, result);
    idFamilyStats = "Access";
    expResult = 6;
    result = instance.getNumberOfKeys(idFamilyStats);
    assertEquals(expResult, result);
    idFamilyStats = RandomGenerator.getRandomString();
    expResult = 0;
    result = instance.getNumberOfKeys(idFamilyStats);
    assertEquals(expResult, result);
  }

  /**
   * Test of isCumulKey method, of class StatisticsConfig.
   */
  @Test
  public void testIsCumulKey() {
    String typeOfStats = "Connexion";
    String cumulKeyName = "countConnection";
    boolean result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertTrue(result);
    cumulKeyName = "duration";
    result = instance.isCumulKey(typeOfStats, cumulKeyName);
    assertTrue(result);
    String notCumulKeyName = "userId";
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertFalse(result);
    notCumulKeyName = RandomGenerator.getRandomString();
    result = instance.isCumulKey(typeOfStats, notCumulKeyName);
    assertFalse(result);
  }

  /**
   * Test of indexOfKey method, of class StatisticsConfig.
   */
  @Test
  public void testIndexOfKey() {
    String typeOfStats = "Connexion";
    String existingKeyName = "countConnection";
    int result = instance.indexOfKey(typeOfStats, existingKeyName);
    assertEquals(2, result);
    String notExistingKeyName = RandomGenerator.getRandomString();
    result = instance.indexOfKey(typeOfStats, notExistingKeyName);
    assertEquals(-1, result);
    typeOfStats = RandomGenerator.getRandomString();
    result = instance.indexOfKey(typeOfStats, existingKeyName);
    assertEquals(-1, result);
  }

  /**
   * Test of getAllTypes method, of class StatisticsConfig.
   */
  @Test
  public void testGetAllTypes() throws Exception {
    assertNotNull(instance);
    Collection<String> types = instance.getAllTypes();
    assertNotNull(types);
    assertThat(types, hasSize(4));
    assertThat(types, hasItems("Connexion", "Size", "Access", "Volume"));
  }

  /**
   * Test of getPurge method, of class StatisticsConfig.
   */
  @Test
  public void testGetPurge() {
    String typeOfStats = "Connexion";
    int expResult = 120;
    int result = instance.getPurge(typeOfStats);
    assertEquals(expResult, result);
    typeOfStats = "Volume";
    expResult = 80;
    result = instance.getPurge(typeOfStats);
    assertEquals(expResult, result);
    typeOfStats = "None";
    expResult = 0;
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
    String typeOfStats = "Size";
    List dataArray = new ArrayList(4);
    dataArray.add(RandomGenerator.getRandomString());
    dataArray.add(RandomGenerator.getRandomString());
    dataArray.add(RandomGenerator.getRandomString());
    dataArray.add(RandomGenerator.getRandomString());
    assertFalse("Should not have only 3 data elements", instance.isGoodDatas(typeOfStats, dataArray));
    typeOfStats = RandomGenerator.getRandomString();
    assertFalse("Type doesn't exist", instance.isGoodDatas(typeOfStats, dataArray));
    typeOfStats = "Connexion";
    assertTrue(instance.isGoodDatas(typeOfStats, dataArray));
  }
}
