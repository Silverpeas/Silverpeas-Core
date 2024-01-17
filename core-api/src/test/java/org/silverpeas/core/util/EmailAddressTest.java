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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailAddressTest {

  /**
   * Test of isValid method, of class EmailAddress.
   */
  @Test
  void testIsValidEmailAddress() {
    // Test variations in the email name
    assertTrue(EmailAddress.isValid("steve@javasrc.com"));
    assertTrue(EmailAddress.isValid("steven.haines@javasrc.com"));
    assertTrue(EmailAddress.isValid("steven-haines@javasrc.com"));
    assertTrue(EmailAddress.isValid("steven+haines@javasrc.com"));
    assertTrue(EmailAddress.isValid("steven_haines@javasrc.com"));
    assertFalse(EmailAddress.isValid("steven#haines@javasrc.com"));

    // Test variations in the domain name
    assertTrue(EmailAddress.isValid("steve@java-src.com"));
    assertTrue(EmailAddress.isValid("steve@java.src.com"));
    assertFalse(EmailAddress.isValid("steve@java\\src.com"));

    // Test variations in the domain name
    assertFalse(EmailAddress.isValid("steve@javasrc.a"));
    assertTrue(EmailAddress.isValid("steve@javasrc.aa"));
    assertTrue(EmailAddress.isValid("steve@javasrc.aaa"));
    assertTrue(EmailAddress.isValid("steve@javasrc.aaaa"));
    assertFalse(EmailAddress.isValid("steve@javasrc.aaaaa"));

    // Test that the email address marks the beginning of the string
    assertFalse(EmailAddress.isValid("aaa steve@javasrc.com"));

    // Test that the email address marks the end of the string
    assertFalse(EmailAddress.isValid("steve@javasrc.com aaa"));
  }
}
