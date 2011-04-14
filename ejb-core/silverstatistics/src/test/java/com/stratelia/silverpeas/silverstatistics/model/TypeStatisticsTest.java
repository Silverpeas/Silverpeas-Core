/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.silverstatistics.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.silverpeas.jcrutil.RandomGenerator;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class TypeStatisticsTest {

  public TypeStatisticsTest() {
  }

  /**
   * Test of getName method, of class TypeStatistics.
   */
  @Test
  public void testGetName() {
    TypeStatistics instance = new TypeStatistics();
    String expResult = "";
    String result = instance.getName();
    assertEquals(expResult, result);
  }

  /**
   * Test of setName method, of class TypeStatistics.
   */
  @Test
  public void testSetName() throws Exception {
    String name = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.setName(name);
    assertEquals(name, instance.getName());

    instance = new TypeStatistics();
    try {
      instance.setName(null);
      fail();
    } catch (SilverStatisticsTypeStatisticsException e) {
      assertEquals("silverstatistics.MSG_NAME_STATS_EMPTY", e.getMessage());
    }


    instance = new TypeStatistics();
    try {
      instance.setName("");
      fail();
    } catch (SilverStatisticsTypeStatisticsException e) {
      assertEquals("silverstatistics.MSG_NAME_STATS_EMPTY", e.getMessage());
    }
  }

  /**
   * Test of getModeCumul method, of class TypeStatistics.
   */
  @Test
  public void testGetModeCumul() {
    TypeStatistics instance = new TypeStatistics();
    StatisticMode expResult = StatisticMode.Add;
    StatisticMode result = instance.getModeCumul();
    assertEquals(expResult, result);
  }

  /**
   * Test of setModeCumul method, of class TypeStatistics.
   */
  @Test
  public void testSetModeCumul() throws Exception {
    StatisticMode mode = StatisticMode.Replace;
    TypeStatistics instance = new TypeStatistics();
    instance.setModeCumul(mode);
    assertEquals(StatisticMode.Replace, instance.getModeCumul());
  }

  /**
   * Test of setIsRun method, of class TypeStatistics.
   */
  @Test
  public void testSetIsRun() {
    boolean value = false;
    TypeStatistics instance = new TypeStatistics();
    instance.setIsRun(value);
    assertEquals(value, instance.isRun());
  }

  /**
   * Test of isRun method, of class TypeStatistics.
   */
  @Test
  public void testIsRun() {
    TypeStatistics instance = new TypeStatistics();
    boolean expResult = true;
    boolean result = instance.isRun();
    assertEquals(expResult, result);
  }

  /**
   * Test of setIsAsynchron method, of class TypeStatistics.
   */
  @Test
  public void testSetIsAsynchron() {
    boolean value = false;
    TypeStatistics instance = new TypeStatistics();
    instance.setIsAsynchron(value);
    boolean result = instance.isAsynchron();
    assertEquals(value, result);
  }

  /**
   * Test of isAsynchron method, of class TypeStatistics.
   */
  @Test
  public void testIsAsynchron() {
    TypeStatistics instance = new TypeStatistics();
    boolean expResult = true;
    boolean result = instance.isAsynchron();
    assertEquals(expResult, result);
  }

  /**
   * Test of getPurge method, of class TypeStatistics.
   */
  @Test
  public void testGetPurge() {
    TypeStatistics instance = new TypeStatistics();
    int expResult = 3;
    int result = instance.getPurge();
    assertEquals(expResult, result);
  }

  /**
   * Test of setPurge method, of class TypeStatistics.
   */
  @Test
  public void testSetPurge() throws Exception {
    int purgeInMonth = 5;
    TypeStatistics instance = new TypeStatistics();
    instance.setPurge(purgeInMonth);
    assertEquals(purgeInMonth, instance.getPurge());
  }

  /**
   * Test of getTableName method, of class TypeStatistics.
   */
  @Test
  public void testGetTableName() {
    TypeStatistics instance = new TypeStatistics();
    String expResult = "";
    String result = instance.getTableName();
    assertEquals(expResult, result);
  }

  /**
   * Test of setTableName method, of class TypeStatistics.
   */
  @Test
  public void testSetTableName() throws Exception {
    String name = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.setTableName(name);
    String result = instance.getTableName();
    assertEquals(name, result);
  }

  /**
   * Test of getAllKeys method, of class TypeStatistics.
   */
  @Test
  public void testGetAllKeys() {
    TypeStatistics instance = new TypeStatistics();
    Collection<String> result = instance.getAllKeys();
    assertNotNull(result);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of setAllKeys method, of class TypeStatistics.
   */
  @Test
  public void testSetAllKeys() {
    String value1 = RandomGenerator.getRandomString();
    String value2 = RandomGenerator.getRandomString();
    Collection<String> allTags = Lists.newArrayList(value1, value2);
    TypeStatistics instance = new TypeStatistics();
    instance.setAllKeys(allTags);
    Collection<String> allKeys = instance.getAllKeys();
    assertNotNull(allKeys);
    assertThat(allKeys, hasSize(2));
    assertThat(allKeys, contains(value1, value2));
  }

  /**
   * Test of addKey method, of class TypeStatistics.
   */
  @Test
  public void testAddKey() throws Exception {
    String value1 = RandomGenerator.getRandomString();
    String value2 = RandomGenerator.getRandomString();
    Collection<String> allTags = Lists.newArrayList(value1, value2);
    TypeStatistics instance = new TypeStatistics();
    instance.setAllKeys(allTags);
    Collection<String> allKeys = instance.getAllKeys();
    assertNotNull(allKeys);
    assertThat(allKeys, hasSize(2));
    assertThat(allKeys, contains(value1, value2));
    String value3 = RandomGenerator.getRandomString();
    instance.addKey(value3, StatDataType.VARCHAR);
    allKeys = instance.getAllKeys();
    assertNotNull(allKeys);
    assertThat(allKeys, hasSize(3));
    assertThat(allKeys, contains(value1, value2, value3));
  }

  /**
   * Test of addCumulKey method, of class TypeStatistics.
   */
  @Test
  public void testAddCumulKey() throws Exception {
    String keyName = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.addKey(keyName, StatDataType.INTEGER);
    instance.addCumulKey(keyName);
    Collection<String> allKeys = instance.getAllKeys();
    assertNotNull(allKeys);
    assertThat(allKeys, hasSize(1));
    assertThat(allKeys, contains(keyName));
  }

  /**
   * Test of indexOfKey method, of class TypeStatistics.
   */
  @Test
  public void testIndexOfKey() {
    String value1 = RandomGenerator.getRandomString();
    String value2 = RandomGenerator.getRandomString();
    String value3 = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    Collection<String> allKeys = Lists.newArrayList(value1, value2, value3);
    instance.setAllKeys(allKeys);
    int result = instance.indexOfKey(value2);
    assertEquals(1, result);
  }

  /**
   * Test of isCumulKey method, of class TypeStatistics.
   */
  @Test
  public void testIsCumulKey() throws Exception {
    String notCumulKeyName = RandomGenerator.getRandomString();
    String cumulKeyName = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.addKey(cumulKeyName, StatDataType.INTEGER);
    instance.addKey(notCumulKeyName, StatDataType.VARCHAR);
    instance.addCumulKey(cumulKeyName);
    Collection<String> allKeys = instance.getAllKeys();
    assertNotNull(allKeys);
    assertThat(allKeys, hasSize(2));
    assertFalse(instance.isCumulKey(notCumulKeyName));
    assertTrue(instance.isCumulKey(cumulKeyName));
  }

  /**
   * Test of isDateStatKeyExist method, of class TypeStatistics.
   */
  @Test
  public void testIsDateStatKeyExist() throws Exception {
    TypeStatistics instance = new TypeStatistics();
    boolean result = instance.isDateStatKeyExist();
    assertFalse(result);
    instance.addKey(TypeStatistics.STATISTIC_DATE_KEY, StatDataType.VARCHAR);
    result = instance.isDateStatKeyExist();
    assertTrue(result);
  }

  /**
   * Test of isAGoodType method, of class TypeStatistics.
   */
  @Test
  public void testIsAGoodType() {
    String typeName = "";
    TypeStatistics instance = new TypeStatistics();
    boolean result = instance.isAGoodType(typeName);
    assertFalse(result);
    typeName = StatDataType.INTEGER.name();
    result = instance.isAGoodType(typeName);
    assertTrue(result);
    typeName = StatDataType.VARCHAR.name();
    result = instance.isAGoodType(typeName);
    assertTrue(result);
    typeName = StatDataType.DECIMAL.name();
    result = instance.isAGoodType(typeName);
    assertTrue(result);
  }

  /**
   * Test of hasACumulType method, of class TypeStatistics.
   */
  @Test
  public void testHasACumulType() throws Exception {
    String notCumulKeyName = RandomGenerator.getRandomString();
    String cumulKeyName = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.addKey(cumulKeyName, StatDataType.INTEGER);
    instance.addKey(notCumulKeyName, StatDataType.VARCHAR);
    instance.addCumulKey(cumulKeyName);
    assertFalse(instance.hasACumulType(notCumulKeyName));
    assertTrue(instance.hasACumulType(cumulKeyName));
  }

  /**
   * Test of hasGoodCumulKey method, of class TypeStatistics.
   */
  @Test
  public void testHasGoodCumulKey() throws Exception {
    String notCumulKeyName = RandomGenerator.getRandomString();
    String cumulKeyName = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.addKey(cumulKeyName, StatDataType.INTEGER);
    instance.addKey(notCumulKeyName, StatDataType.VARCHAR);
    assertFalse(instance.hasGoodCumulKey());
    instance.addCumulKey(cumulKeyName);
    assertTrue(instance.hasGoodCumulKey());
  }

  /**
   * Test of getKeyType method, of class TypeStatistics.
   */
  @Test
  public void testGetKeyType() throws Exception {
    String notCumulKeyName = RandomGenerator.getRandomString();
    String cumulKeyName = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.addKey(cumulKeyName, StatDataType.INTEGER);
    instance.addKey(notCumulKeyName, StatDataType.VARCHAR);
    instance.addCumulKey(cumulKeyName);
    assertEquals(StatDataType.INTEGER, instance.getKeyType(cumulKeyName));
    assertEquals(StatDataType.VARCHAR, instance.getKeyType(notCumulKeyName));
  }

  /**
   * Test of numberOfKeys method, of class TypeStatistics.
   */
  @Test
  public void testNumberOfKeys() {
    String value1 = RandomGenerator.getRandomString();
    String value2 = RandomGenerator.getRandomString();
    String value3 = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    Collection<String> allKeys = Lists.newArrayList(value1, value2, value3);
    instance.setAllKeys(allKeys);
    int result = instance.numberOfKeys();
    assertEquals(allKeys.size(), result);
  }
}
