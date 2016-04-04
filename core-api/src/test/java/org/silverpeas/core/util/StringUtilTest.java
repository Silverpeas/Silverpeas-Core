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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.util;

import org.junit.Test;
import org.silverpeas.core.util.StringUtil;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
/**
 *
 * @author ehugonnet
 */
public class StringUtilTest {

  /**
   * Test of isDefined method, of class StringUtil.
   */
  @Test
  public void testIsDefined() {
    assertTrue(StringUtil.isDefined("1"));
    assertFalse(StringUtil.isDefined("   "));
    assertFalse(StringUtil.isDefined(""));
    assertFalse(StringUtil.isDefined("null"));
    assertFalse(StringUtil.isDefined("NuLl"));
    assertFalse(StringUtil.isDefined(null));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  public void testIsInteger() {
    assertTrue(StringUtil.isInteger("1"));
    assertTrue(StringUtil.isInteger("00100"));
    assertFalse(StringUtil.isInteger("1.1"));
    assertFalse(StringUtil.isInteger("a"));
    assertTrue(StringUtil.isInteger("0"));
    assertTrue(StringUtil.isInteger("-1"));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  public void testIsFloat() {
    assertTrue(StringUtil.isFloat("1"));
    assertTrue(StringUtil.isFloat("00100"));
    assertTrue(StringUtil.isFloat("1.1"));
    assertFalse(StringUtil.isFloat("a"));
    assertTrue(StringUtil.isFloat("0"));
    assertTrue(StringUtil.isFloat("-1"));
    assertFalse(StringUtil.isFloat("1,1"));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  public void testConvertFloat() {
    assertEquals(1.0f, StringUtil.convertFloat("1"), 0.001f);
    assertEquals(1.1f, StringUtil.convertFloat("1.1"), 0.001f);
    assertEquals(0f, StringUtil.convertFloat("a"), 0.001f);
    assertEquals(-1.0f, StringUtil.convertFloat("-1"), 0.001f);
    assertEquals(1.1f, StringUtil.convertFloat("1,1"), 0.001f);
  }

  /**
   * Test of escapeQuote method, of class StringUtil.
   */
  @Test
  public void testEscapeQuote() {
    String text = "'hello'";
    String expResult = " hello ";
    String result = StringUtil.escapeQuote(text);
    assertEquals(expResult, result);
  }

  /**
   * Test of isValidEmailAddress method, of class StringUtil.
   */
  @Test
  public void testIsValidEmailAddress() {
    // Test variations in the email name
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven.haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven-haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven+haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven_haines@javasrc.com"));
    assertFalse(StringUtil.isValidEmailAddress("steven#haines@javasrc.com"));

    // Test variations in the domain name
    assertTrue(StringUtil.isValidEmailAddress("steve@java-src.com"));
    assertTrue(StringUtil.isValidEmailAddress("steve@java.src.com"));
    assertFalse(StringUtil.isValidEmailAddress("steve@java\\src.com"));

    // Test variations in the domain name
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.a"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aa"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aaa"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aaaa"));
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.aaaaa"));

    // Test that the email address marks the beginning of the string
    assertFalse(StringUtil.isValidEmailAddress("aaa steve@javasrc.com"));

    // Test that the email address marks the end of the string
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.com aaa"));
  }

  /**
   * Test of convertToEncoding method, of class StringUtil.
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testConvertToEncoding() throws UnsupportedEncodingException {
    assertEquals("élève", StringUtil.convertToEncoding(new String("élève".getBytes("UTF-8")),
        "UTF-8"));
    assertNotSame("élève", StringUtil.convertToEncoding(new String("élève".getBytes("ISO-8859-1")),
        "UTF-8"));
  }

  /**
   * Test of getBooleanValue method, of class StringUtil.
   */
  @Test
  public void testGetBooleanValue() {
    assertTrue(StringUtil.getBooleanValue("1"));
    assertTrue(StringUtil.getBooleanValue("yes"));
    assertTrue(StringUtil.getBooleanValue("YeS"));
    assertTrue(StringUtil.getBooleanValue("trUe"));
    assertTrue(StringUtil.getBooleanValue("oUi"));
    assertTrue(StringUtil.getBooleanValue("Y"));
    assertFalse(StringUtil.getBooleanValue(""));
    assertFalse(StringUtil.getBooleanValue("no"));
    assertFalse(StringUtil.getBooleanValue("0"));
    assertFalse(StringUtil.getBooleanValue(null));
  }

  @Test
  public void testDefaultStringIfNotDefinedReturningEmptyStringAsDefault() {
    assertThat(StringUtil.defaultStringIfNotDefined(null), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined(""), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined("   "), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined(" A "), is(" A "));
  }

  @Test
  public void testDefaultStringIfNotDefinedReturningGivenStringAsDefault() {
    String defaultString = "givenString";
    assertThat(StringUtil.defaultStringIfNotDefined(null, defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined("", defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined("   ", defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined(" A ", defaultString), is(" A "));
  }

  @Test
  public void testIsValidHour() {
    assertTrue(StringUtil.isValidHour("10:30"));
    assertTrue(StringUtil.isValidHour("24:59"));
    assertTrue(StringUtil.isValidHour("00:00"));
    assertTrue(StringUtil.isValidHour("8:01"));
    assertTrue(StringUtil.isValidHour("0:01"));
    // Check limit hour
    assertFalse(StringUtil.isValidHour("25:00"));
    assertFalse(StringUtil.isValidHour("24:60"));
    assertFalse(StringUtil.isValidHour("10-30"));
    assertFalse(StringUtil.isValidHour(null));
    assertFalse(StringUtil.isValidHour(""));
  }
}
