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

package com.stratelia.silverpeas.silverstatistics.model;

import java.util.Arrays;
import java.util.Collection;

import com.silverpeas.jcrutil.RandomGenerator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class TypeStatisticsTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
    assertThat(result, is(expResult));
  }

  /**
   * Test of setName method, of class TypeStatistics.
   */
  @Test
  public void testSetNullName() throws SilverStatisticsTypeStatisticsException {
    expectedException.expect(SilverStatisticsTypeStatisticsException.class);
    expectedException.expectMessage("silverstatistics.MSG_NAME_STATS_EMPTY");
    TypeStatistics instance = new TypeStatistics();
    instance.setName(null);
  }

  /**
   * Test of setName method, of class TypeStatistics.
   */
  @Test
  public void testSetName() throws SilverStatisticsTypeStatisticsException {
    String name = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.setName(name);
    assertThat(instance.getName(), is(name));
  }

  /**
   * Test of setName method, of class TypeStatistics.
   */
  @Test
  public void testSetEmptyName() throws SilverStatisticsTypeStatisticsException {
    expectedException.expect(SilverStatisticsTypeStatisticsException.class);
    expectedException.expectMessage("silverstatistics.MSG_NAME_STATS_EMPTY");
    String name = RandomGenerator.getRandomString();
    TypeStatistics instance = new TypeStatistics();
    instance.setName("");
  }

  /**
   * Test of getModeCumul method, of class TypeStatistics.
   */
  @Test
  public void testGetModeCumul() {
    TypeStatistics instance = new TypeStatistics();
    StatisticMode expResult = StatisticMode.Add;
    StatisticMode result = instance.getModeCumul();
    assertThat(result, is(expResult));
  }

  /**
   * Test of setModeCumul method, of class TypeStatistics.
   */
  @Test
  public void testSetModeCumul() throws Exception {
    StatisticMode mode = StatisticMode.Replace;
    TypeStatistics instance = new TypeStatistics();
    instance.setModeCumul(mode);
    assertThat(instance.getModeCumul(), is(StatisticMode.Replace));
  }

  /**
   * Test of setIsRun method, of class TypeStatistics.
   */
  @Test
  public void testSetIsRun() {
    boolean value = false;
    TypeStatistics instance = new TypeStatistics();
    instance.setIsRun(value);
    assertThat(instance.isRun(), is(value));
  }

  /**
   * Test of isRun method, of class TypeStatistics.
   */
  @Test
  public void testIsRun() {
    TypeStatistics instance = new TypeStatistics();
    boolean expResult = true;
    boolean result = instance.isRun();
    assertThat(result, is(expResult));
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
    assertThat(result, is(value));
  }

  /**
   * Test of isAsynchron method, of class TypeStatistics.
   */
  @Test
  public void testIsAsynchron() {
    TypeStatistics instance = new TypeStatistics();
    boolean expResult = true;
    boolean result = instance.isAsynchron();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getPurge method, of class TypeStatistics.
   */
  @Test
  public void testGetPurge() {
    TypeStatistics instance = new TypeStatistics();
    int expResult = 3;
    int result = instance.getPurge();
    assertThat(result, is(expResult));
  }

  /**
   * Test of setPurge method, of class TypeStatistics.
   */
  @Test
  public void testSetPurge() throws Exception {
    int purgeInMonth = 5;
    TypeStatistics instance = new TypeStatistics();
    instance.setPurge(purgeInMonth);
    assertThat(instance.getPurge(), is(purgeInMonth));
  }

  /**
   * Test of getTableName method, of class TypeStatistics.
   */
  @Test
  public void testGetTableName() {
    TypeStatistics instance = new TypeStatistics();
    String expResult = "";
    String result = instance.getTableName();
    assertThat(result, is(expResult));
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
    assertThat(result, is(name));
  }

  /**
   * Test of getAllKeys method, of class TypeStatistics.
   */
  @Test
  public void testGetAllKeys() {
    TypeStatistics instance = new TypeStatistics();
    Collection<String> result = instance.getAllKeys();
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
  }

  /**
   * Test of setAllKeys method, of class TypeStatistics.
   */
  @Test
  public void testSetAllKeys() {
    String value1 = RandomGenerator.getRandomString();
    String value2 = RandomGenerator.getRandomString();
    Collection<String> allTags = Arrays.asList(value1, value2);
    TypeStatistics instance = new TypeStatistics();
    instance.setAllKeys(allTags);
    Collection<String> allKeys = instance.getAllKeys();
    assertThat(allKeys, is(notNullValue()));
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
    Collection<String> allTags = Arrays.asList(value1, value2);
    TypeStatistics instance = new TypeStatistics();
    instance.setAllKeys(allTags);
    Collection<String> allKeys = instance.getAllKeys();
    assertThat(allKeys, is(notNullValue()));
    assertThat(allKeys, hasSize(2));
    assertThat(allKeys, contains(value1, value2));
    String value3 = RandomGenerator.getRandomString();
    instance.addKey(value3, StatDataType.VARCHAR);
    allKeys = instance.getAllKeys();
    assertThat(allKeys, is(notNullValue()));
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
    assertThat(allKeys, is(notNullValue()));
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
    Collection<String> allKeys = Arrays.asList(value1, value2, value3);
    instance.setAllKeys(allKeys);
    int result = instance.indexOfKey(value2);
    assertThat(result, is(1));
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
    assertThat(allKeys, is(notNullValue()));
    assertThat(allKeys, hasSize(2));
    assertThat(instance.isCumulKey(notCumulKeyName), is(false));
    assertThat(instance.isCumulKey(cumulKeyName), is(true));
  }

  /**
   * Test of isDateStatKeyExist method, of class TypeStatistics.
   */
  @Test
  public void testIsDateStatKeyExist() throws Exception {
    TypeStatistics instance = new TypeStatistics();
    boolean result = instance.isDateStatKeyExist();
    assertThat(result, is(false));
    instance.addKey(TypeStatistics.STATISTIC_DATE_KEY, StatDataType.VARCHAR);
    result = instance.isDateStatKeyExist();
    assertThat(result, is(true));
  }

  /**
   * Test of isAGoodType method, of class TypeStatistics.
   */
  @Test
  public void testIsAGoodType() {
    String typeName = "";
    TypeStatistics instance = new TypeStatistics();
    boolean result = instance.isAGoodType(typeName);
    assertThat(result, is(false));
    typeName = StatDataType.INTEGER.name();
    result = instance.isAGoodType(typeName);
    assertThat(result, is(true));
    typeName = StatDataType.VARCHAR.name();
    result = instance.isAGoodType(typeName);
    assertThat(result, is(true));
    typeName = StatDataType.DECIMAL.name();
    result = instance.isAGoodType(typeName);
    assertThat(result, is(true));
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
    assertThat(instance.hasACumulType(notCumulKeyName), is(false));
    assertThat(instance.hasACumulType(cumulKeyName), is(true));
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
    assertThat(instance.hasGoodCumulKey(), is(false));
    instance.addCumulKey(cumulKeyName);
    assertThat(instance.hasGoodCumulKey(), is(true));
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
    assertThat(instance.getKeyType(cumulKeyName), is(StatDataType.INTEGER));
    assertThat(instance.getKeyType(notCumulKeyName), is(StatDataType.VARCHAR));
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
    Collection<String> allKeys = Arrays.asList(value1, value2, value3);
    instance.setAllKeys(allKeys);
    int result = instance.numberOfKeys();
    assertThat(result, is(allKeys.size()));
  }
}
