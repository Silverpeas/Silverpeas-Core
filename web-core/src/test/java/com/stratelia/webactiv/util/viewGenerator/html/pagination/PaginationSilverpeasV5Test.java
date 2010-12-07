/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.util.viewGenerator.html.pagination;

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
public class PaginationSilverpeasV5Test {

  public PaginationSilverpeasV5Test() {
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
   * Test of init method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testInit() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(nbItems, instance.getNbItems());
    assertEquals(nbItemsPerPage, instance.getNbItemsPerPage());
    assertEquals(firstItemIndex, instance.getFirstItemIndex());
  }

  /**
   * Test of printCounter method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintCounterWithTwoPages() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printCounter();
    assertNotNull(result);
    assertEquals("1 - 10 / 14 ", result);

    firstItemIndex = 12;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    result = instance.printCounter();
    assertNotNull(result);
    assertEquals("13 - 14 / 14 ", result);
  }

  /**
   * Test of printCounter method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintCounterWithOnePages() {
    int nbItems = 9;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printCounter();
    assertNotNull(result);
    assertEquals("9 ", result);
  }

  /**
   * Test of printIndex method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintIndex() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.setActionSuffix("");
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printIndex();
    assertNotNull(result);    
    assertEquals("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" "
        + "align=\"center\"><tr valign=\"middle\" class=\"intfdcolor\"><td align=\"center\">"
        + "&#160;&#160;&#160; 1  <a href=\"Pagination?Index=10\">2</a>  <a href=\"Pagination?Index=10\"><img src=\""
        + "/silverpeas//util/viewGenerator/icons/arrows/arrowRight.gif\" border=\"0\" "
        + "align=\"absmiddle\" alt=\"\"/></a></td></tr></table>", result);
    
    instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, 10);
    result = instance.printIndex();    
    assertNotNull(result);
    assertEquals("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" "
        + "align=\"center\"><tr valign=\"middle\" class=\"intfdcolor\"><td align=\"center\"> "
        + "<a href=\"Pagination?Index=0\"><img src=\"/silverpeas//util/viewGenerator/icons/arrows/"
        + "arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"\"/></a>  <a href="
        + "\"Pagination?Index=0\">1</a>  2 &#160;&#160;&#160;</td></tr></table>", result);
  }

  /**
   * Test of printIndex method, of class PaginationSilverpeasV5.
   */
  /*@Test
  public void testPrintIndex_String() {
    System.out.println("printIndex");
    String javascriptFunc = "";
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    String expResult = "";
    String result = instance.printIndex(javascriptFunc);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }*/

  /**
   * Test of print method, of class PaginationSilverpeasV5.
   */
  /*@Test
  public void testPrint() {
    System.out.println("print");
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    String expResult = "";
    String result = instance.print();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }*/
}