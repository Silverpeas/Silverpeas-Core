package com.silverpeas.util;

import junit.framework.TestCase;

public class TestStringUtil extends TestCase {

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
  
  public void testIsInteger() {
    assertTrue(StringUtil.isInteger("1"));
    assertTrue(StringUtil.isInteger("00100"));
    assertFalse(StringUtil.isInteger("1.1"));
    assertFalse(StringUtil.isInteger("a"));
    assertTrue(StringUtil.isInteger("0"));
    assertTrue(StringUtil.isInteger("-1"));
  }
  
  public void testIsDefined() {
    assertTrue(StringUtil.isDefined("1"));
    assertTrue(StringUtil.isDefined("   "));
    assertFalse(StringUtil.isDefined(""));
    assertFalse(StringUtil.isDefined("null"));
    assertFalse(StringUtil.isDefined("NuLl"));
    assertFalse(StringUtil.isDefined(null));
  }
}
