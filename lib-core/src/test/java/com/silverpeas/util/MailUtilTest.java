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

import javax.mail.internet.InternetAddress;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class MailUtilTest {

  public MailUtilTest() {
  }

  /**
   * Test of isDomainAuthorized method, of class MailUtil.
   */
  @Test
  public void testIsDomainAuthorized() {
    String email = "toto@silverpeas.com";
    boolean result = MailUtil.isDomainAuthorized(email);
    assertTrue(result);
    MailUtil.reloadConfiguration("silverpeas.org");
    result = MailUtil.isDomainAuthorized(email);
    assertFalse(result);
    MailUtil.reloadConfiguration( "Silverpeas.COM,silverpeas.org");
    result = MailUtil.isDomainAuthorized(email);
    assertTrue(result);
  }

  /**
   * Test of getAuthorizedEmail method, of class MailUtil.
   */
  @Test
  public void testGetAuthorizedEmail() {
    MailUtil.reloadConfiguration( "Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertEquals(authorizedEmail, result);
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmail(unauthorizedEmail);
    assertEquals("silverpeas@silverpeas.com", result);
  }
  
  /**
   * Test of getAuthorizedEmail method, of class MailUtil.
   */
  @Test
  public void testGetAuthorizedEmailWithNoDomain() {
    MailUtil.reloadConfiguration(null);
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertEquals(authorizedEmail, result);
    MailUtil.reloadConfiguration("");
    authorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertEquals("toto@slashdot.com", result);
  }
  
  
  @Test
  public void testGetAuthorizedEmailAddress() throws Exception {
    MailUtil.reloadConfiguration( "Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    InternetAddress result = MailUtil.getAuthorizedEmailAddress(authorizedEmail); 
    assertEquals(new InternetAddress("\"toto\"<toto@silverpeas.com>"), result);
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmailAddress(unauthorizedEmail);
    assertEquals(new InternetAddress("\"toto\"<silverpeas@silverpeas.com>"), result);
  }
}