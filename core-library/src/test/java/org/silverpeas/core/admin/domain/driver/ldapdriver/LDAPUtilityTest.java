/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class LDAPUtilityTest {

  public LDAPUtilityTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of isAGuid method, of class LDAPUtility.
   */
  @Test
  public void testIsAGuid() {
   assertTrue(LDAPUtility.isAGuid("objectGUID"));
   assertTrue(LDAPUtility.isAGuid("OBJECTguid"));
   assertTrue(LDAPUtility.isAGuid("GUID"));
   assertTrue(LDAPUtility.isAGuid("guid"));
   assertFalse(LDAPUtility.isAGuid("uid"));
  }


  /**
   * Test of escapeDN method, of class LDAPUtility.
   */
  @Test
  public void testEscapeDN() {
    //escapeDN
    assertEquals("No special characters to escape", "Helloé", LDAPUtility.escapeDN("Helloé"));
    assertEquals("leading #", "\\# Helloé", LDAPUtility.escapeDN("# Helloé"));
    assertEquals("leading space", "\\ Helloé", LDAPUtility.escapeDN(" Helloé"));
    assertEquals("trailing space", "Helloé\\ ", LDAPUtility.escapeDN("Helloé "));
    assertEquals("only 3 spaces", "\\  \\ ", LDAPUtility.escapeDN("   "));
    assertEquals("Christmas Tree DN", "\\ Hello\\\\ \\+ \\, \\\"World\\\" \\;\\ ",
        LDAPUtility.escapeDN(" Hello\\ + , \"World\" ; "));
  }

  /**
   * Test of escapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  public void testEscapeLDAPSearchFilter() {
    final String expectedEscapedValue = "Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";

    assertEquals("No special characters to escape", "Hi This is a test #çà",
        LDAPUtility.escapeLDAPSearchFilter("Hi This is a test #çà"));
    assertEquals("LDAP Christams Tree", expectedEscapedValue,
        LDAPUtility.escapeLDAPSearchFilter("Hi \u0000 (This) = is * a \\ test # ç à ô"));
    assertEquals("LDAP Christams Tree",
        "Hi \\5c00 \\5c28This\\5c29 = is \\5c2a a \\5c5c test # ç à ô",
        LDAPUtility.escapeLDAPSearchFilter(expectedEscapedValue));
  }

  /**
   * Test of unescapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  public void testUnescapeLDAPSearchFilter() {
    final String expectedUnescapedValue = "Hi \u0000 (This) = is * a \\ test # ç à ô";

    assertEquals("No special characters to escape", "Hi This is a test #çà",
        LDAPUtility.unescapeLDAPSearchFilter("Hi This is a test #çà"));
    assertEquals("LDAP Christams Tree", expectedUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter("Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô"));
    assertEquals("LDAP Christams Tree", expectedUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter(expectedUnescapedValue));
  }

  /**
   * Test of unescapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  public void testChainedUnescapeAndEscapeLDAPSearchFilter() {
    final String escapedValue = "Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";
    final String unescapedValue = "Hi \u0000 (This) = is * a \\ test # ç à ô";

    assertEquals("LDAP Christams Tree", unescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter(escapedValue));
    assertEquals("LDAP Christams Tree", escapedValue,
        LDAPUtility.escapeLDAPSearchFilter(LDAPUtility.unescapeLDAPSearchFilter(escapedValue)));

    final String partiallyEscapedValue =
        "Hi ( ) \\ * \u0000 \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";
    final String partiallyUnescapedValue =
        "Hi ( ) \\ * \u0000 \u0000 (This) = is * a \\ test # ç à ô";
    final String expectedPartiallyEscapedValue =
        "Hi \\28 \\29 \\5c \\2a \\00 \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";

    assertEquals("LDAP Christams Tree", partiallyUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter(partiallyEscapedValue));
    assertEquals("LDAP Christams Tree", expectedPartiallyEscapedValue, LDAPUtility
        .escapeLDAPSearchFilter(LDAPUtility.unescapeLDAPSearchFilter(partiallyEscapedValue)));
  }
}
