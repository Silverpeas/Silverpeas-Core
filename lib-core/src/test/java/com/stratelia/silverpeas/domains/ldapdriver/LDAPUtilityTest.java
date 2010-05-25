/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
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
    assertEquals("No special characters to escape", "Hi This is a test #çà", 
        LDAPUtility.escapeLDAPSearchFilter("Hi This is a test #çà"));
       assertEquals("LDAP Christams Tree", "Hi \\28This\\29 = is \\2a a \\5c test # ç à ô",
           LDAPUtility.escapeLDAPSearchFilter("Hi (This) = is * a \\ test # ç à ô"));
  }
}
