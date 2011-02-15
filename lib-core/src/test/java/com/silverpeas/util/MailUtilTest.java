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