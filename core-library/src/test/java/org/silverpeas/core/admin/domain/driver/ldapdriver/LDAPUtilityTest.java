/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author ehugonnet
 */
@UnitTest
class LDAPUtilityTest {

  /**
   * Test of isAGuid method, of class LDAPUtility.
   */
  @Test
  void testIsAGuid() {
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
  void testEscapeDN() {
    //escapeDN
    assertEquals("Helloé", LDAPUtility.escapeDN("Helloé"), "No special characters to escape");
    assertEquals("\\# Helloé", LDAPUtility.escapeDN("# Helloé"), "leading #");
    assertEquals("\\ Helloé", LDAPUtility.escapeDN(" Helloé"), "leading space");
    assertEquals("Helloé\\ ", LDAPUtility.escapeDN("Helloé "), "trailing space");
    assertEquals("\\  \\ ", LDAPUtility.escapeDN("   "), "only 3 spaces");
    assertEquals("\\ Hello\\\\ \\+ \\, \\\"World\\\" \\;\\ ",
        LDAPUtility.escapeDN(" Hello\\ + , \"World\" ; "), "Christmas Tree DN");
  }

  /**
   * Test of escapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  void testEscapeLDAPSearchFilter() {
    final String expectedEscapedValue = "Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";

    assertEquals("Hi This is a test #çà",
        LDAPUtility.escapeLDAPSearchFilter("Hi This is a test #çà"),
        "No special characters to escape");
    assertEquals(expectedEscapedValue,
        LDAPUtility.escapeLDAPSearchFilter("Hi \u0000 (This) = is * a \\ test # ç à ô"),
        "LDAP Christams Tree");
    assertEquals(
        "Hi \\5c00 \\5c28This\\5c29 = is \\5c2a a \\5c5c test # ç à ô",
        LDAPUtility.escapeLDAPSearchFilter(expectedEscapedValue), "LDAP Christams Tree");
  }

  /**
   * Test of escapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  void testEscapeLDAPSearchFilterWithSpecialCharacterClause() {
    assertEquals("Hi This * is a test * #çà",
        LDAPUtility.escapeLDAPSearchFilter("Hi This % is a test % #çà"),
        "% character to escape");
    assertEquals("Hi This % is a test % #çà",
        LDAPUtility.escapeLDAPSearchFilter("Hi This \\% is a test \\% #çà"),
        "No special characters to escape");
  }

  /**
   * Test of unescapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  void testUnescapeLDAPSearchFilter() {
    final String expectedUnescapedValue = "Hi \u0000 (This) = is * a \\ test # ç à ô";

    assertEquals("Hi This is a test #çà",
        LDAPUtility.unescapeLDAPSearchFilter("Hi This is a test #çà"),
        "No special characters to escape");
    assertEquals(expectedUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter("Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô"),
        "LDAP Christams Tree");
    assertEquals(expectedUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter(expectedUnescapedValue), "LDAP Christams Tree");
  }

  /**
   * Test of unescapeLDAPSearchFilter method, of class LDAPUtility.
   */
  @Test
  void testChainedUnescapeAndEscapeLDAPSearchFilter() {
    final String escapedValue = "Hi \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";
    final String unescapedValue = "Hi \u0000 (This) = is * a \\ test # ç à ô";

    assertEquals(unescapedValue, LDAPUtility.unescapeLDAPSearchFilter(escapedValue),
        "LDAP Christams Tree");
    assertEquals(escapedValue,
        LDAPUtility.escapeLDAPSearchFilter(LDAPUtility.unescapeLDAPSearchFilter(escapedValue)),
        "LDAP Christams Tree");

    final String partiallyEscapedValue =
        "Hi ( ) \\ * \u0000 \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";
    final String partiallyUnescapedValue =
        "Hi ( ) \\ * \u0000 \u0000 (This) = is * a \\ test # ç à ô";
    final String expectedPartiallyEscapedValue =
        "Hi \\28 \\29 \\5c \\2a \\00 \\00 \\28This\\29 = is \\2a a \\5c test # ç à ô";

    assertEquals(partiallyUnescapedValue,
        LDAPUtility.unescapeLDAPSearchFilter(partiallyEscapedValue), "LDAP Christams Tree");
    assertEquals(expectedPartiallyEscapedValue, LDAPUtility.escapeLDAPSearchFilter(
        LDAPUtility.unescapeLDAPSearchFilter(partiallyEscapedValue)), "LDAP Christams Tree");
  }
}
