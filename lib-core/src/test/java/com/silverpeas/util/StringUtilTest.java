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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.util;

import java.io.UnsupportedEncodingException;
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
public class StringUtilTest {

  public StringUtilTest() {
  }

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
   * Test of format method, of class StringUtil.
   */
  /*@Test
  public void testFormat() {
  System.out.println("format");
  String label = "";
  Map<String, ?> values = null;
  String expResult = "";
  String result = StringUtil.format(label, values);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of truncate method, of class StringUtil.
   */
  /* @Test
  public void testTruncate() {
  System.out.println("truncate");
  String text = "";
  int maxLength = 0;
  String expResult = "";
  String result = StringUtil.truncate(text, maxLength);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
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

  /**
   * Test of detectEncoding method, of class StringUtil.
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testDetectEncoding() throws UnsupportedEncodingException {
    String testString = "voici une chaîne créée exprès";

    String result = StringUtil.detectEncoding(testString.getBytes("ISO-8859-1"), null);
    assertThat(result, is("ISO-8859-1"));
    result = StringUtil.detectEncoding(testString.getBytes("ISO-8859-1"), "UTF-8");
    assertThat(result, is("ISO-8859-1"));
    result = StringUtil.detectEncoding(testString.getBytes("UTF-8"), null);
    assertThat(result, is("UTF-8"));
    result = StringUtil.detectEncoding(testString.getBytes("UTF-8"), "UTF-8");
    assertThat(result, is("UTF-8"));
    
    /*String copyright = "Département de la Drôme";
    result = StringUtil.detectEncoding(copyright.getBytes("UTF-8"), "UTF-8");
    assertThat(result, is("UTF-8"));
    copyright = "Département de la Drôme";
    result = StringUtil.detectEncoding(copyright.getBytes("ISO-8859-1"), "UTF-8");
    assertThat(result, is("ISO-8859-1"));*/
  }
}
